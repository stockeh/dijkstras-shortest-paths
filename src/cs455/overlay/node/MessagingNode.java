package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import cs455.overlay.dijkstra.RoutingCache;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.Message;
import cs455.overlay.wireformats.MessagingNodeList;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.TaskSummaryResponse;

/**
 * Messaging nodes initiate and accept both communications and
 * messages within the system.
 *
 * @author stock
 *
 */
public class MessagingNode implements Node, Protocol {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private static final String PRINT_SHORTEST_PATH = "print-shortest-path";

  private static final String EXIT_OVERLAY = "exit-overlay";

  private static final String CONNECTIONS = "connections";

  private TCPConnection registryConnection;

  private LinkWeights linkWeights = null;

  private RoutingCache routes = null;

  private Map<String, TCPConnection> connections = new HashMap<>();

  private Integer nodePort;

  private String nodeHost;

  /**
   * Messaging Node Statistics
   */
  private AtomicInteger sendTracker = new AtomicInteger( 0 );

  private AtomicInteger receiveTracker = new AtomicInteger( 0 );

  private AtomicInteger relayTracker = new AtomicInteger( 0 );

  private AtomicLong sendSummation = new AtomicLong( 0 );

  private AtomicLong receiveSummation = new AtomicLong( 0 );

  /**
   * Default constructor - creates a new messaging node tying the
   * <b>host:port</b> combination for the node as the identifier for
   * itself.
   * 
   * @param nodeHost
   * @param nodePort
   */
  private MessagingNode(String nodeHost, int nodePort) {
    this.nodeHost = nodeHost;
    this.nodePort = nodePort;
  }

  /**
   * Start up a new TCPServerThread for the messaging node to listen on
   * then register the node.
   *
   * @param args
   */
  public static void main(String[] args) {
    if ( args.length < 2 )
    {
      LOG.error(
          "USAGE: java cs455.overlay.node.MessagingNode registry-host registry-port" );
      System.exit( 1 );
    }
    LOG.info( "Messaging Node starting up at: " + new Date() );
    try ( ServerSocket serverSocket = new ServerSocket( 0 ) )
    {
      int nodePort = serverSocket.getLocalPort();
      // TODO: check host address
      // InetAddress.getLocalHost().getHostAddress()
      MessagingNode node = new MessagingNode(
          serverSocket.getInetAddress().getHostName(), nodePort );
      (new Thread( new TCPServerThread( node, serverSocket ) )).start();
      node.registerNode( args[0], Integer.valueOf( args[1] ) );
      node.interact();
    } catch ( IOException e )
    {
      LOG.error( "Exiting " + e.getMessage() );
      e.printStackTrace();
    }
  }

  /**
   * Registers a node with the registry.
   *
   * @param host identifier for the registry node.
   * @param port number for the registry node
   */
  private void registerNode(String registryHost, Integer registryPort) {
    try
    {
      Socket socketToTheServer = new Socket( registryHost, registryPort );
      TCPConnection connection = new TCPConnection( this, socketToTheServer );

      Register register = new Register( Protocol.REGISTER_REQUEST,
          this.nodeHost, this.nodePort );

      LOG.info(
          "MessagingNode Identifier: " + this.nodeHost + ":" + this.nodePort );
      connection.getTCPSenderThread().sendData( register.getBytes() );
      connection.start();

      this.registryConnection = connection;
    } catch ( IOException | InterruptedException e )
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
    boolean running = true;
    while ( running )
    {
      Scanner scan = new Scanner( System.in );
      switch ( scan.nextLine().toLowerCase() )
      {
        case PRINT_SHORTEST_PATH :
          printShortestPath();
          break;

        case EXIT_OVERLAY :
          exitOverlay();
          running = false;
          break;

        case CONNECTIONS :
          System.out
              .println( "\nthis connections - " + nodeHost + ":" + nodePort );
          connections.forEach( (k, v) -> System.out.println( k ) );
          System.out.println();
          break;
        default :
          LOG.info(
              "Not a valid command. USAGE: print-shortest-path | exit-overlay" );
          break;
      }
    }
  }

  /**
   * Remove the node from the registry. This must occur prior to setting
   * up the overlay on the registry.
   * 
   * TODO: Do I close the socket here? Current exceptions.
   */
  private void exitOverlay() {
    LOG.debug( "HOST:PORT " + this.nodeHost + ":"
        + Integer.toString( this.nodePort ) + " is leaving the overlay" );

    Register register = new Register( Protocol.DEREGISTER_REQUEST,
        this.nodeHost, this.nodePort );

    try
    {
      registryConnection.getTCPSenderThread().sendData( register.getBytes() );
      registryConnection.close();
    } catch ( IOException | InterruptedException e )
    {
      LOG.error( e.getMessage() );
      e.printStackTrace();
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
      case Protocol.MESSAGING_NODE_LIST :
        establishOverlayConnections( event );
        break;

      case Protocol.REGISTER_REQUEST :
        acknowledgeNewConnection( event, connection );
        break;

      case Protocol.LINK_WEIGHTS :
        linkWeights = ( LinkWeights ) event;
        routes = new RoutingCache( linkWeights, nodeHost + ":" + nodePort );
        LOG.info(
            "Link weights are received and processed. Ready to send messages." );
        break;

      case Protocol.TASK_INITIATE :
        taskInitiate( event );
        break;

      case Protocol.MESSAGE :
        messageHandler( event );
        break;

      case Protocol.PULL_TRAFFIC_SUMMARY :
        sendTrafficSummary();
        break;
    }
  }

