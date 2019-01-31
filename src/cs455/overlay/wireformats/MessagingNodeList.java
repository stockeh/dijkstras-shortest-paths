package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cs455.overlay.util.Logger;

/**
 * Messaging Node List message type to provide a peer-list to the
 * connected nodes for setting up the overlay.
 * 
 * @author stock
 *
 */
public class MessagingNodeList implements Event {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private int type;

  private int numberPeers;

  private List<String> peerInfo;

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public MessagingNodeList(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    this.numberPeers = din.readInt();

    this.peerInfo = new ArrayList<String>( this.numberPeers );
    for ( int i = 0; i < this.numberPeers; ++i )
    {
      int len = din.readInt();
      byte[] bytes = new byte[len];
      din.readFully( bytes );
      LOG.error( new String( bytes ) );
      this.peerInfo.add( new String( bytes ) );
    }

    inputStream.close();
    din.close();
  }

  /**
   * Default constructor - create a new messaging node list message.
   * 
   * @param type
   * @param numberLinks
   * @param linkInfo
   */
  public MessagingNodeList(int type, int numberLinks, List<String> linkInfo) {
    this.type = type;
    this.numberPeers = numberLinks;
    this.peerInfo = linkInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getBytes() throws IOException {
    byte[] marshalledBytes = null;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DataOutputStream dout =
        new DataOutputStream( new BufferedOutputStream( outputStream ) );

    dout.writeInt( type );

    dout.writeInt( numberPeers );

    for ( String item : peerInfo )
    {
      byte[] bytes = item.getBytes();
      dout.writeInt( bytes.length );
      dout.write( bytes );
    }

    dout.flush();
    marshalledBytes = outputStream.toByteArray();

    outputStream.close();
    dout.close();
    return marshalledBytes;
  }

  @Override
  public String toString() {
    return "\n" + Integer.toString( this.type ) + " "
        + Integer.toString( this.numberPeers ) + " "
        + String.join( ",\n", peerInfo );
  }

}
