package cs455.overlay.node;

import java.io.IOException;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;

/**
 * 
 * @author stock
 *
 */
public interface Node {

  /**
   * 
   * @param event
   * @param socket 
   * @throws IOException 
   */
  public void onEvent(Event event, TCPConnection connection) throws IOException;

}
