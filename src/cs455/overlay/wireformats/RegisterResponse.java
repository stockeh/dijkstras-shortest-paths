package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Register Response message type to respond to message node with the
 * status and information from the registry.
 * 
 * @author stock
 *
 */
public class RegisterResponse implements Event {

  private int type;

  private byte status;

  private String info;

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public RegisterResponse(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    this.status = din.readByte();

    int len = din.readInt();
    byte[] infoBytes = new byte[len];
    din.readFully( infoBytes, 0, len );

    this.info = new String( infoBytes );

    inputStream.close();
    din.close();
  }

  /**
   * Default constructor - create a new RegisterResponse message.
   * 
   * @param status
   * @param info
   */
  public RegisterResponse(byte status, String info) {
    this.type = Protocol.REGISTER_RESPONSE;
    this.status = status;
    this.info = info;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    // TODO Auto-generated method stub
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

    dout.writeByte( status );

    byte[] infoBytes = info.getBytes();
    dout.writeInt( infoBytes.length );
    dout.write( infoBytes );

    dout.flush();
    marshalledBytes = outputStream.toByteArray();

    outputStream.close();
    dout.close();
    return marshalledBytes;
  }

  /**
   * Display the information associated with the registration response
   * 
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return info;
  }

}
