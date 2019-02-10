package cs455.overlay.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;
import cs455.overlay.util.Logger;

/**
 * Singleton class in charge of creating objects, i.e., messaging
 * types, from reading the first byte of a message.
 * 
 * @author stock
 *
 */
public class EventFactory {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private final static Logger LOG = new Logger( true, true );

  private static EventFactory instance = null;

  /**
   * Default constructor - Exists only to defeat instantiation.
   */
  private EventFactory() {}

  /**
   * Single instance ensures that singleton instances are created only
   * when needed.
   * 
   * @return Returns the instance for the class
   */
  public static final EventFactory getInstance() {
    if ( instance == null )
    {
      instance = new EventFactory();
    }
    return instance;
  }

  /**
   * Create a new event, i.e., wireformat object from the marshalled
   * bytes of said object.
   * 
   * @param message
   * @return the event object from the <code>byte[]</code>.
   * @throws IOException
   */
  public Event createEvent(byte[] marshalledBytes) throws IOException {

    switch ( ByteBuffer.wrap( marshalledBytes ).getInt() )
    {
      case Protocol.REGISTER_REQUEST :
        return new Register( marshalledBytes );

      case Protocol.REGISTER_RESPONSE :
        return new RegisterResponse( marshalledBytes );

      case Protocol.DEREGISTER_REQUEST :
        return new Register( marshalledBytes );

      case Protocol.MESSAGING_NODE_LIST :
        return new MessagingNodeList( marshalledBytes );

      case Protocol.LINK_WEIGHTS :
        return new LinkWeights( marshalledBytes );

      case Protocol.TASK_INITIATE :
        return new TaskInitiate( marshalledBytes );

      case Protocol.MESSAGE :
        return new Message( marshalledBytes );

      case Protocol.TASK_COMPLETE :
        return new TaskComplete( marshalledBytes );

      case Protocol.PULL_TRAFFIC_SUMMARY :
        return new TaskSummaryRequest( marshalledBytes );

      case Protocol.TRAFFIC_SUMMARY :
        return new TaskSummaryResponse( marshalledBytes );

      default :
        LOG.error( "Event could not be created. "
            + ByteBuffer.wrap( marshalledBytes ).getInt() );
        return null;
    }
  }
}
