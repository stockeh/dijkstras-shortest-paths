package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.MessagingNodeList;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;

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

  private TCPConnection registryConnection;

  private Integer nodePort;

  private String nodeHost;

  public MessagingNode(String nodeHost, int nodePort) {
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
    }
  }

  private void establishOverlayConnections(Event event) {
    List<String> peers = (( MessagingNodeList ) event).getPeers();

    for ( String peer : peers )
    {
      String[] info = peer.split( ":" );
      Socket socketToTheServer = null;
      try
      {
        socketToTheServer = new Socket( info[0], Integer.parseInt( info[1] ) );
      } catch ( NumberFormatException | IOException e)
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
      }
      try
      {
        TCPConnection connection = new TCPConnection( this, socketToTheServer );
//        connection.getTCPSenderThread().sendData(  );
//        connection.start();
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
      }
    }
  }
}
