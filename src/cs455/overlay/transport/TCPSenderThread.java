package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;

/**
 * Class used to send data, via <code>byte[]</code> to the receiver.
 * 
 * @author stock
 *
 */
public class TCPSenderThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, true );

  private Socket socket;

  private DataOutputStream dout;

  private ConcurrentLinkedQueue<Event> queue;

  /**
   * Default constructor - Initialize the TCPSender with the socket and
   * data output stream information
   * 
   * @param socket
   * @throws IOException
   */
  public TCPSenderThread(Socket socket) throws IOException {
    this.socket = socket;
    this.dout = new DataOutputStream( socket.getOutputStream() );
    this.queue = new ConcurrentLinkedQueue<>();
  }

  /**
   * Add a message to the current connection queue.
   * 
   * @param e
   */
  public synchronized void appendData(Event e) {
    queue.offer( e );
    notify();
  }

  /**
   * Send the data through the {@link TCPSenderThread#socket} using the
   * {@link TCPSenderThread#dout} data output stream. Write the length
   * first, and then the actual data - that way the receiver knows when
   * to stop reading.
   * 
   * @param data
   * @throws IOException
   */
  private void sendData(final byte[] data) throws IOException {
    LOG.debug( "Sending message to: " + socket.getRemoteSocketAddress() );
    int len = data.length;
    dout.writeInt( len );
    dout.write( data, 0, len );
    dout.flush();
  }

  private synchronized void trySending() throws InterruptedException, IOException {
    while ( queue.isEmpty() )
    {
      wait();
    }
    sendData( queue.poll().getBytes() );
  }
  
  /**
   * Retrieves and removes the head of the queue, and send the data as a
   * marshalled array of bytes.
   * 
   */
  @Override
  public void run() {
    while ( !socket.isClosed() )
    {
      try
      {
        trySending();
      } catch ( InterruptedException | IOException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
        break;
      }
    }
    LOG.info( "tcpSENDERthread" );
  }
}
