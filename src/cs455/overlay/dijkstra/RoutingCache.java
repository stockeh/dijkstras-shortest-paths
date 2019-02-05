package cs455.overlay.dijkstra;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.LinkWeights;

/**
 * 
 * @author stock
 *
 */
public class RoutingCache {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private Map<String, String[]> routes;

  private String self;

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
    routes.forEach( (k, v) -> System.out
        .println( "\nEND : " + k + " PATH : " + Arrays.toString( v ) ) );
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
