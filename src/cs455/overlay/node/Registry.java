package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Logger;
import cs455.overlay.util.OverlayCreator;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.TaskSummaryRequest;
import cs455.overlay.wireformats.TaskSummaryResponse;

/**
 * Maintains information about the registered messaging nodes.
 * 
 * The registry is the brain of the network overlay. It is in charge
 * of registering / deregistering messaging nodes, setting up the
 * overlay, sending schematics to the nodes, and starting the process
 * of message sending. There will only be <b>one</> instance of the
 * registry in the network - this is tied to a specific port number
 * upon startup.
 *
 * @author stock
 *
 */
public class Registry implements Node {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private static final String LIST_MSG_NODES = "list-messaging-nodes";

  private static final String SETUP_OVERLAY = "setup-overlay";

  private static final String SEND_LINK_WEIGHTS = "send-overlay-link-weights";

  private static final String LIST_WEIGHTS = "list-weights";

  private static final String START = "start";

  private Map<String, TCPConnection> connections = new HashMap<>();

  private List<TaskSummaryResponse> statisticsSummary = new ArrayList<>();

  private LinkWeights linkWeights = null;

  private AtomicInteger receivedCompletedTasks = new AtomicInteger( 0 );

  /**
   * Stands-up the registry as an entry point to the class.
   *
   * @param args
   */
  public static void main(String[] args) {
    if ( args.length < 1 )
    {
      LOG.error( "USAGE: java cs455.overlay.node.Registry portnum" );
      return;
    }

    LOG.info( "Registry starting up at: " + new Date() );
    Registry registry = new Registry();
    try ( ServerSocket serverSocket =
        new ServerSocket( Integer.valueOf( args[0] ) ) )
    {
      (new Thread( new TCPServerThread( registry, serverSocket ) )).start();

      registry.interact();

    } catch ( IOException e )
    {
      LOG.error( e.getMessage() );
      e.printStackTrace();
    }
  }

