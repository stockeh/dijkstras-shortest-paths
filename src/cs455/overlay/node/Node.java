package cs455.overlay.node;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;

/**
 * Interface for the MessagingNode and Registry, so underlying
 * communication is indistinguishable, i.e., Nodes send messages to
 * Nodes.
 * 
 * @author stock
 *
 */
public interface Node {

  /**
   * Handle events delivered by messages
   * 
   * @param event
   * @param connection
   */
  public void onEvent(Event event, TCPConnection connection);

}
