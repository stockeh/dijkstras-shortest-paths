package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;

/**
 * Maintains information about the registered messaging nodes.
 *
 * @author stock
 *
 */
public class Registry implements Node {

  private final static Logger LOG = new Logger(true);

  /**
   * Stands-up the registry.
   *
   * @param args
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      LOG.error("USAGE: java cs455.overlay.node.Registry portnum");
      return;
    }
    LOG.info("Starting up");
    Registry registry = new Registry();
    try (ServerSocket serverSocket = new ServerSocket(Integer.valueOf(args[0]))) {
      registry.connectMessagingNode(serverSocket);
    } catch (IOException e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Listens and connects new messaging nodes.
   *
   * @param serverSocket
   */
  private void connectMessagingNode(ServerSocket serverSocket) {
    
    while (true) {
      try {
        Socket socket = serverSocket.accept();
        (new Thread(new TCPReceiverThread(socket))).start();
        
        String remoteHost = socket.getRemoteSocketAddress().toString().substring(1);
        LOG.info("InetAddress: " + remoteHost);
        
      } catch (IOException e) {
        LOG.error(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onEvent(Event event) {
    switch (event.getType())
    {
      case Protocol.REGISTER_REQUEST:
        LOG.info("SOMETHING: " + event.getType());
        break;
    }
    
  }

}
