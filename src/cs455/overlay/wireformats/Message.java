package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * A message that will be passed between nodes from a source to a
 * <i>sink</i>.
 * 
 * The message will be of the following format:
 * 
 * <ul>
 * <li>Message Type : ( this ) Message</li>
 * <li>Payload : ( negative ) 2147483648 to 2147483647</li>
 * <li>Position : index of connection in the routing path</li>
 * <li>Routing Path : array of connections, host:port, host:port,
 * etc.</li>
 * </ul>
 * 
 * @author stock
 *
 */
public class Message implements Event {

  private int type;

  private int payload;

  private int position;

  private String[] routingPath;

  /**
   * Default constructor - create a new message to send between nodes.
   * 
   * @param type
   * @param payload
   * @param position
   * @param routingPath
   */
  public Message(int type, int payload, int position, String[] routingPath) {
    this.type = type;
    this.payload = payload;
    this.position = position;
    this.routingPath = routingPath;
  }

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public Message(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    this.payload = din.readInt();

    this.position = din.readInt();

    int arrayLength = din.readInt();

    this.routingPath = new String[arrayLength];

    for ( int i = 0; i < arrayLength; ++i )
    {
      int len = din.readInt();
      byte[] bytes = new byte[len];
      din.readFully( bytes );
      this.routingPath[i] = (new String( bytes ));
    }

    inputStream.close();
    din.close();
  }

  /**
   * Increment the position for the next connection
   */
  public void incrementPosition() {
    ++position;
  }

  public int getPayload() {
    return payload;
  }

  public int getPosition() {
    return position;
  }

  public String[] getRoutingPath() {
    return routingPath;
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

    dout.writeInt( payload );

    dout.writeInt( position );

    dout.writeInt( routingPath.length );

    for ( String item : routingPath )
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
        + Integer.toString( this.payload ) + " "
        + Integer.toString( this.position ) + " "
        + Arrays.toString( routingPath );
  }

}
