package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import cs455.overlay.transport.TCPConnection;

/**
 * 
 * @author stock
 *
 */
public class OverlayNode {

  private HashSet<String> neighbors;

  private List<String> peers;

  private String self;

  private TCPConnection connection;

  /**
   * 
   * @param connection
   * @param self
   */
  public OverlayNode(TCPConnection connection, String self) {
    this.peers = new ArrayList<>();
    this.neighbors = new HashSet<>();
    this.connection = connection;
    this.self = self;
    this.update( self );
  }

  /**
   * 
   * @return
   */
  public List<String> getPeers() {
    return peers;
  }

  /**
   * 
   * @param item
   * @return
   */
  public boolean contains(String item) {
    return neighbors.contains( item );
  }
  
  /**
   * 
   * @return
   */
  public TCPConnection getConnection() {
    return connection;
  }

  /**
   * Get the size of total connections from self, or from peers.
   * 
   * @return
   */
  public int size() {
    return neighbors.size() - 1;
  }

  /**
   * 
   * @param item
   */
  public void add(String item) {
    peers.add( item );
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

  /**
   * 
   */
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    peers.forEach( s -> stringBuilder.append( s + ", " ) );
    return self + ": " + stringBuilder.toString();
  }
}
