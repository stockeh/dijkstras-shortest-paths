package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;
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
  public void onEvent(Event event, Socket socket) throws IOException;

}
