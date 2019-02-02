package cs455.overlay.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.MessagingNodeList;
import cs455.overlay.wireformats.Protocol;

public class OverlayCreator {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  /**
   * Start point for setting up the overlay for the network. Verifies
   * the network conditions are satisfied before attempting to create.
   * 
   * @param connections all the connections for the messaging nodes
   * @param connectingEdges how many bidirectional connections should be
   *        made between each node.
   * @throws Exception throws an exception if the network conditions are
   *         not met.
   */
  public void setupOverlay(Map<String, TCPConnection> connections,
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

    // Catch IOException ?
    disperseConnections( topology );
  }

  /**
   * 
   * 
   * @param connections
   * @param connectingEdges
   * @param totalConnections
   */
  private OverlayNode[] buildTopology(Map<String, TCPConnection> connections,
      int connectingEdges, int totalConnections) {

    String[] addresses = new String[totalConnections];
    OverlayNode[] topology = new OverlayNode[totalConnections];

    int index = 0;
    for ( Entry<String, TCPConnection> mapEntry : connections.entrySet() )
    {
      String address = mapEntry.getKey();
      addresses[index] = address;

      topology[index] = new OverlayNode( mapEntry.getValue(), address );

      ++index;
      // May be able to add A<-B<-C<-D here
    }
    for ( int node = 0; node < totalConnections - 1; node++ )
    {
      topology[node].add( addresses[node + 1] );
      topology[node + 1].update( addresses[node] );
    }

    for ( int node = 0; node < totalConnections; node++ )
    {
      for ( int peer = 0; peer < totalConnections; peer++ )
      {
        String address = addresses[peer];

        if ( !topology[node].contains( address )
            && topology[node].size() < connectingEdges
            && topology[peer].size() < connectingEdges )
        {
          topology[node].add( address );
          topology[peer].update( addresses[node] );
        }
      }
    }

    for ( int i = 0; i < totalConnections; i++ )
    {
      LOG.info( topology[i].toString() );
    }
    return topology;
  }

  /**
   * Used to send out message to the messaging nodes
   * 
   * @param connections
   * @throws IOException
   */
  private void disperseConnections(OverlayNode[] topology) throws IOException {
    for ( int i = 0; i < topology.length; i++ )
    {
      List<String> peers = topology[i].getPeers();
      int numPeers = peers.size();

      MessagingNodeList message = new MessagingNodeList(
          Protocol.MESSAGING_NODE_LIST, numPeers, peers );

      topology[i].getConnection().getTCPSenderThread()
          .sendData( message.getBytes() );
    }
  }
}
