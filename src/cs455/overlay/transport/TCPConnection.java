package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import cs455.overlay.node.Node;
import cs455.overlay.util.Logger;

/**
 * This class is used to establish a connection by starting a new
 * TCPSenderThread and TCPReceiverThread.
 * 
 * @author stock
 *
 */
public class TCPConnection {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  @SuppressWarnings( "unused" )
  private final static Logger LOG = new Logger( true, true );

  private Socket socket;

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
    this.socket = socket;
    // TODO: This helped increase the number of rounds for a time, but not
    // a definite solution
    // this.socket.setSendBufferSize( Integer.MAX_VALUE );
    this.sender = new TCPSenderThread( this.socket );
    this.receiver = new TCPReceiverThread( node, this.socket, this );
  }

  /**
   * Get the Socket for the connection to verify Inet information
   * 
   * @return the socket for the connection.
   */
  public Socket getSocket() {
    return this.socket;
  }

  /**
   * Get the TCPSenderThread so the client or server can send a message
   * over the socket
   * 
   * @return the TCPSenderThread instance that was instantiated during
   *         the {@link #run()} method of the new thread.
   */
  public TCPSenderThread getTCPSenderThread() {
    return this.sender;
  }

  /**
   * Allow the TCPConnection to start receiving messages.
   * 
   */
  public void start() {
    (new Thread( this.receiver )).start();
    (new Thread( this.sender  )).start();
  }

  /**
   * Close the socket sender and receiver. Use a one second wait to
   * ensure all remaining messages are sent.
   * 
   * @throws IOException
   * @throws InterruptedException
   */
  public void close() throws IOException, InterruptedException {
    TimeUnit.SECONDS.sleep( 1 );
    this.socket.close();
    this.sender.dout.close();
    this.receiver.din.close();
  }
}
