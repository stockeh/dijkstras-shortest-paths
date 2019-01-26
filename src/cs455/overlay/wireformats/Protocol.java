package cs455.overlay.wireformats;

/**
 * Interface defining the wireformats between
 * messaging nodes and the registry.
 *
 * @author Jason Stock
 *
 */
public interface Protocol {
  
  final int REGISTER_REQUEST = 0;
  
  final int REGISTER_RESPONSE = 1;
  
  final int DEREGISTER_REQUEST = 2;
  
  final int MESSAGING_NODE_LIST = 3;
  
  final int LINK_WEIGHTS = 4;
  
  final int TASK_INITIATE = 5;
  
  final int TASK_COMPLETE = 6;
  
  final int PULL_TRAFFIC_SUMMARY = 7;
  
  final int TRAFFIC_SUMMARY = 8;
  
}
