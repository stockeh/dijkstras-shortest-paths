package cs455.overlay.util;

/**
 * Class used to print <b>info</b> and <b>error</b> logs to the
 * console.
 * 
 * @author stock
 *
 */
public class Logger {

  /**
   * Configure the master output for info and error logs
   */
  private final boolean MASTER = true;

  /**
   * Object specific configurable
   */
  private boolean DEBUG;

  private boolean INFO;

  /**
   * Default constructor - Initialize a new Logger for a give class. Use
   * as a private final static object in the calling class.
   * 
   * @param INFO
   * @param DEBUG
   */
  public Logger(boolean INFO, boolean DEBUG) {
    this.INFO = INFO;
    this.DEBUG = DEBUG;
  }

  /**
   * Retrieve the details for the log.
   * 
   * @return Return a <code>String</code> of the caller class in the
   *         format: <code>caller(method:line)</code>.
   */
  public String details() {
    try
    {
      String line = Integer.toString(
          Thread.currentThread().getStackTrace()[ 3 ].getLineNumber() );

      String method =
          Thread.currentThread().getStackTrace()[ 3 ].getMethodName();

      String caller =
          Thread.currentThread().getStackTrace()[ 3 ].getClassName();

      return caller + "(" + method + ":" + line + ") ";

    } catch ( ArrayIndexOutOfBoundsException e )
    {
      return "null ";
    }
  }

  /**
   * Display the message with details for the <b>'INFO'</b> type.
   * Configured by the global {@link Logger#MASTER} and
   * {@link Logger#INFO} variables.
   * 
   * @param message The message to display
   */
  public void info(String message) {
    if ( MASTER && INFO )
    {
      System.out.println( details() + "[INFO] - " + message );
    }
  }

  /**
   * Display the message with details for the <b>'DEBUG'</b> type.
   * Configured by the global {@link Logger#MASTER} and
   * {@link Logger#DEBUG} variables.
   * 
   * @param message The message to display
   */
  public void debug(String message) {
    if ( MASTER && DEBUG )
    {
      System.out.println( details() + "[DEBUG] - " + message );
    }
  }

  /**
   * Display the message with details for the <b>'ERROR'</b> type.
   * Configured by the global {@link Logger::MASTER} variable.
   * 
   * @param message The message to display
   */
  public void error(String message) {
    if ( MASTER )
    {
      System.out.println( details() + "[ERROR] - " + message );
    }
  }

}
