package cs455.overlay.transport;

import java.net.ServerSocket;
import cs455.overlay.node.Node;
import cs455.overlay.util.Logger;

public class TCPConnection implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, true );

  private Node node;

  private ServerSocket serverSocket;

  public TCPConnection(Node node, ServerSocket serverSocket) {
    this.node = node;
    this.serverSocket = serverSocket;
  }

  @Override
  public void run() {

  }
}
