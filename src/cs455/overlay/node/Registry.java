package cs455.overlay.node;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Maintains information about the registered
 * messaging nodes.
 *
 * @author Jason Stock
 *
 */
public class Registry {

  /**
   * Stands-up the registry.
   *
   * @param args
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println(
        "USAGE: java cs455.overlay.node.Registry portnum");
      return;
    }
    Registry registry = new Registry();
    try (ServerSocket serverSocket = new ServerSocket(Integer.valueOf(args[0]))) {
      registry.connectMessagingNode(serverSocket);
    } catch (IOException e) {
      System.err.println("ERROR: ServerSocket Exception:" + e.getMessage());
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
      try (
        Socket socket = serverSocket.accept();
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
      ) {
        Integer msgLength = inputStream.readInt();
        byte[] ipAddress = new byte[msgLength];
        inputStream.readFully(ipAddress, 0, msgLength);
        System.out.println("IP message: " + new String(ipAddress, StandardCharsets.UTF_8));
        byte[] status;
        
      } catch (IOException e) {
        System.err.println("ERROR: Socket Exception:" + e.getMessage());
        e.printStackTrace();
      }
    }
  }

}
