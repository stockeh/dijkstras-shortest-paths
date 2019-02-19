package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import cs455.overlay.util.OverlayNode;

/**
 * Defines the weights between connections for the network overlay.
 * 
 * In order to create the links between networks, it is expected that
 * the topology is created before creating an instance of this class.
 * This is done by registering new messaging nodes with the registry
 * and invoking the {@link OverlayCreator} class via the command line
 * at the registry.
 * 
 * @author stock
 *
 */
public class LinkWeights implements Event {

  private int type;

  private int numLinks;

  /**
   * <code>String</code> link format are of the form: <b>host:port
   * host:port weight</b>
   */
  private String[] links;

  private boolean weightsSent = false;

  /**
   * Default constructor - create a new link weight object with the
   * links between nodes
   * 
   * @param topology
   */
  public LinkWeights(OverlayNode[] topology) {
    this.type = Protocol.LINK_WEIGHTS;

    // Retrieve the number of links from the topology to allocate the
    // correct number of links in the overlay
    for ( OverlayNode node : topology )
    {
      this.numLinks += node.numPeers();
    }
    this.links = new String[ numLinks ];

    // Create all the link between each node in the overlay
    Random random = new Random();
    final int minWeight = 1;
    final int maxWeight = 10;

    int index = 0;
    for ( OverlayNode node : topology )
    {
      for ( String peer : node.getPeers() )
      {
        links[ index++ ] =
            node.getSelf() + " " + peer + " " + Integer.toString( random
                .ints( minWeight, ( maxWeight + 1 ) ).findFirst().getAsInt() );
      }
    }
  }

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public LinkWeights(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    this.numLinks = din.readInt();

    int arrayLength = din.readInt();

    this.links = new String[ arrayLength ];

    for ( int i = 0; i < arrayLength; ++i )
    {
      int len = din.readInt();
      byte[] bytes = new byte[ len ];
      din.readFully( bytes );
      this.links[ i ] = ( new String( bytes ) );
    }

    inputStream.close();
    din.close();
  }

  /**
   * Check if the weights have been sent to the overlay.
   * 
   * @return true if weights have been sent to the messaging nodes,
   *         false otherwise.
   */
  public boolean areWeightsSent() {
    return weightsSent;
  }

  /**
   * Update to reflect if the weights have been sent to the messaging
   * nodes.
   * 
   * @param weightsSent
   */
  public void setWeightsSent(boolean weightsSent) {
    this.weightsSent = weightsSent;
  }

  /**
   * {@link LinkWeights#links}
   * 
   * @return The links between connections is returned
   */
  public String[] getLinks() {
    return links;
  }

  public int getNumLinks() {
    return numLinks;
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

    dout.writeInt( numLinks );

    dout.writeInt( links.length );

    for ( String item : links )
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

  /**
   * Return a string of the connection links between messaging nodes.
   */
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    for ( String link : links )
    {
      stringBuilder.append( "\t" + link + "\n" );
    }
    String msg =
        "\nThere are " + Integer.toString( numLinks ) + " total links:\n\n";
    return msg + stringBuilder.toString();
  }

  /**
   * Retrieve the weight for a bidirectional link given some current and
   * next node. It is assumed that <code>current</code> and
   * <code>next</code> connections are valid in the links.
   * 
   * @param current
   * @param next
   * @return A string will be returned containing the weight for the
   *         that connection in the <code>String</code> format:
   *         <code>--##--</code>.
   */
  public String getWeight(String current, String next) {
    StringBuilder sb = new StringBuilder();

    for ( int i = 0; i < links.length; ++i )
    {
      String link = links[ i ];
      if ( link.contains( current ) && link.contains( next ) )
      {
        // retrieve the weight for that connection and pad a leading 0
        int weight = Integer.parseInt( link.split( "\\s+" )[ 2 ] );
        sb.append( "--" );
        sb.append( String.format( "%02d", weight ) );
        sb.append( "--" );
        break;
      }
    }

    return sb.toString();
  }
}
