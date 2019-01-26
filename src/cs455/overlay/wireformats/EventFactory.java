package cs455.overlay.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;
import cs455.overlay.util.Logger;

/**
 * Singleton class in charge of creating objects,
 * i.e., messaging types, from reading the first
 * byte of a message.
 * 
 * @author stock
 *
 */
public class EventFactory {

  private final static Logger LOG = new Logger(true);
  
  private static EventFactory instance = null;

  /**
   * Default constructor - Exists only to defeat
   * instantiation.
   */
  private EventFactory() { }

  /**
   * Single instance ensures that singleton instances
   * are created only when needed.
   * 
   * @return Returns the instance for the class
   */
  public static final EventFactory getInstance() {
    if (instance == null) {
      instance = new EventFactory();
    }
    return instance;
  }
  
  /**
   * 
   * @param message
   * @return
   * @throws IOException 
   */
  public Event createEvent(byte[] marshalledBytes) throws IOException {
    
    switch (ByteBuffer.wrap(marshalledBytes).getInt())
    {
      case Protocol.REGISTER_REQUEST:
        return new Register(marshalledBytes);
    }
    LOG.error("ERROR: Could not create Event");
    return null;
  }
}
