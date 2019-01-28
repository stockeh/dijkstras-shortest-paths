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

  private TCPConnection connection;

  /**
   * Default constructor - Initialize the TCPReceiverThread with the
   * socket and data input stream information
   * 
   * @param node
   * @param socket
   * @throws IOException
   */
  public TCPReceiverThread(Node node, Socket socket, TCPConnection connection)
      throws IOException {
    this.node = node;
    this.socket = socket;
    this.connection = connection;
    this.din = new DataInputStream( socket.getInputStream() );
  }

  /**
   * Start running the thread to read from the data input stream. Create
   * an event from the data and handle it appropriately.
   * 
   * {@inheritDoc}
   */
  @Override
  public void run() {
    while ( socket != null )
    {
      try
      {
        int len = din.readInt();

        byte[] data = new byte[len];
        din.readFully( data, 0, len );

        EventFactory eventFactory = EventFactory.getInstance();
        Event event = eventFactory.createEvent( data );
        node.onEvent( event, connection );

      } catch ( SocketException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
        break;

      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        e.printStackTrace();
        break;
      }
    }
    try // TODO: Check if this is the right place to close...
    {
      socket.close();
      din.close();
    } catch ( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
