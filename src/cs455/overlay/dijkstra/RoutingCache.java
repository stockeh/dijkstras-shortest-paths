package cs455.overlay.dijkstra;

import java.util.HashMap;
import java.util.Map;
import cs455.overlay.wireformats.LinkWeights;

/**
 * 
 * @author stock
 *
 */
public class RoutingCache {

  private Map<String, String[]> routes;

  private String self;

  // List of total connections in the overlay
  private String[] connections;

  /**
   * Default constructor - create the routes from this instance to every
   * other node.
   * 
   * @param linkWeights weights distributed from the registry
   * @param self host:port of calling messaging node
   */
  public RoutingCache(LinkWeights linkWeights, String self) {
    this.self = self;
    this.routes = new HashMap<>();
    (new ShortestPath()).buildShortestPath( routes, linkWeights, self );
    this.connections = routes.keySet().toArray( new String[routes.size()] );
  }

  /**
   * Retrieve the route from the source to the <i>sink</i> node as an
   * array of type <code>String</code> where each item is the host:port
   * identifier of where to traverse to next.
   * 
   * @param sinkNode Location of the end point
   * @return A <code>String[]</code> from the given sinkNode
   */
  public String[] getRoute(String sinkNode)
      throws NullPointerException, ClassCastException {
    return routes.get( sinkNode );
  }

  /**
   * Provided an index, an identifier for another messaging node in the
   * network will be returned. This is constructed after the routes have
   * been built to every node.
   * 
   * @param index of the item to retrieve
   * @return A <code>String</code> of the identifier
   */
  public String getConnection(int index)
      throws ArrayIndexOutOfBoundsException, NullPointerException {
    return connections[index];
  }

  /**
   * Retrieve the total number of connections in the overlay
   * 
   * @return An integer of the total number of connections.
   */
  public int numConnection() throws NullPointerException {
    return connections.length;
  }

  /**
   * Each messaging node will hold its out routing cache. This method
   * will assist in formatting the <code>routes</code> computed. the
   * link weights are required to retrieve the original weights.
   * 
   * @param linkWeights Original connection links and weights provided
   *        by the registry.
   */
  public void printShortestPath(LinkWeights linkWeights) {
    System.out.println();
    routes.forEach( (k, v) ->
    {
      StringBuilder sb = new StringBuilder();
      String current = self;
      sb.append( current );
      for ( int i = 0; i < v.length; ++i )
      {
        String next = v[i];
        sb.append( linkWeights.getWeight( current, next ) );
        sb.append( next );
        current = next;
      }
      System.out.println( sb.toString() );
    } );
    System.out.println();
  }
}
