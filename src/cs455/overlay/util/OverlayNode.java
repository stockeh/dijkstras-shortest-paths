package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import cs455.overlay.transport.TCPConnection;

/**
 * Class to maintain a nodes properties while constructing the
 * topology for the networks overlay.
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
   * Default constructor - setup a new node to hold information about
   * the messaging node list
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
   * Fetch all the peers that one should connect to
   * 
   * @return Returns the list of peers
   */
  public List<String> getPeers() {
    return peers;
  }

  /**
   * 
   * @return The connection associated with this node.
   */
  public TCPConnection getConnection() {
    return connection;
  }

  /**
   * Self represents the host:port for this specific node instance
   * 
   * @return The host:port for this node
   */
  public String getSelf() {
    return self;
  }

  /**
   * 
   * @return The number of peers this object has
   */
  public int numPeers() {
    return this.peers.size();
  }

  /**
   * Check if a new connection ( link ) should be made between two nodes
   * by checking all of the neighbors incoming and outgoing.
   * 
   * @param item the item to search for
   * @return True if the there is a link, false otherwise
   */
  public boolean contains(String item) {
    return neighbors.contains( item );
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
   * Add a new connection ( link ) to one self and update the neighbors
   * for that node.
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
   * Convert this class, <code>OverlayNode</code> to a readable format
   */
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    peers.forEach( s -> stringBuilder.append( s + ", " ) );
    return self + ": " + stringBuilder.toString();
  }
}
