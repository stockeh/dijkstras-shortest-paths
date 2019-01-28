package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;
import cs455.overlay.node.Node;
import cs455.overlay.util.Logger;

/**
 * This class is used to establish a connection by starting a new
 * TCPSenderThread and TCPReceiverThread.
 * 
 * @author stock
 *
 */
public class TCPConnection implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  @SuppressWarnings( "unused" )
  private final static Logger LOG = new Logger( true, true );

  @SuppressWarnings( "unused" )
  private Node node;

  private TCPSenderThread sender;

  private TCPReceiverThread receiver;

  /**
   * Default constructor - create a new TCPConnection given a Node,
   * i.e., MessageNode or Registry, and the socket for the connection.
   * 
   * @param node
   * @param socket
   * @throws IOException
   */
  public TCPConnection(Node node, Socket socket) throws IOException {
    this.node = node;
    this.sender = new TCPSenderThread( socket );
    this.receiver = new TCPReceiverThread( node, socket, this );
  }

  /**
   * Get the TCPSenderThread so the client or server can send a message
   * over the socket
   * 
   * @return Returns the TCPSenderThread instance that was instantiated
   *         during the {@link #run()} method of the new thread.
   */
  public TCPSenderThread getTCPSenderThread() {
    return sender;
  }

  @Override
  public void run() {
    (new Thread( sender )).start();
    (new Thread( receiver )).start();
  }
}
