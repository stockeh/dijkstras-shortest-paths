package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Register message type to initialize itself with another node.
 * 
 * This is a reusable class for registering, and deregistering
 * messaging nodes with the registry. As well as connecting messaging
 * nodes to other messaging nodes to construct the overlay.
 * 
 * @author stock
 *
 */
public class Register implements Event {

  private int type;

  private String ipAddress;

  private int port;

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public Register(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    int len = din.readInt();
    byte[] ipBytes = new byte[ len ];
    din.readFully( ipBytes );

    this.ipAddress = new String( ipBytes );

    this.port = din.readInt();

    inputStream.close();
    din.close();
  }

  /**
   * Default constructor - create a new register or deregister message.
   * 
   * @param type Specified for use of register or deregister message.
   * @param ipAddress
   * @param port
   */
  public Register(int type, String ipAddress, int port) {
    this.type = type;
    this.ipAddress = ipAddress;
    this.port = port;
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

    byte[] ipBytes = ipAddress.getBytes();
    dout.writeInt( ipBytes.length );
    dout.write( ipBytes );

    dout.writeInt( port );

    dout.flush();
    marshalledBytes = outputStream.toByteArray();

    outputStream.close();
    dout.close();
    return marshalledBytes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return Integer.toString( this.type ) + " " + this.getConnection();
  }

  /**
   * Converts the IP Address and Port to a readable format.
   * 
   * @return Returns a string in the format <code>host:port</code>
   */
  public String getConnection() {
    return this.ipAddress + ":" + Integer.toString( this.port );
  }
}
