package cs455.overlay.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodeList;

/**
 * The network topology and connections are established for the
 * overlay.
 * 
 * Each messaging node will get an event alluding to the topology
 * connections and link weights between said connections.
 * 
 * @author stock
 *
 */
public class OverlayCreator {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  /**
   * Start point for setting up the overlay for the network. Verifies
   * the network conditions are satisfied before attempting to create.
   * 
   * @param connections all the connections for the messaging nodes
   * @param connectingEdges how many bidirectional connections should be
   *        made between each node.
   * @return
   * @throws Exception throws an exception if the network conditions are
   *         not met.
   */
  public LinkWeights setupOverlay(Map<String, TCPConnection> connections,
      int connectingEdges) throws Exception {
    int totalConnections = connections.size();
    String insufficientError =
        "Insufficient conditions for a K-regular graph of order N. ";

    if ( totalConnections == 0 )
    {
      throw new Exception( "There are no connections. Overlay not created." );
    }
    if ( totalConnections > 2 && connectingEdges < 2 )
    {
      throw new Exception( insufficientError
          + "The topological structure must not make a partition, let K = 2" );
    }
    if ( (totalConnections * connectingEdges) % 2 == 1 || connectingEdges < 1 )
    {
      throw new Exception( insufficientError
          + "The topological structure must have KN be even" );
    }
    if ( totalConnections < (connectingEdges + 1) )
    {
      throw new Exception( insufficientError
          + "The topological structure must satisfy N ≥ K+1" );
    }

    OverlayNode[] topology =
        buildTopology( connections, connectingEdges, totalConnections );

    LinkWeights linkWeights = new LinkWeights( topology );

    disperseConnections( topology );

    return linkWeights;
  }

  /**
   * Construct the topology for the overlay for each connection. It is
   * important that the overlay is constructed following a k-regular
   * graph.
   * 
   * Let k = connectingEdges, n = number of connections, there exits
   * some integer m :
   * 
   * If k = 2m is even, put all the nodes around a circle, and join each
   * to its m nearest neighbors on either side. {@link joinNeighbors}
   * 
   * If k = 2m + 1 is odd, and n is even, put the nodes on a circle,
   * join each to its m nearest neighbors on each side, and also to the
   * node directly opposite. {@link joinOpposite}
   * 
   * @param connections the total messaging node connections
   * @param connectingEdges the number of edges ( links ) to have
   *        between each connection for a bidirectional graph
   * @param totalConnections the total number of messaging nodes
   */
  private OverlayNode[] buildTopology(Map<String, TCPConnection> connections,
      int connectingEdges, int totalConnections) {

    String[] addresses = new String[totalConnections];
    OverlayNode[] topology = new OverlayNode[totalConnections];

    int index = 0;
    for ( Entry<String, TCPConnection> entry : connections.entrySet() )
    {
      String address = entry.getKey();
      addresses[index] = address;
      topology[index++] = new OverlayNode( entry.getValue(), address );
    }
    // Default to creating a connecting ring between the nodes.
    for ( int node = 0; node < totalConnections; node++ )
    {
      topology[node].add( addresses[(node + 1) % totalConnections] );
    }
    joinNeighbors( addresses, topology, connectingEdges, totalConnections );

    if ( connectingEdges % 2 == 1 )
    {
      joinOpposite( addresses, topology, connectingEdges, totalConnections );
    }

    return topology;
  }

  /**
   * Join each node to its m nearest neighbors on either side.
   * 
   * @param addresses array of node identifiers
   * @param topology array of OverlayNodes that is being constructed
   * @param connectingEdges the number of edges ( links ) to have
   *        between each connection for a bidirectional graph
   * @param totalConnections the total number of messaging nodes
   */
  private void joinNeighbors(String[] addresses, OverlayNode[] topology,
      int connectingEdges, int totalConnections) {
    int spacing = 2;
    for ( int links = 1; links < connectingEdges / 2; links++ )
    {
      for ( int node = 0; node < totalConnections; node++ )
      {
        int forwardPeer = (node + spacing) % totalConnections;
        String forwardAddress = addresses[forwardPeer];

        if ( !topology[node].contains( forwardAddress ) )
        {
          topology[node].add( forwardAddress );
          topology[forwardPeer].update( addresses[node] );
        }
        int backwardPeer =
            (node + totalConnections - spacing) % totalConnections;
        String backwardAddress = addresses[backwardPeer];

        if ( !topology[node].contains( backwardAddress ) )
        {
          topology[node].add( backwardAddress );
          topology[backwardPeer].update( addresses[node] );
        }
      }
      ++spacing;
    }
  }

  /**
   * The number of connecting edges is odd, thus the node opposing each
   * will be connected.
   * 
   * @param addresses array of node identifiers
   * @param topology array of OverlayNodes that is being constructed
   * @param connectingEdges the number of edges ( links ) to have
   *        between each connection for a bidirectional graph
   * @param totalConnections the total number of messaging nodes
   */
  private void joinOpposite(String[] addresses, OverlayNode[] topology,
      int connectingEdges, int totalConnections) {
    int midpoint = totalConnections / 2;
    for ( int node = 0; node < totalConnections; node++ )
    {
      int peer = (node + midpoint) % totalConnections;
      String peerAddress = addresses[peer];
      if ( !topology[node].contains( peerAddress ) )
      {
        topology[node].add( peerAddress );
        topology[peer].update( addresses[node] );
      }
    }
  }

  /**
   * Used to send out message to the messaging nodes
   * 
   * @param topology the constructed topology as an array of
   *        <code>OverlayNode</code>'s.
   * @throws IOException
   */
  private void disperseConnections(OverlayNode[] topology)
      throws IOException, InterruptedException {
    for ( int i = 0; i < topology.length; i++ )
    {
      List<String> peers = topology[i].getPeers();
      int numPeers = peers.size();

      MessagingNodeList message = new MessagingNodeList( numPeers, peers );
      LOG.debug( message.toString() );
      topology[i].getConnection().getTCPSenderThread()
          .sendData( message.getBytes() );
    }
  }
}
