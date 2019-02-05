package cs455.overlay.dijkstra;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

  Map<String, String[]> routes;

  /**
   * Default constructor - create the routes from this instance to every
   * other node.
   * 
   * @param linkWeights weights distributed from the registry
   * @param self host:port of calling messaging node
   */
  public RoutingCache(LinkWeights linkWeights, String self) {
    routes = (new ShortestPath()).buildShortestPath( linkWeights, self );
    LOG.debug( Arrays.toString( routes.get( "A" ) ) );
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

  @Override
  public String toString() {
    return "";
  }
}
