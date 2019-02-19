package cs455.overlay.wireformats;

/**
 * Interface defining the wireformats between messaging nodes and the
 * registry.
 *
 * @author stock
 *
 */
public interface Protocol {

  final int REGISTER_REQUEST = 0;

  final int REGISTER_RESPONSE = 1;

  final int DEREGISTER_REQUEST = 2;

  final int MESSAGING_NODE_LIST = 3;

  final int MESSAGE = 4;

  final int LINK_WEIGHTS = 5;

  final int TASK_INITIATE = 6;

  final int TASK_COMPLETE = 7;

  final int PULL_TRAFFIC_SUMMARY = 8;

  final int TRAFFIC_SUMMARY = 9;

  final byte SUCCESS = ( byte ) 200;

  final byte FAILURE = ( byte ) 500;

}
