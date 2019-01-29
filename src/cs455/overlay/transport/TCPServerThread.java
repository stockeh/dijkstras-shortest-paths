package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import cs455.overlay.node.Node;
import cs455.overlay.util.Logger;

public class TCPServerThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, true );

  private Node node;

  private ServerSocket serverSocket;

  public TCPServerThread(Node node, ServerSocket serverSocket) {
    this.node = node;
    this.serverSocket = serverSocket;
  }

  /**
   * Listen for incoming connections and start a new thread for each
   * socket once connected.
   * 
   * {@inheritDoc}
   */
  @Override
  public void run() {
    while ( serverSocket != null )
    {
      try
      {
        Socket socket = serverSocket.accept();
        ( new TCPConnection( node, socket ) ).start();
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
        break;
      }
    }
    LOG.info( "tcpSERVERthread" );
  }
}
