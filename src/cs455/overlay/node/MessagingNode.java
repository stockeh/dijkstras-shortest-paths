package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
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
  private final static Logger LOG = new Logger( true, true );

  private final static String PRINT_SHORTEST_PATH = "print-shortest-path";

  private final static String EXIT_OVERLAY = "exit-overlay";

  private final static String QUIT = "quit";

  private TCPConnection registryConnection;

  private Integer nodePort;

  private String nodeHost;

  public MessagingNode(String nodeHost, int nodePort) {
    this.nodeHost = nodeHost;
    this.nodePort = nodePort;
  }

  /**
   * Diver for each messaging node.
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
      serverSocket.getInetAddress();
      MessagingNode node = new MessagingNode(
          InetAddress.getLocalHost().getHostName(),
          nodePort );

      (new Thread( new TCPServerThread( node, serverSocket ) )).start();
      node.registerNode( args[0], Integer.valueOf( args[1] ) );
      node.interact();

    } catch ( IOException e )
    {
      LOG.error( "Exiting " + e.getMessage() );
      e.printStackTrace();
      System.exit( 1 );
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
      Socket socket = new Socket( registryHost, registryPort );
      TCPConnection connection = new TCPConnection( this, socket );

      Register register = new Register( Protocol.REGISTER_REQUEST,
          this.nodeHost, this.nodePort );

      connection.getTCPSenderThread().appendMessage( register );

      (new Thread( connection )).start();
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
    while ( true )
    {
      Scanner scan = new Scanner( System.in );
      switch ( scan.nextLine() )
      {
        case PRINT_SHORTEST_PATH :
          break;

        case EXIT_OVERLAY :
          exitOverlay();
          break;

        case QUIT :
          System.exit( 0 );

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

    registryConnection.getTCPSenderThread().appendMessage( register );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEvent(Event event, TCPConnection connection) {
    LOG.debug( event.toString() );
  }
}
