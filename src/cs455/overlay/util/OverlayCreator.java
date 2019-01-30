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

  public void setupOverlay(Map<String, TCPConnection> connections) {
    if ( connections.size() == 0 )
    {
      LOG.info( "There are no connections. Overlay not created." );
      return;
    }
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
