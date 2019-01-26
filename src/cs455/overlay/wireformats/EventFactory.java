package cs455.overlay.wireformats;

/**
 * Singleton class in charge of creating objects,
 * i.e., messaging types, from reading the first
 * byte of a message.
 * 
 * @author stock
 *
 */
public class EventFactory {

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
}
