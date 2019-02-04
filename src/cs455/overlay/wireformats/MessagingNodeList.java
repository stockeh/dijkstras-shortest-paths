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

/**
 * Messaging Node List message type to provide a peer-list to the
 * connected nodes for setting up the overlay.
 * 
 * @author stock
 *
 */
public class MessagingNodeList implements Event {

  private int type;

  private int numPeers;

  /**
   * host:port, host:port, ...
   */
  private List<String> peers;

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

    this.numPeers = din.readInt();

    this.peers = new ArrayList<String>( this.numPeers );
    for ( int i = 0; i < this.numPeers; ++i )
    {
      int len = din.readInt();
      byte[] bytes = new byte[len];
      din.readFully( bytes );
      this.peers.add( new String( bytes ) );
    }

    inputStream.close();
    din.close();
  }

  /**
   * Default constructor - create a new messaging node list message.
   * 
   * @param type
   * @param numPeers
   * @param peers
   */
  public MessagingNodeList(int type, int numPeers, List<String> peers) {
    this.type = type;
    this.numPeers = numPeers;
    this.peers = peers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    return type;
  }

  /**
   * 
   * @return The peers for this given node. May be empty or uninitialized.
   */
  public List<String> getPeers() {
    return peers;
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

    dout.writeInt( numPeers );

    for ( String item : peers )
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
        + Integer.toString( this.numPeers ) + " "
        + String.join( ", ", peers );
  }

}
