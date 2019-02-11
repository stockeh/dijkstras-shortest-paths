package cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cs455.overlay.wireformats.LinkWeights;

/**
 * Compute the shortest path given the link weights to all other
 * connections.
 * 
 * This class will transform the link weights to a two-dimensional
 * graph where representing each connection as an index and the
 * weights as bidirectional values. Implementation of Dijkstra's
 * Algorithm was referenced from the GeeksforGeeks lecture on
 * <i>Dijkstra’s shortest path algorithm</i>. The routes from the
 * <i>source</i> node to all others in the network are appended to a
 * map of <code>routes</code> from the driving method.
 * 
 * @author stock
 *
 */
public class ShortestPath {

  private int[][] graph;

  private static final int NO_PARENT = -1;

  /**
   * Constructs the routes, from a given starting node, for the topology
   * using dijkstras shortest path algorithm. The
   * <code>Map<String, String[]> routes</code> object has an entry of
   * the form <b>sink : [host:port, host:port, ...]</b>, where the value
   * for the sink node defines the route from the parent self ( start ).
   * 
   * @param routes
   * @param linkWeights Defines the connection and weights between each
   *        link in the overlay.
   * @param self Identifier for the parent self in the form
   *        <b>host:port</b>. This will be the starting node to compute
   *        the <code>routes</code>.
   */
  public void buildShortestPath(Map<String, String[]> routes,
      LinkWeights linkWeights, String self) {
    String[] links = linkWeights.getLinks();
    List<String> transformer = new ArrayList<>();
    transformLinks( transformer, links );

    int numConnections = transformer.size();
    graph = new int[numConnections][numConnections];
    for ( String connection : links )
    {
      String[] splited = connection.split( "\\s+" );
      addEdge( transformer.indexOf( splited[0] ),
          transformer.indexOf( splited[1] ), Integer.parseInt( splited[2] ) );
    }
    int indexOfStart = transformer.indexOf( self );
    int[] parents = dijkstra( indexOfStart );

    for ( int current = 0; current < numConnections; ++current )
    {
      if ( current != indexOfStart )
      {
        List<String> addresses = new ArrayList<>();
        buildPath( indexOfStart, current, parents, transformer, addresses );
        routes.put( transformer.get( current ),
            addresses.toArray( new String[] {} ) );
      }
    }
  }

  /**
   * Transform array of weighted links to integer placeholder.
   * 
   * @param transformer The map that will maintain the transformed
   *        representations
   * @param links The weighted links between connections. As a list in
   *        the format <i>host:port host:port weight</i>
   */
  private void transformLinks(List<String> transformer, String[] links) {
    for ( String connection : links )
    {
      String[] splited = connection.split( "\\s+" );
      if ( !transformer.contains( splited[0] ) )
      {
        transformer.add( splited[0] );
      }
      if ( !transformer.contains( splited[1] ) )
      {
        transformer.add( splited[1] );
      }
    }
  }

  /**
   * Create the bidirectional graph from a source to destination with a
   * weight. These values will come from the weight list of connection
   * link weight attributes.
   * 
   * @param source
   * @param destination
   * @param weight
   */
  public void addEdge(int source, int destination, int weight) {
    graph[source][destination] = weight;
    graph[destination][source] = weight;
  }

  /**
   * Compute Dijkstra's Algorithm from the source node to every node.
   * This entails traversing the graph and dynamically update
   * unprocessed nodes.
   * 
   * @param source
   * @return The array of parent values from the start node to each
   *         node.
   */
  public int[] dijkstra(int source) {
    int numNodes = graph[0].length;

    // shortestDistances[i] will hold the shortest distance from src to i
    int[] shortestDistances = new int[numNodes];

    // added[i] will true if vertex i is included / in shortest path tree
    // or shortest distance from src to i is finalized
    boolean[] added = new boolean[numNodes];

    for ( int index = 0; index < numNodes; index++ )
    {
      shortestDistances[index] = Integer.MAX_VALUE;
      added[index] = false;
    }

    // Store the shortest path for each node
    int[] parents = new int[numNodes];
    parents[source] = NO_PARENT;

    shortestDistances[source] = 0;

    for ( int i = 1; i < numNodes; i++ )
    {
      // find the minimum distance node that has not been processed.
      int nearestVertex = -1;
      int distance = Integer.MAX_VALUE;
      for ( int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++ )
      {
        if ( !added[nodeIndex] && shortestDistances[nodeIndex] < distance )
        {
          nearestVertex = nodeIndex;
          distance = shortestDistances[nodeIndex];
        }
      }

      // Mark the picked vertex as processed
      added[nearestVertex] = true;

      // Update the distances for each connected node from the nearest.
      for ( int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++ )
      {
        int edgeDistance = graph[nearestVertex][nodeIndex];

        if ( edgeDistance > 0
            && ((distance + edgeDistance) < shortestDistances[nodeIndex]) )
        {
          parents[nodeIndex] = nearestVertex;
          shortestDistances[nodeIndex] = distance + edgeDistance;
        }
      }
    }
    return parents;
  }

  /**
   * Recursive function to build the path from the source to the
   * specified <code>current</code> value. This will call itself while
   * the specified <code>current</code> value is not the end. At this
   * point, the method will return and for every value that is not the
   * source will be converted from it's transformed index value to
   * actual identifier, i.e., host:port address.
   * 
   * @param source index of the nodes self
   * @param current index traversing the graph to the end
   * @param parents contains the list for the routes as an index
   * @param transformer contains a string of the address with respective
   *        indices for each node
   * @param addresses will be the value that is being added to as the
   *        recursive function returns.
   */
  private void buildPath(int source, int current, int[] parents,
      List<String> transformer, List<String> addresses) {
    if ( current == NO_PARENT )
    {
      return;
    }
    buildPath( source, parents[current], parents, transformer, addresses );
    if ( current != source )
    {
      addresses.add( transformer.get( current ) );
    }
  }
}
