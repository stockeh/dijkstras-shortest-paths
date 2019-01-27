package cs455.overlay.node;

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
   */
  public void onEvent(Event event);

}
