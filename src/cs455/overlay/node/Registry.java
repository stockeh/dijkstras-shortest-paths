package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Logger;
import cs455.overlay.util.OverlayCreator;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskInitiate;

/**
 * Maintains information about the registered messaging nodes.
 *
 * @author stock
 *
 */
public class Registry implements Node {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private static Map<String, TCPConnection> connections = new HashMap<>();

  private static final String LIST_MSG_NODES = "list-messaging-nodes";

  private static final String SETUP_OVERLAY = "setup-overlay";

  private static final String LIST_WEIGHTS = "list-weights";

  private static final String START = "start";

  private LinkWeights linkWeights = null;

  /**
   * Stands-up the registry.
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
      String line = scan.nextLine();
      String[] input = line.split( " " );
      switch ( input[0] )
      {
        case SETUP_OVERLAY :
          setupOverlay( input );
          break;

        case LIST_MSG_NODES :
          LOG.info( "list-messaging-nodes" );
          break;

        case LIST_WEIGHTS :
          if ( linkWeights != null )
          {
            LOG.info( linkWeights.toString() );
          } else
          {
            LOG.error( "The overlay has not yet been configured." );
          }
          break;

        case START :
          taskInitiate( input );
          break;

        default :
          LOG.info( "Not a valid command" );
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
        .getRemoteSocketAddress().toString().split( ":" )[0].substring( 1 ),
        register );
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
      status = Protocol.FAILURE;
    }

    RegisterResponse response =
        new RegisterResponse( Protocol.REGISTER_RESPONSE, status, message );
    try
    {
      connection.getTCPSenderThread().sendData( response.getBytes() );
    } catch ( IOException e )
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
    // TODO: Check IP LOG.debug( "Connection IP: " + connectionIP );
    if ( !nodeDetails.split( ":" )[0].equals( connectionIP )
        && !connectionIP.equals( "127.0.0.1" ) )
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
    int connectingEdges = 2;
    try
    {
      connectingEdges = Integer.parseInt( input[1] );
    } catch ( ArrayIndexOutOfBoundsException | NumberFormatException e )
    {
      LOG.error( "Input did not contain a valid number of message connections. "
          + "Defaulting to each having " + connectingEdges + " links." );
    }
    try
    {
      this.linkWeights =
          (new OverlayCreator()).setupOverlay( connections, connectingEdges );
    } catch ( Exception e )
    {
      LOG.error( e.getMessage() );
    }
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
   * <li>Overlay topology is created</li>
   * <li>Link weights are distributed</li>
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
      LOG.error( "Input did not contain a valid number of rounds. "
          + "Defaulting to each having " + rounds + " rounds." );
    }
    TaskInitiate startTask = new TaskInitiate( Protocol.TASK_INITIATE, rounds );
    for ( Entry<String, TCPConnection> entry : connections.entrySet() )
    {
      try
      {
        entry.getValue().getTCPSenderThread().sendData( startTask.getBytes() );
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        // TODO: Return if unable to send to one connection?
      }
    }
  }
}
