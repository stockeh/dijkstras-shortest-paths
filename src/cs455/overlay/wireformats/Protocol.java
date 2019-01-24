package cs455.overlay.wireformats;

/**
 * Interface defining the wireformats between
 * messaging nodes and the registry.
 *
 * @author Jason Stock
 *
 */
interface Protocol {
  // MESSAGE TYPE
  final int REGISTER_REQUEST = 0;
  final int REGISTER_RESPONSE = 1;

  // STATUS TYPES
  final int SUCCESS = 200;
  final int FAILURE = 409;
}
