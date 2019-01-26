package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class used to send data, via <code>byte[]</code> to the
 * receiver.
 * 
 * @author stock
 *
 */
public class TCPSender {

  @SuppressWarnings("unused")
  private Socket socket;
  
  private DataOutputStream dout;
  
  /**
   * Default constructor - Initialize the TCPSender with the socket
   * and data output stream information
   * 
   * @param socket
   * @throws IOException
   */
  public TCPSender(Socket socket) throws IOException {
    this.socket = socket;
    this.dout = new DataOutputStream(socket.getOutputStream());
  }
  
  /**
   * Send the data through the {@link TCPSender#socket}
   * using the {@link TCPSender#dout} data output stream.
   * Write the length first, and then the actual data - 
   * that way the receiver knows when to stop reading.
   * 
   * @param data
   * @throws IOException
   */
  public void sendData(final byte[] data) throws IOException {
    int len = data.length;
    dout.writeInt(len);
    dout.write(data, 0, len);
    dout.flush();
  }
  
}
