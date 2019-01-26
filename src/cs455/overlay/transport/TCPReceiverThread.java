package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPReceiverThread implements Runnable {

  private final static Logger LOG = new Logger(true);
  
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
        
        EventFactory eventFactory = EventFactory.getInstance();
        Event event = eventFactory.createEvent(data);
        LOG.info(event.toString());
 
      } catch (SocketException e) {
        LOG.error(e.getMessage());
        e.printStackTrace();
        break;
        
      } catch (IOException e) {
        LOG.error(e.getMessage());
        e.printStackTrace();
        break;
      }
    }
  }
}
