package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import cs455.overlay.util.StatisticsCollectorAndDisplay;

/**
 * 
 * Response to the registry node of task statistics.
 * 
 * This message is delivered up receiving a TaskSummaryRequest.
 * 
 * @author stock
 *
 */
public class TaskSummaryResponse implements Event {

  private int type;

  private String host;

  private int port;

  private int sendTracker;

  private long sendSummation;

  private int receiveTracker;

  private long receiveSummation;

  private int relayTracker;

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public TaskSummaryResponse(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    int len = din.readInt();
    byte[] hostBytes = new byte[ len ];
    din.readFully( hostBytes );

    this.host = new String( hostBytes );

    this.port = din.readInt();
    this.sendTracker = din.readInt();
    this.sendSummation = din.readLong();
    this.receiveTracker = din.readInt();
    this.receiveSummation = din.readLong();
    this.relayTracker = din.readInt();

    inputStream.close();
    din.close();
  }

  /**
   * Default constructor - create a new task summary response object
   * with the respective populated fields.
   * 
   * @param host
   * @param port
   * @param stats holds a link to the statistics information for the
   *        messaging tasks
   */
  public TaskSummaryResponse(String host, int port,
      StatisticsCollectorAndDisplay stats) {
    this.type = Protocol.TRAFFIC_SUMMARY;
    this.host = host;
    this.port = port;
    this.sendTracker = stats.getSendTracker();
    this.sendSummation = stats.getSendSummation();
    this.receiveTracker = stats.getReceiveTracker();
    this.receiveSummation = stats.getReceiveSummation();
    this.relayTracker = stats.getRelayTracker();
  }

  public int getSendTracker() {
    return sendTracker;
  }

  public int getReceiveTracker() {
    return receiveTracker;
  }

  public long getSendSummation() {
    return sendSummation;
  }

  public long getReceiveSummation() {
    return receiveSummation;
  }

  public int getRelayTracker() {
    return relayTracker;
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
    dout.writeInt( sendTracker );
    dout.writeLong( sendSummation );
    dout.writeInt( receiveTracker );
    dout.writeLong( receiveSummation );
    dout.writeInt( relayTracker );

    dout.flush();
    marshalledBytes = outputStream.toByteArray();

    outputStream.close();
    dout.close();
    return marshalledBytes;
  }

  /**
   * Convert the statistics summary response to a readable text format.
   */
  public String toString() {
    return String.format( "%1$20s %2$12s %3$10s %4$15s %5$15s %6$10s",
        host + ":" + Integer.toString( port ), Integer.toString( sendTracker ),
        Integer.toString( receiveTracker ), Long.toString( sendSummation ),
        Long.toString( receiveSummation ), Integer.toString( relayTracker ) );
  }
}
