package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class OverlayNode {

  private HashSet<String> neighbors;

  private List<String> links;
  
  private String self;

  public OverlayNode(String self) {
    this.links = new ArrayList<>();
    this.neighbors = new HashSet<>();
    this.self = self;
    this.update( self );
  }

  public boolean contains(String item) {
    return neighbors.contains( item );
  }

  public int size() {
    return neighbors.size() - 1;
  }

  public void add(String item) {
    links.add( item );
    update( item );
  }
  
  /**
   * Add the item as a neighbor
   * 
   * @param item
   */
  public void update(String item) {
    neighbors.add( item );
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    links.forEach(s -> stringBuilder.append( s + ", " ));
    return self + ": " + stringBuilder.toString();
  }
}
