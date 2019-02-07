package cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.LinkWeights;

/**
 * 
 * @author stock
 *
 */
public class ShortestPath {
  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private int[][] graph;

  private static final int NO_PARENT = -1;

  /**
   * USED FOR TESTING
   */
  public static void main(String[] args) {
    ShortestPath s = new ShortestPath();
    s.buildShortestPath( null, null, "0.0.0.0:63673" );
  }

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
   * 
   * 
   * @param startVertex
   * @return
   */
  public int[] dijkstra(int startVertex) {
    int nVertices = graph[0].length;

    // shortestDistances[i] will hold the shortest distance from src to i
    int[] shortestDistances = new int[nVertices];

    // added[i] will true if vertex i is included / in shortest path tree
    // or shortest distance from src to i is finalized
    boolean[] added = new boolean[nVertices];

    for ( int index = 0; index < nVertices; index++ )
    {
      shortestDistances[index] = Integer.MAX_VALUE;
      added[index] = false;
    }

    // Distance of source vertex from itself is always 0
    shortestDistances[startVertex] = 0;

    // Parent array to store shortest path tree
    int[] parents = new int[nVertices];

    // The starting vertex does not have a parent
    parents[startVertex] = NO_PARENT;

    // Find shortest path for all vertices
    for ( int i = 1; i < nVertices; i++ )
    {
      // Pick the minimum distance vertex from the set of vertices not yet
      // processed. nearestVertex is always equal to startNode in first
      // iteration.
      int nearestVertex = -1;
      int shortestDistance = Integer.MAX_VALUE;
      for ( int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++ )
      {
        if ( !added[vertexIndex]
            && shortestDistances[vertexIndex] < shortestDistance )
        {
          nearestVertex = vertexIndex;
          shortestDistance = shortestDistances[vertexIndex];
        }
      }

      // Mark the picked vertex as processed
      added[nearestVertex] = true;

      // Update dist value of the adjacent vertices of the picked vertex.
      for ( int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++ )
      {
        int edgeDistance = graph[nearestVertex][vertexIndex];

        if ( edgeDistance > 0 && ((shortestDistance
            + edgeDistance) < shortestDistances[vertexIndex]) )
        {
          parents[vertexIndex] = nearestVertex;
          shortestDistances[vertexIndex] = shortestDistance + edgeDistance;
        }
      }
    }

    // printSolution( startVertex, shortestDistances, parents );
    return parents;
  }

  /**
   * 
   * 
   * @param startVertex
   * @param currentVertex
   * @param parents
   * @param transformer
   * @param addresses
   */
  private void buildPath(int startVertex, int currentVertex, int[] parents,
      List<String> transformer, List<String> addresses) {
    if ( currentVertex == NO_PARENT )
    {
      return;
    }
    buildPath( startVertex, parents[currentVertex], parents, transformer,
        addresses );
    if ( currentVertex != startVertex )
    {
      addresses.add( transformer.get( currentVertex ) );
    }
  }

  /**
   * A utility function to print the constructed distances array and
   * shortest paths.
   * 
   * @param startVertex
   * @param distances
   * @param parents
   */
  private void printSolution(int startVertex, int[] distances, int[] parents) {
    int nVertices = distances.length;
    System.out.print( "Vertex\t\tDistance\tPath" );

    for ( int index = 0; index < nVertices; index++ )
    {
      if ( index != startVertex )
      {
        System.out.print( "\n" + startVertex + " -> " );
        System.out.print( index + " \t\t " );
        System.out.print( distances[index] + "\t\t" );
        printPath( startVertex, index, parents );
      }
    }
    System.out.println();
  }

  /**
   * Function to print shortest path from source to currentVertex using
   * parents array.
   * 
   * @param startVertex
   * @param currentVertex
   * @param parents
   */
  private void printPath(int startVertex, int currentVertex, int[] parents) {

    // Base case : Source node has been processed
    if ( currentVertex == NO_PARENT )
    {
      return;
    }
    printPath( startVertex, parents[currentVertex], parents );
    if ( currentVertex != startVertex )
    {
      System.out.print( currentVertex + " " );
    }
  }
}
