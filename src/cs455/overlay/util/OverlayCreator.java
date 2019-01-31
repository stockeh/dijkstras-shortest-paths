package cs455.overlay.util;

import java.io.IOException;
import java.util.ArrayList;
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
    if ( totalConnections == 0 )
    {
      throw new Exception( "There are no connections. Overlay not created." );
    }
    if ( (totalConnections * connectingEdges) % 2 == 1 || connectingEdges < 1)
    {
      throw new Exception(
          "Insufficient conditions for a K-regular graph of order N."
              + "The topological structure must have KN be even" );
    }
    if ( totalConnections < (connectingEdges + 1) )
    {
      throw new Exception(
          "Insufficient conditions for a K-regular graph of order N."
              + "The topological structure must satisfy N ≥ K+1" );
    }

    buildTopology( connections, connectingEdges, totalConnections );
    // establishConnections( connections );
  }

  /**
   * 
   * 
   * @param connections
   * @param connectingEdges
   * @param totalConnections
   */
  private void buildTopology(Map<String, TCPConnection> connections,
      int connectingEdges, int totalConnections) {

    String[] addresses = new String[totalConnections];
    OverlayNode[] topology = new OverlayNode[totalConnections];

    int index = 0;
    for ( Entry<String, TCPConnection> mapEntry : connections.entrySet() )
    {
      String address = mapEntry.getKey();
      addresses[index] = address;

      topology[index] = new OverlayNode( address );
      ++index;
      // May be able to add A<-B<-C<-D here
    }
    // Connecting the first nodes, i.e., A->B->C->D
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
      System.out.println( topology[i].toString() );
    }
  }

  /**
   * Used to send out message to the messaging nodes
   * 
   * @param connections
   */
  private void establishConnections(Map<String, TCPConnection> connections) {
    int numberPeers = connections.size();
    List<String> peerInfo = new ArrayList<>( numberPeers );

    for ( Entry<String, TCPConnection> mapEntry : connections.entrySet() )
    {
      peerInfo.add( (mapEntry.getKey() + " : " + mapEntry.getValue()) );
    }

    MessagingNodeList message = new MessagingNodeList(
        Protocol.MESSAGING_NODE_LIST, numberPeers, peerInfo );

    for ( Entry<String, TCPConnection> entry : connections.entrySet() )
    {
      try
      {
        entry.getValue().getTCPSenderThread().sendData( message.getBytes() );
      } catch ( IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
