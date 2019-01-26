package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Register message type to initialize messaging node 
 * with the registry.
 * 
 * NOTE: Use ToStringBuilder.reflectionToString(object) to
 * convert class to a <code>String</code>.
 * 
 * @author stock
 *
 */
public class Register implements Event {

  private int type;
  
  private String ipAddress;
  
  private int port;

  /**
   * Default constructor - Unmarshall the <code>byte[]</code> to the
   * respective class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public Register(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(marshalledBytes);
    DataInputStream din = new DataInputStream(new BufferedInputStream(inputStream));
    
    this.type = din.readInt();
    
    byte[] ipBytes = new byte[din.readInt()];
    din.readFully(ipBytes);
    
    this.ipAddress = ipBytes.toString();
    
    this.port = din.readInt();
    
    inputStream.close();
    din.close();
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
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(outputStream));
    
    dout.writeInt(type);
    
    byte[] ipBytes = ipAddress.getBytes();
    dout.writeInt(ipBytes.length);
    dout.write(ipBytes);
    
    dout.writeInt(port);
    
    dout.flush();
    marshalledBytes = outputStream.toByteArray();
    
    outputStream.close();
    dout.close();
    return marshalledBytes;
  }
  
}
