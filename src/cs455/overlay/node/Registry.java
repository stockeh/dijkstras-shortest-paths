package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;

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

  private static Map<String, Integer> connections = new HashMap<>();

  private static final String LIST_MSG_NODES = "list-messaging-nodes";

  private static final String SETUP_OVERLAY = "setup-overlay";

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
    while ( true )
    {
      Scanner scan = new Scanner( System.in );
      switch ( scan.nextLine() )
      {
        case SETUP_OVERLAY :
          
          break;
          
        case LIST_MSG_NODES :
          LOG.info( "list-messaging-nodes" );
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
        connections.put( nodeDetails, 0 );
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
    connection.getTCPSenderThread().appendData( response );
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
    /**
     * TODO: Check connection IP from local host connections. CURRENTLY
     * NOT CHECKING
     */
    LOG.info( "NODE DETAILS: " + nodeDetails );
    LOG.info( "CONNECTION DETAILS: " + connectionIP );
    if ( !nodeDetails.split( ":" )[0].equals( connectionIP )
        && !connectionIP.equals( "127.0.0.1" ) )
    {
      message +=
          "There is a mismatch in the address that isspecified in request and "
              + "the IP of the socket.";
    }
    return message;
  }

}
