package cs455.overlay.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import cs455.overlay.wireformats.TaskSummaryResponse;

/**
 * Holds the information that pertains to tracking communications
 * between nodes.
 * 
 * @author stock
 *
 */
public class StatisticsCollectorAndDisplay {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  public AtomicInteger sendTracker;

  public AtomicInteger receiveTracker;

  public AtomicInteger relayTracker;

  public AtomicLong sendSummation;

  public AtomicLong receiveSummation;

  /**
   * Default constructor - initialize the atomic variables with an
   * initial value of 0.
   */
  public StatisticsCollectorAndDisplay() {
    this.sendTracker = new AtomicInteger( 0 );
    this.receiveTracker = new AtomicInteger( 0 );
    this.relayTracker = new AtomicInteger( 0 );
    this.sendSummation = new AtomicLong( 0 );
    this.receiveSummation = new AtomicLong( 0 );
  }

  /**
   * Display the results of the statistics summary provided from the
   * registry. This is a list of task summary responses from the
   * individual messaging nodes.
   * 
   * @param statisticsSummary
   */
  public void display(List<TaskSummaryResponse> statisticsSummary) {
    if ( statisticsSummary.size() == 0 )
    {
      LOG.error( "Unable to display statistics" );
      return;
    }
    int totalSent = 0;
    int totalReceived = 0;
    long totalSentSummation = 0;
    long totalReceivedSummation = 0;

    System.out.println(
        String.format( "\n%1$20s %2$12s %3$10s %4$15s %5$15s %6$10s", "",
            "Sent", "Received", "Sigma Sent", "Sigma Received", "Relayed" ) );
    for ( TaskSummaryResponse summary : statisticsSummary )
    {
      System.out.println( summary.toString() );
      totalSent += summary.getSendTracker();
      totalReceived += summary.getReceiveTracker();
      totalSentSummation += summary.getSendSummation();
      totalReceivedSummation += summary.getReceiveSummation();
    }
    System.out.println( String.format( "%1$20s %2$12s %3$10s %4$15s %5$15s\n",
        "Total Sum:", Integer.toString( totalSent ),
        Integer.toString( totalReceived ), Long.toString( totalSentSummation ),
        Long.toString( totalReceivedSummation ) ) );
  }

  /**
   * Reset the already initialized statistics to zero. This should be
   * done after each new <code>start N</code> provided by the registry.
   */
  public void reset() {
    this.sendTracker.set( 0 );
    this.receiveTracker.set( 0 );
    this.relayTracker.set( 0 );
    this.sendSummation.set( 0 );
    this.receiveSummation.set( 0 );
  }
}