  /**
   * Allow support for commands to be specified while the processes are
   * running.
   */
  @SuppressWarnings( "resource" )
  private void interact() {
    LOG.info( "Input a command to interact with processes" );
    Scanner scan = new Scanner( System.in );
    while ( true )
    {
      String line = scan.nextLine().toLowerCase();
      String[] input = line.split( " " );
      switch ( input[0] )
      {
        case SETUP_OVERLAY :
          setupOverlay( input );
          break;

        case SEND_LINK_WEIGHTS :
          sendLinkWeights();
          break;

        case LIST_MSG_NODES :
          if ( connections.size() == 0 )
          {
            LOG.error(
                "There are no connections in the registry. Initialize new messaging nodes." );
          } else
          {
            System.out.println(
                "\nThere are " + connections.size() + " total links" );
            connections.forEach( (k, v) -> System.out.println( k ) );
            System.out.println();
          }
          break;

        case LIST_WEIGHTS :
          if ( linkWeights == null )
          {
            LOG.error( "The overlay has not yet been configured." );

          } else
          {
            System.out.println( linkWeights.toString() );
          }
          break;

        case START :
          taskInitiate( input );
          break;

        default :
          LOG.error( "Unable to process. Please enter a valid command!" );
          break;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEvent(Event event, TCPConnection connection) {
    LOG.debug( event.toString() );
    switch ( event.getType() )
    {
      case Protocol.REGISTER_REQUEST :
        registrationHandler( event, connection, true );
        break;

      case Protocol.DEREGISTER_REQUEST :
        registrationHandler( event, connection, false );
        break;

      case Protocol.TASK_COMPLETE :
        completedTaskHandler();
        break;

      case Protocol.TRAFFIC_SUMMARY :
        trafficResonseHandler( event );
        break;
    }
  }

  /**
   * Manage the registry synchronously by either registering a new
   * MessagingNode or removing one from the system.
   * 
   * @param event the object containing node details
   * @param connection the connection details, i.e., TCPSenderThread
   * @param register true to register new node, false to remove it
   */
  private synchronized void registrationHandler(Event event,
      TCPConnection connection, final boolean register) {
    String nodeDetails = (( Register ) event).getConnection();
    String message = registerStatusMessage( nodeDetails, connection.getSocket()
        .getInetAddress().getHostName().split("\\.")[0], register );
    byte status;
    if ( message.length() == 0 )
    {
      if ( register )
      {
        connections.put( nodeDetails, connection );
      } else
      {
        connections.remove( nodeDetails );
      }
      message =
          "Registration request successful.  The number of messaging nodes currently "
              + "constituting the overlay is (" + connections.size() + ").";
      status = Protocol.SUCCESS;
    } else
    {
      LOG.error( "Unable to process request. Responding with a failure." );
      status = Protocol.FAILURE;
    }
    LOG.debug(message);
    RegisterResponse response = new RegisterResponse( status, message );
    try
    {
      connection.getTCPSenderThread().sendData( response.getBytes() );
    } catch ( IOException | InterruptedException e )
    {
      LOG.error( e.getMessage() );
      e.printStackTrace();
    }
  }

  /**
   * Verify the node had <b>not</b> previously been registered, and the
   * address that is specified in the registration request and the IP
   * address of the request (the socket’s input stream) match.
   * 
   * @param nodeDetails the host:port from the event message (request)
   * @param connectionIP the remote socket IP address from the current
   *        TCPConnection
   * @return a <code>String</code> containing the error message, or
   *         otherwise empty
   */
  private String registerStatusMessage(String nodeDetails, String connectionIP,
      final boolean register) {
    LOG.debug( "Node Details : " + nodeDetails );
    LOG.debug( "Connection IP: " + connectionIP );
    String message = "";
    if ( connections.containsKey( nodeDetails ) && register )
    {
      message =
          "The node, " + nodeDetails + " had previously registered and has "
              + "a valid entry in its registry. ";
    } else if ( !connections.containsKey( nodeDetails ) && !register )
    { // The case that the item is not in the registry.
      message =
          "The node, " + nodeDetails + " had not previously been registered. ";
    }
    if ( !nodeDetails.split( ":" )[0].equals( connectionIP ) )
    {
      message +=
          "There is a mismatch in the address that isspecified in request and "
              + "the IP of the socket.";
    }
    return message;
  }

  /**
   * Setup the overlay for the messaging nodes. This is done by first
   * creating the topology and sending the connections within the
   * {@link OverlayCreator}. The link weights are returned by this class
   * and sent to each messaging node in the overlay.
   * 
   * @param input The arguments passed by the command line interpreter
   */
  private void setupOverlay(String[] input) {
    int connectingEdges = connections.size() < 5 ? connections.size() - 1 : 4;
    try
    {
      connectingEdges = Integer.parseInt( input[1] );
    } catch ( ArrayIndexOutOfBoundsException | NumberFormatException e )
    {
      LOG.info( "Input did not contain a valid number of message connections. "
          + "Defaulting to each having " + connectingEdges + " links." );
    }
    try
    {
      this.linkWeights =
          (new OverlayCreator()).setupOverlay( connections, connectingEdges );
    } catch ( Exception e )
    {
      LOG.error( e.getMessage()
          + "\nUnable to send overlay information to connection." );
      return;
    }
    System.out.println( "\nOverlay configuration has been sent to the "
        + connections.size() + " connections in the network.\n" );
  }

  /**
   * Void method in charge of sending link weights to each of the
   * connections in the overlay. It is expected that the overlay
   * topology has been created prior with <i>setup-overlay N</i>.
   * 
   * Once received, the client will compute the routing cache for the
   * topology from each given node.
   */
  private void sendLinkWeights() {
    if ( linkWeights == null )
    {
      LOG.error(
          "The overlay has not yet been configured, and there are no link wieghts" );
      return;
    }
    connections.forEach( (k, v) ->
    {
      try
      {
        v.getTCPSenderThread().sendData( linkWeights.getBytes() );
      } catch ( IOException | InterruptedException e )
      {
        LOG.error(
            e.getMessage() + "\nUnable to send link weights to connection." );
        return;
      }
    } );
    System.out.println( "\nOverlay Link Weights have been sent to the "
        + connections.size() + " connections in the network.\n" );
  }

  /**
   * Each node in the overlay will be responsible for sending N rounds
   * of messages. The number of rounds is specified from the command
   * line with the <b>start N</b> command. This method will send a
   * message to each of the registered messaging nodes to start sending
   * messages.
   * 
   * By the time this method is instantiated, the following have taken
   * place:
   * 
   * <ul>
   * <li>Overlay topology is created: <i>setup-overlay N</i></li>
   * <li>Link weights are distributed:
   * <i>send-overlay-link-weights</i></li>
   * <li>Routing paths have been computed on the messaging nodes</li>
   * </ul>
   * 
   * @param input foreground command from scanner input.
   */
  private void taskInitiate(String[] input) {
    int rounds = 1;
    try
    {
      rounds = Integer.parseInt( input[1] );
    } catch ( ArrayIndexOutOfBoundsException | NumberFormatException e )
    {
      LOG.info( "Input did not contain a valid number of rounds. "
          + "Defaulting to each having " + rounds + " rounds." );
    }
    TaskInitiate startTask = new TaskInitiate( rounds );
    connections.forEach( (k, v) ->
    {
      try
      {
        v.getTCPSenderThread().sendData( startTask.getBytes() );
      } catch ( IOException | InterruptedException e )
      {
        LOG.error( e.getMessage() );
        // TODO: Return if unable to send to one connection?
      }
    } );
  }

  /**
   * TODO: It may be better to verify the connections that are received
   * by host:port identifier. Then if there is a specific node missing,
   * it can be handled more seriously.
   * 
   * Increment for every received completed task and compare with the
   * total number in the registry. If all tasks have <i>completed</i>
   * then wait for a few seconds, to ensure all transit messages are
   * delivered. Then request to pull a traffic summary.
   * 
   * @param event
   */
  private void completedTaskHandler() {
    receivedCompletedTasks.getAndIncrement();
    LOG.debug( "TASK HANDLER: " + receivedCompletedTasks.toString()
        + " , " + Integer.toString( connections.size() ) );
    if ( receivedCompletedTasks.get() == connections.size() )
    {
      try
      {
        // Sleep for 15 seconds to allow all messages to be received.
        TimeUnit.SECONDS.sleep( 15 );
      } catch ( InterruptedException e )
      {
        LOG.error( "Unable to sleep thread: " + e.getMessage() );
      }
      connections.forEach( (k, connection) ->
      {
        TaskSummaryRequest request = new TaskSummaryRequest();
        try
        {
          connection.getTCPSenderThread().sendData( request.getBytes() );
        } catch ( IOException | InterruptedException e )
        {
          LOG.error(
              e.getMessage() + "\nUnable to send link weights to connection." );
          return;
        }
      } );
      receivedCompletedTasks.set( 0 );
    }
  }

  /**
   * Display the statistics from the messaging nodes after the start
   * task initiation has occurred, i.e., messages were transfered
   * amongst the overlay.
   * 
   * @param event
   */
  private synchronized void trafficResonseHandler(Event event) {
    statisticsSummary.add( ( TaskSummaryResponse ) event );
    LOG.debug( "TRAFFIC HANDLE: " + Integer.toString( statisticsSummary.size() )
        + " , " + Integer.toString( connections.size() ) );
    if ( statisticsSummary.size() == connections.size() )
    {
      (new StatisticsCollectorAndDisplay()).display( statisticsSummary );
      statisticsSummary.clear();
    }
  }
}
