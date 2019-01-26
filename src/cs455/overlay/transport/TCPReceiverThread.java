package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TCPReceiverThread implements Runnable {

  private Socket socket;
  
  private DataInputStream din;
  
  /**
   * Default constructor - Initialize the TCPReceiverThread with the 
   * socket and data input stream information
   * 
   * @param socket
   * @throws IOException
   */
  public TCPReceiverThread(Socket socket) throws IOException {
    this.socket = socket;
    this.din = new DataInputStream(socket.getInputStream());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    int len;
    while (socket != null) {
      try {
        len = din.readInt();
        
        byte[] data = new byte[len];
        din.readFully(data, 0, len);
        
      } catch (SocketException e) {
        System.err.println("ERROR: " + e.getMessage());
        e.printStackTrace();
        break;
      } catch (IOException e) {
        System.err.println("ERROR: " + e.getMessage());
        e.printStackTrace();
        break;
      }
    }
  }

}
