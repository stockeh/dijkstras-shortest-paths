package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;

/**
 * Messaging nodes initiate and accept both communications and messages within the system.
 *
 * @author Jason Stock
 *
 */
public class MessagingNode implements Node, Protocol {

  private final static Logger LOG = new Logger(true);
  
  /**
   * Diver for each messaging node.
   *
   * @param args
   */
  public static void main(String[] args) {
    if (args.length < 2 && (Integer.parseInt(args[1]) < 1024 || Integer.parseInt(args[1]) > 65535)) {
      LOG.error("USAGE: java cs455.overlay.node.MessagingNode registry-host registry-port");
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
      LOG.error(
          "ERROR: Local host name " + "could not be resolved into an address" + e.getMessage());
      e.printStackTrace();
      return;
    }
    try {
      Socket socket = new Socket(host, port);
      TCPSender sender = new TCPSender(socket);
      
      String ipAddress = InetAddress.getLocalHost().getHostAddress();
      Register register = new Register(Protocol.REGISTER_REQUEST, ipAddress, 5000);
      sender.sendData(register.getBytes());
      socket.close();
      
    } catch (IOException e) {
      System.err.println("ERROR: Client Exception:" + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void onEvent(Event event) {
    // TODO Auto-generated method stub
    
  }
}
