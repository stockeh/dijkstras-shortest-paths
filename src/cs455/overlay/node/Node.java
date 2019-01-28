package cs455.overlay.node;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;

/**
 * 
 * @author stock
 *
 */
public interface Node {

  /**
   * Handle events delivered by messages
   * 
   * @param event
   * @param socket
   */
  public void onEvent(Event event, TCPConnection connection);

}
