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
    s.buildShortestPath( null, "0.0.0.0:63673" );
  }

  public Map<String, String[]> buildShortestPath(LinkWeights linkWeights,
      String self) {
    String[] links =
        { "0.0.0.0:64596 0.0.0.0:64598 2", "0.0.0.0:64596 0.0.0.0:64600 2",
            "0.0.0.0:64596 0.0.0.0:64605 9", "0.0.0.0:64598 0.0.0.0:64600 6",
            "0.0.0.0:64598 0.0.0.0:64604 10", "0.0.0.0:64600 0.0.0.0:64605 10",
            "0.0.0.0:64605 0.0.0.0:64604 4", "0.0.0.0:64604 0.0.0.0:64594 5" };
    // ---
    String start = "0.0.0.0:64596";
    // ---

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
    int indexOfStart = transformer.indexOf( start );
    int[] parents = dijkstra( indexOfStart );

    for ( int current = 1; current < numConnections; ++current )
    {
      System.out.println();
      int seperator = 0;
      buildPath( indexOfStart, current, parents, seperator );
    }
    return null;
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
   * weight.
   * 
   * @param source
   * @param destination
   * @param weight
   */
  public void addEdge(int source, int destination, int weight) {
    graph[source][destination] = weight;
    graph[destination][source] = weight;
  }

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

    printSolution( startVertex, shortestDistances, parents );
    return parents;
  }

  // A utility function to print the constructed distances array and
  // shortest paths
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

  // Function to print shortest path from source to currentVertex using
  // parents array
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

  private void buildPath(int startVertex, int currentVertex, int[] parents,
      int seperator) {
    if ( currentVertex == NO_PARENT )
    {
      return;
    }
    ++seperator;
    buildPath( startVertex, parents[currentVertex], parents, seperator );
    if ( currentVertex != startVertex )
    {
      System.out.print( currentVertex + " " );
    }
  }
}