  /**
   * Establish a connection with other nodes in the topology as
   * specified by the registry.
   * 
   * @param event
   */
  private void establishOverlayConnections(Event event) {
    List<String> peers = (( MessagingNodeList ) event).getPeers();

    for ( String peer : peers )
    {
      String[] info = peer.split( ":" );
      Socket socketToMessagingNode = null;
      try
      {
        socketToMessagingNode =
            new Socket( info[0], Integer.parseInt( info[1] ) );
      } catch ( NumberFormatException | IOException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
        return;
      }
      try
      {
        TCPConnection connection =
            new TCPConnection( this, socketToMessagingNode );
        Register register = new Register( Protocol.REGISTER_REQUEST,
            this.nodeHost, this.nodePort );
        connection.getTCPSenderThread().sendData( register.getBytes() );
        connection.start();
        // Add "outgoing" connection to this.connections
        connections.put( peer, connection );
      } catch ( IOException | InterruptedException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
      }
    }
  }

  /**
   * Acknowledge "incoming" connections and add connection to
   * this.connections. Allows for this to send bidirectional message.
   * 
   * @param event
   * @param connection
   */
  private synchronized void acknowledgeNewConnection(Event event,
      TCPConnection connection) {
    String nodeDetails = (( Register ) event).getConnection();
    connections.put( nodeDetails, connection );
  }

  /**
   * Begin sending messages to randomly chosen <i>sink</i> nodes for N
   * rounds. The statistics for this node begin to populate here as the
   * following:
   * 
   * <ul>
   * <li>sendSummation: summation of the <i>random</i> sent payload</li>
   * <li>sendTracker: the number of messages that were <b>sent</b></li>
   * </ul>
   * 
   * Once a node has finished sending messages for all the rounds, a
   * task completion message is send back to the registry.
   * 
   * @param event received to retrieve the number of sending rounds
   * 
   */
  private void taskInitiate(Event event) {
    int rounds = (( TaskInitiate ) event).getNumRounds();

    Random random = new Random();
    for ( int i = 0; i < rounds; ++i )
    {
      int payload = random.nextInt();
      this.sendSummation.getAndAdd( payload );
      int position = 0;
      try
      {
        String sinkNode =
            routes.getConnection( random.nextInt( routes.numConnection() ) );
        String[] routingPath = routes.getRoute( sinkNode );
        LOG.debug( "New Route to: " + Arrays.toString( routingPath ) );
        TCPConnection connection = connections.get( routingPath[position] );
        Message msg =
            new Message( Protocol.MESSAGE, payload, ++position, routingPath );

        // TODO: java.nio.BufferUnderflowException starts here...
        connection.getTCPSenderThread().sendData( msg.getBytes() );
        this.sendTracker.getAndIncrement();
      } catch ( ArrayIndexOutOfBoundsException | NullPointerException
          | ClassCastException | IOException | InterruptedException e )
      {
        LOG.error( e.getMessage() );
      }
    }

    TaskComplete complete =
        new TaskComplete( Protocol.TASK_COMPLETE, nodeHost, nodePort );
    try
    {
      registryConnection.getTCPSenderThread().sendData( complete.getBytes() );
    } catch ( IOException | InterruptedException e )
    {
      LOG.error(
          "Unable to inform registry of task completion. " + e.getMessage() );
    }

  }

  /**
   * Manage incoming messages by either forwarding the content, or
   * receiving the message. The statistics for this node are updated as
   * new messages arrive as the following:
   * 
   * <ul>
   * <li>receiveSummation: summation of the <i>random</i> received
   * payload</li>
   * <li>receiveTracker: the number of messages that are
   * <b>received</b></li>
   * <li>relayTracker: the number of messages that are
   * <b>forwarded</b></li>
   * </ul>
   * 
   * @param event received to retrieve the message
   */
  private void messageHandler(Event event) {
    Message msg = ( Message ) event;
    String[] routingPath = msg.getRoutingPath();
    int position = msg.getPosition();

    if ( routingPath.length == position )
    {
      LOG.debug( "RECEIVED" );
      this.receiveTracker.getAndIncrement();
      this.receiveSummation.getAndAdd( msg.getPayload() );
    } else
    {
      TCPConnection connection = connections.get( routingPath[position] );
      msg.incrementPosition();
      try
      {
        LOG.debug( "FORWARDING to: " + routingPath[position] );
        connection.getTCPSenderThread().sendData( msg.getBytes() );
        this.relayTracker.getAndIncrement();
      } catch ( IOException | InterruptedException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
      }
    }
  }

  /**
   * Print the shortest path as computed by Dijkstras shortest path
   * algorithm for this node.
   * 
   * This is only possible <b>once</b> the overlay has been setup,
   * weights are distributed, and the routing cache is created.
   */
  private void printShortestPath() {
    if ( routes == null )
    {
      LOG.error( "Link weights have not yet been received from registry."
          + "Unable to display the shortest paths." );
    } else
    {
      try
      {
        routes.printShortestPath( linkWeights );
      } catch ( Exception e )
      {
        LOG.error( e.getMessage() + " : Unable to display shortest path." );
      }
    }
  }

  /**
   * Upon receiving a request for the traffic summary from the registry,
   * this node will respond with the messaging statistics and reset all
   * associated counters.
   */
  private void sendTrafficSummary() {
    TaskSummaryResponse response = new TaskSummaryResponse( nodeHost, nodePort,
        sendTracker.get(), sendSummation.get(), receiveTracker.get(),
        receiveSummation.get(), relayTracker.get() );

    try
    {
      registryConnection.getTCPSenderThread().sendData( response.getBytes() );
    } catch ( IOException | InterruptedException e )
    {
      LOG.error( "Unable to send traffic summary response. " + e.getMessage() );
    }
    this.sendTracker.set( 0 );
    this.receiveTracker.set( 0 );
    this.relayTracker.set( 0 );
    this.sendSummation.set( 0 );
    this.receiveSummation.set( 0 );;
  }
}
