package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import cs455.overlay.node.Node;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPReceiverThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, true );

  private Socket socket;

  private DataInputStream din;

  private Node node;

  /**
   * Default constructor - Initialize the TCPReceiverThread with the
   * socket and data input stream information
   * 
   * @param socket
   * @param node
   * @throws IOException
   */
  public TCPReceiverThread(Socket socket, Node node) throws IOException {
    this.socket = socket;
    this.din = new DataInputStream( socket.getInputStream() );
    this.node = node;
  }

  /**
   * 
   * 
   * {@inheritDoc}
   */
  @Override
  public void run() {
    if ( socket != null )
    {
      try
      {
        int len = din.readInt();

        byte[] data = new byte[len];
        din.readFully( data, 0, len );

        EventFactory eventFactory = EventFactory.getInstance();
        Event event = eventFactory.createEvent( data );
        node.onEvent( event );

      } catch ( SocketException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();

      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
      }
    }
  }
}
