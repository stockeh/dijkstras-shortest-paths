# Distributed Computing
**Dijkstra's Shortest Paths to Route Packets in a Network Overlay**

The purpose of this application is to explore distributed development by constructing a logical overlay over a distributed set of nodes, and computing shortest paths using Dijkstra’s algorithm to route packets throughout the system.

The overlay is constructed with a single **registry** that will manage an array of *N* **messaging nodes**.  Each messaging nodes will be connected to *k* other messaging nodes with bidirectional links.

Once the overlay has been setup, messaging nodes in the system will repeatedly select a random *sink node* to send a message too.  The overlay is constructed following a *k*-regular graph of order *N*, such that a source node will use the overlay for computation by forwarding packets to zero or more intermediary nodes.  This construction is specified by an arbitrary collection of link weights that are established between each link.  All communication between the messaging nodes and registry are done over TCP, and implemented in full in the various Java packages.

## Components  
* **Registry**  
There is exactly one registry in the system. The registry provides the following functions:  
  * Allows messaging nodes to register themselves. This is performed when a messaging node starts up for the first time.  
  * Allows messaging nodes to deregister themselves. This is performed when a messaging node leaves the overlay.  
  * Enables the construction of the overlay by orchestrating connections that a messaging node initiates with other messaging nodes in the system. Based on its knowledge of the messaging nodes (through function A) the registry informs messaging nodes about the other messaging nodes that they should connect to.  
  * Assign and publish weights to the links connecting any two messaging nodes in the overlay. The weights these links take will range from 1-10.  
  
* **Messaging Node**  
Unlike the registry, there are multiple messaging nodes in the system.  A messaging node provides two closely related functions; it initiates and accepts both communications and messages within the system.  </br></br>Communications that nodes have with each other are based on TCP. Each messaging node is automatically configure to a ports over which it listens for communications.  </br></br>Once the initialization is complete, the node will send a registration request to the Registry.

* **Package Structure**
  * *cs455.overlay.dijkstra*: consists of a nodes routing cache, and classes to compute the shortest paths from the overlay.
  * *cs455.overlay.node*: fundamental classes for the Messaging Node, Registry, and Node classes.
  * *cs455.overlay.transport*: underlaying TCP transportation structure for new connections, receiving, and sending threads.
  * *cs455.overlay.util*: utility classes to assist the implementation across the network / application
  * *cs455.overlay.wireformats*: protocol defined for the various messages that are sent amongst the network.  
  
## Interacting with the Processes. 
**Registry**  
  * *list-messaging-nodes*: this results in information about the messaging nodes (hostname, and port-number) being displayed.  
  * *list-weights*: information about the links comprising the overlay are displayed, each representing a bidirectional connection and the weight for that link.  
  * *setup-overlay k*: results in the registry setting up the overlay to produce a *k*-regular graph of order *N*, where *k* defines the number of bidirectional connections a node will have.  Ensuring that the topology created produces no partitions.  
  * *send-overlay-link-weights*: a message is sent to all the registered nodes in the overlay with information about each connection and their link weights.  This allows all the nodes in the system to be aware of not just its immediate neighbors, but the complete set of links and nodes.  
  * *start R*: results in nodes exchanging messages within the overlay.  Each node will send *R* rounds of messages to randomly chosen nodes (excluding itself).  The packet format is specified in the statistics section of this description.

**Messaging Node**  
  * *print-shortest-path*: the shortest paths that have been computed using Dijkstra’s algorithm is displayed.  The listing indicates the path from the source to every node in the overlay with the respective link weights.  
  * *exit-overlay*: allows a messaging node to exit the overlay.  This must occur prior to the overlay being constructed on the registry.  

## Statistics  
Messages are sent between randomly chosen nodes for *R* rounds containing a specific payload.  Specifically, each packet will contain:  
* *message-type*
* *payload*: random integer with a range from 2147483647 to -2147483648 
* *position*: position of where, if not delivered, to send the packet to next
* *routing-path*: an array of the routing path for that specific packet

Every node maintains a count for the number of sent, received, and relayed messages.  As well as a continuous summation of the payload on the sending and receiving nodes.  These values are delivered to the Registry once all messages have been sent.  Once collected they are totaled and displayed to tracking patterns for the overlay - for example, an overlay containg of 12 nodes each sending 10,000 messages had the following statistics:  

|            	|  Sent  	| Received 	|   Sigma Sent  	| Sigma Received 	| Relayed 	|
|-----------:	|:------:	|:--------:	|:-------------:	|:--------------:	|:-------:	|
|     Node A 	|  10000 	|   9881   	|  37127559661  	|   -8950917082  	|  21830  	|
|     Node B 	|  10000 	|   9950   	|  214879893825 	|  -86804828953  	|  19829  	|
|     Node C 	|  10000 	|   10125  	| -107488485726 	|  143702823041  	|  21776  	|
|     Node D 	|  10000 	|   9999   	| -181623245082 	|   23304637639  	|  23639  	|
|     Node E 	|  10000 	|   9962   	|  29445926778  	|   30567107677  	|  25442  	|
|     Node F 	|  10000 	|   10150  	| -100173989947 	|  139106643017  	|  21856  	|
|     Node G 	|  10000 	|   9989   	|  65394751641  	|  -317034837897 	|  19853  	|
|     Node H 	|  10000 	|   10078  	|  41609441211  	|   95540882070  	|  25392  	|
|     Node I 	|  10000 	|   9986   	|  -4292666229  	|  -123008492962 	|  23635  	|
|     Node J 	|  10000 	|   9817   	|  -84206165698 	|  164292048662  	|  23729  	|
|     Node K 	|  10000 	|   10026  	|  150365968051 	|  -17279284539  	|  21816  	|
|     Node L 	|  10000 	|   10037  	|  -77571695101 	|  -59968487289  	|  23628  	|
| **total:** 	| 120000 	|  120000  	|  -16532706616 	|  -16532706616  	|         	|

## Executing  
Gradle is used for build automation, and can be executing manually with ```gradle clean; gralde build```. The application is constructed within a multi-layer package under **cs455.overlay**. Thus, the build directory will be constructed with the compiled class files under ```/build/classes/java/main```, and then the command-line arguments and the order in which they should be specified for the Messaging node and the Registry can be run by:  
* ```java cs455.overlay.node.Registry port```
* ```java cs455.overlay.node.MessagingNode registry-host registry-port```  

At this point, multiple Messaging Nodes can be instantiated on any or single machine. It may be easier to do this with one of the provided run scripts. The ___ script is designed to be executed on MacOS, and ___ is used and for Linux but is configured to run in the lab at Colorado State Univeristy.
