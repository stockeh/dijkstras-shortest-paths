package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import cs455.overlay.util.Logger;

/**
 * Class used to send data, via <code>byte[]</code> to the receiver.
 * 
 * @author stock
 *
 */
public class TCPSenderThread {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, true );

  private Socket socket;

  protected DataOutputStream dout;
  
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
  public void sendData(final byte[] data) throws IOException {
//    LOG.debug( "Sending message to: " + socket.getRemoteSocketAddress() );
    int len = data.length;
    dout.writeInt( len );
    dout.write( data, 0, len );
    dout.flush();
  }
}
