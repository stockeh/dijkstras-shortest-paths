package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
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
import cs455.overlay.wireformats.TaskInitiate;

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
  private static final Logger LOG = new Logger( true, true );

  private static final String PRINT_SHORTEST_PATH = "print-shortest-path";

  private static final String EXIT_OVERLAY = "exit-overlay";

  private static final String CONNECTIONS = "connections";

  private TCPConnection registryConnection;

  private LinkWeights linkWeights = null;

  private RoutingCache routes = null;

  private Map<String, TCPConnection> connections = new HashMap<>();

  private Integer nodePort;

  private String nodeHost;

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
   * @param host
   * @param port
   * @return
   */
  private void registerNode(String registryHost, Integer registryPort) {
    try
    {
      Socket socketToTheServer = new Socket( registryHost, registryPort );
      TCPConnection connection = new TCPConnection( this, socketToTheServer );

      Register register = new Register( Protocol.REGISTER_REQUEST,
          this.nodeHost, this.nodePort );

      LOG.debug( "MessagingNode ID: " + this.nodeHost + ":" + this.nodePort );
      connection.getTCPSenderThread().sendData( register.getBytes() );
      connection.start();

      this.registryConnection = connection;
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
    boolean running = true;
    while ( running )
    {
      Scanner scan = new Scanner( System.in );
      switch ( scan.nextLine() )
      {
        case PRINT_SHORTEST_PATH :
          break;

        case EXIT_OVERLAY :
          exitOverlay();
          running = false;
          break;

        case CONNECTIONS :
          LOG.info( "this: " + nodeHost + ":" + nodePort );
          for ( Entry<String, TCPConnection> mapEntry : connections.entrySet() )
          {
            LOG.info( mapEntry.getKey() );
          }
          break;
        default :
          LOG.info(
              "Not a valid command. USAGE: print-shortest-path | exit-overlay" );
          break;
      }
    }
  }

  /**
   * Remove the node from the registry. TODO: Do I close the socket
   * here? Current exceptions.
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
      } catch ( IOException e )
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
  private void acknowledgeNewConnection(Event event, TCPConnection connection) {
    String nodeDetails = (( Register ) event).getConnection();
    connections.put( nodeDetails, connection );
  }

  /**
   * Begin sending messages to randomly chosen "sink" nodes for N
   * rounds.
   * 
   * @param event
   */
  private void taskInitiate(Event event) {
    int rounds = (( TaskInitiate ) event).getNumRounds();
    LOG.debug( routes.toString() );
    /**
     * TODO: RETURN TO THIS Random random = new Random(); for ( int i = 0;
     * i < rounds; ++i ) { int payload = random.nextInt(); int position =
     * 0; // TODO: Randomly select sink node and get the routing path //
     * Get all the links, and then a random connection, and then split to
     * // get the sink node for that connection String sinkNode =
     * (linkWeights.getLinks())[random.nextInt( linkWeights.getNumLinks()
     * )] .split( " " )[1]; try { String[] routingPath = routes.getRoute(
     * sinkNode );
     * 
     * TCPConnection connection = connections.get( routingPath[position]
     * ); // Increment position for receiving node to handle Message msg =
     * new Message( Protocol.MESSAGE, payload, ++position, routingPath );
     * 
     * connection.getTCPSenderThread().sendData( msg.getBytes() ); } catch
     * ( NullPointerException | ClassCastException | IOException e ) {
     * LOG.error( e.getMessage() ); } }
     */
  }

  /**
   * Manage incoming messages by either forwarding the content, or
   * receiving the message.
   * 
   * @param event
   */
  private void messageHandler(Event event) {
    Message msg = ( Message ) event;
    String[] routingPath = msg.getRoutingPath();
    int position = msg.getPosition();

    if ( routingPath.length == position )
    {
      // At Sink Node
    } else
    {
      TCPConnection connection = connections.get( routingPath[position] );
      msg.incrementPosition();
      try
      {
        connection.getTCPSenderThread().sendData( msg.getBytes() );
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
      }
    }
  }
}
