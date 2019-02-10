package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import cs455.overlay.util.Logger;

/**
 * Class used to send data, via <code>byte[]</code> to the receiver.
 * 
 * Running as a thread, the TCPConnection holds an instance to the
 * sender for new messages. This makes use of a linked blocking queue
 * to buffer the rate at which messages are being sent.
 * 
 * @author stock
 *
 */
public class TCPSenderThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, false );

  protected DataOutputStream dout;

  private LinkedBlockingQueue<byte[]> queue;


  /**
   * Default constructor - Initialize the TCPSenderThread with the queue
   * size and data output stream information from the
   * <code>socket</code>.
   * 
   * @param socket
   * @throws IOException
   */
  public TCPSenderThread(Socket socket) throws IOException {
    final int defaultQueueSize = 100;
    this.queue = new LinkedBlockingQueue<>( defaultQueueSize );
    this.dout = new DataOutputStream( socket.getOutputStream() );
  }

  /**
   * Send the data to the linked blocking queue, waiting if necessary
   * for space to become available.
   * 
   * @param data that will be added to the tail of the queue.
   * @throws InterruptedException
   */
  public void sendData(final byte[] data) throws InterruptedException {
    queue.put( data );
  }

  /**
   * Send the data through the socket connection using the data output
   * stream. Write the length first, and then the actual data - that way
   * the receiver knows when to stop reading.
   * 
   * This block on the <code>queue.take()</code> method until there is
   * data to be read on the queue.
   */
  @Override
  public void run() {
    while ( true )
    {
      try
      {
        byte[] data = queue.take();
        int len = data.length;
        dout.writeInt( len );
        dout.write( data, 0, len );
        dout.flush();

      } catch ( InterruptedException | IOException e )
      {
        LOG.error( e.getMessage() );
      }
    }
  }
}
