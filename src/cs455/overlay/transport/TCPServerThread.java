package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import cs455.overlay.node.Node;
import cs455.overlay.util.Logger;

/**
 * A new TCP Server Thread is setup on the Registry and each new
 * Messaging Node to accept new connections.
 * 
 * Upon a new connection being made a TCP Connection is established on
 * to send and receive messages as a response. The thread is blocked
 * on the accept statement untill these new connections are established.
 * 
 * @author stock
 *
 */
public class TCPServerThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, true );

  private Node node;

  private ServerSocket serverSocket;

  /**
   * Default constructor - setup the server socket for the thread to run
   * on.
   * 
   * @param node
   * @param serverSocket
   */
  public TCPServerThread(Node node, ServerSocket serverSocket) {
    this.node = node;
    this.serverSocket = serverSocket;
  }

  /**
   * Listen for incoming connections and start a new thread for each
   * socket once connected.
   * 
   * {@inheritDoc}
   */
  @Override
  public void run() {
    while ( serverSocket != null )
    {
      try
      {
        Socket incomingConnectionSocket = serverSocket.accept();
        (new TCPConnection( node, incomingConnectionSocket )).start();
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        break;
      }
    }
  }
}
