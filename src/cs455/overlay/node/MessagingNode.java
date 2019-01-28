package cs455.overlay.node;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPSenderThread;
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

  private final static int MAX_PORTS = 65535;

  private final static String PRINT_SHORTEST_PATH = "print-shortest-path";

  private final static String EXIT_OVERLAY = "exit-overlay";

  /**
   * Diver for each messaging node.
   *
   * @param args
   */
  public static void main(String[] args) {
    if ( args.length < 2 && (Integer.parseInt( args[1] ) < 1024
        || Integer.parseInt( args[1] ) > 65535) )
    {
      LOG.error(
          "USAGE: java cs455.overlay.node.MessagingNode registry-host registry-port" );
      return;
    }
    LOG.info( "Messaging Node starting up at: " + new Date() );
    MessagingNode node = new MessagingNode();

    for ( int port = 1025; port < MAX_PORTS; )
    {
      try ( ServerSocket serverSocket = new ServerSocket( port ) )
      {

        (new Thread( new TCPServerThread( node, serverSocket ) )).start();
        node.registerNode( args[0], Integer.valueOf( args[1] ), port );
        node.interact();

      } catch ( BindException e )
      {
        LOG.error( e.getMessage() );
        ++port;
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        break;
      }
    }
  }

  /**
   * Registers a node with the registry.
   *
   * @param host
   * @param port
   */
  private void registerNode(final String registryHost,
      final Integer registryPort, final Integer port) {
    @SuppressWarnings( "unused" )
    InetAddress localhost = null;
    try
    {
      localhost = InetAddress.getLocalHost();
    } catch ( UnknownHostException e )
    {
      LOG.error( "Localhost name could not be resolved into an address"
          + e.getMessage() );
      e.printStackTrace();
      return;
    }
    try
    {
      Socket socket = new Socket( registryHost, registryPort );
      TCPConnection connection = new TCPConnection( this, socket );
      TCPSenderThread sender = connection.getTCPSenderThread();

      String ipAddress = InetAddress.getLocalHost().getHostAddress();
      Register register =
          new Register( Protocol.REGISTER_REQUEST, ipAddress, port );

      sender.appendMessage( register );
      (new Thread( connection )).start();
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
          LOG.info( "print-shortest-path" );
          break;

        case EXIT_OVERLAY :
          LOG.info( "exit-overlay" );
          break;

        default :
          LOG.info( "Not a valid command" );
          break;
      }
    }
  }

  @Override
  public void onEvent(Event event, TCPConnection connection) {
    LOG.debug( event.toString() );
  }
}
