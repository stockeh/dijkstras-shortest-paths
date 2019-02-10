package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Upon completion of sending messages, a node will inform the
 * registry of its task being complete.
 * 
 * @author stock
 *
 */
public class TaskComplete implements Event {

  private int type;

  private String host;

  private int port;

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public TaskComplete(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    int len = din.readInt();
    byte[] hostBytes = new byte[len];
    din.readFully( hostBytes );

    this.host = new String( hostBytes );

    this.port = din.readInt();

    inputStream.close();
    din.close();
  }

  /**
   * Default constructor - create a task complete object to inform the
   * registry
   * 
   * @param host
   * @param port
   */
  public TaskComplete(String host, int port) {
    this.type = Protocol.TASK_COMPLETE;
    this.host = host;
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

    byte[] hostBytes = host.getBytes();
    dout.writeInt( hostBytes.length );
    dout.write( hostBytes );

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
    return this.host + ":" + Integer.toString( this.port );
  }
}
