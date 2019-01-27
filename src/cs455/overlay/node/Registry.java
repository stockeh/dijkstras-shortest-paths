package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import cs455.overlay.transport.TCPSender;
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
  private final static Logger LOG = new Logger( true, true );

  private static Map<String, Integer> connections = new HashMap<>();

  private final static String LIST_MSG_NODES = "list-messaging-nodes";

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
   * Handle events delivered by messages TODO: Manage throws exception
   * 
   * @param event
   * @param socket
   * @throws IOException
   */
  @Override
  public void onEvent(Event event, Socket socket) throws IOException {
    LOG.debug( event.toString() );
    switch ( event.getType() )
    {
      case Protocol.REGISTER_REQUEST :
        registerNode( event, socket );
        break;
    }
  }

  /**
   * Register a new MessagingNode to the Registry. Verify the node had
   * <b>not</b> previously been registered, and the address that is
   * specified in the registration request and the IP address of the
   * request (the socket’s input stream) match.
   * 
   * @param event
   * @param socket
   * @throws IOException
   */
  private void registerNode(Event event, Socket socket) throws IOException {
    String connection = (( Register ) event).getConnection();
    byte status;
    String message = "I am from the registry";
    // TODO: check socket IP versus event IP. && boolean?
    if ( !connections.containsKey( connection ) )
    {
      // TODO: Figure out the value for key
      connections.put( connection, 0 );
      status = Protocol.SUCCESS;
    } else
    {
      status = Protocol.FAILURE;
    }
    
    TCPSender sender = new TCPSender( socket );
    RegisterResponse response =
        new RegisterResponse( Protocol.REGISTER_RESPONSE, status, message );
    sender.sendData( response.getBytes() );
  }

}
