package cs455.overlay.node;

import cs455.overlay.wireformats.Event;

public interface Node {

  public void onEvent(Event event);
  
}
