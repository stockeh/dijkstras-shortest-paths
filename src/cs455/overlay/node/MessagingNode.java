package cs455.overlay.node;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Messaging nodes initiate and accept both communications and messages within the system.
 *
 * @author Jason Stock
 *
 */
public class MessagingNode {

  /**
   * Diver for each messaging node.
   *
   * @param args
   */
  public static void main(String[] args) {
    if (args.length < 2 && (Integer.parseInt(args[1]) < 1024 || Integer.parseInt(args[1]) > 65535)) {
      System.err
          .println("USAGE: java cs455.overlay.node.MessagingNode registry-host registry-port");
      return;
    }
    // TODO: Initialize using TCPServerSocket to accept incoming TCP communications
    MessagingNode node = new MessagingNode();
    node.registerNode(args[0], Integer.valueOf(args[1]));
  }

  /**
   * Registers a node with the registry.
   *
   * @param host
   * @param port
   */
  private void registerNode(final String host, final Integer port) {
    InetAddress localhost = null;
    try {
      localhost = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      System.err.println(
          "ERROR: Local host name " + "could not be resolved into an address" + e.getMessage());
      e.printStackTrace();
      return;
    }
    try (Socket socket = new Socket(host, port);
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());) {
      String ipAddress = InetAddress.getLocalHost().getHostAddress();

      byte[] message = ipAddress.getBytes();
      Integer msgLength = message.length;

      outputStream.writeInt(msgLength);
      outputStream.write(message, 0, msgLength);
    } catch (IOException e) {
      System.err.println("ERROR: Client Exception:" + e.getMessage());
      e.printStackTrace();
    }
  }
}
