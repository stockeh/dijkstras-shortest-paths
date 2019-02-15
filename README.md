# Distributed Computing
**Dijkstra's Shortest Paths to Route Packets in a Network Overlay**

The purpose of this application is to explore distributed development by constructing a logical overlay over a distributed set of nodes, and computing shortest paths using Dijkstra’s algorithm to route packets throughout the system.

The overlay is constructed with a single **Registry** that will manage an array of *N* **Messaging Nodes**.  Each messaging nodes will be connected to *k* other messaging nodes with bidirectional links.

Once the overlay has been setup, messaging nodes in the system will repeatedly select a random *sink node* to send a message too.  The overlay is constructed following a *k*-regular graph of order *N*, such that a source node will use the overlay for computation by forwarding packets to zero or more intermediary nodes.  This construction is specified by an arbitrary collection of link weights that are established between each link.  All communication between the messaging nodes and Registry are done over TCP, and implemented in full in the various Java packages.

![](animation.gif)

*NOTE:* the above GIF displays the execution of spawning a Registry, followed by six messaging nodes on a single local machine. The Registry displays the connections, creates the network overlay, sends the connection details, and then commands the messaging nodes to each send 1000 rounds of messages. Execution details are defined in the **Startup** section of this README.

## Components  
### Registry
There is exactly one Registry in the system which provides the following functions:  
* Allows messaging nodes to register themselves. This is performed when a messaging node starts up for the first time.  
* Allows messaging nodes to deregister themselves. This is performed when a messaging node leaves the overlay.  
* Enables the construction of the overlay by orchestrating connections that a messaging node initiates with other messaging nodes in the system. Based on its knowledge of the messaging nodes (through function A) the Registry informs messaging nodes about the other messaging nodes that they should connect to.  
* Assign and publish weights to the links connecting any two messaging nodes in the overlay. The weights these links take will range from 1-10.  
  
### Messaging Node 
Unlike the Registry, there are multiple messaging nodes in the system.  A messaging node provides two closely related functions; it initiates and accepts both communications and messages within the system.

Communications that nodes have with each other are based on TCP. Each messaging node is automatically configure to a ports over which it listens for communications.  

Once the initialization is complete, the node will send a registration request to the Registry.

### Package Structure
* *cs455.overlay.dijkstra*: consists of routing cache, and classes too compute the shortest paths from the overlay.
  * RoutingCache.java
  * ShortestPath.java
* *cs455.overlay.node*: fundamental classes for the Messaging Node, Registry, and Node classes.
  * MessagingNode.java
  * Node.java
  * Registry.java
* *cs455.overlay.transport*: underlaying TCP structure for new connections, receiving, and sending threads.
  * TCPConnection.java
  * TCPReceiverThread.java
  * TCPSenderThread.java
  * TCPServerThread.java
* *cs455.overlay.util*: utility classes to assist the implementation across the network / application
  * Logger.java
  * OverlayCreator.java
  * OverlayNode.java
StatisticsCollectorAndDisplay.java
* *cs455.overlay.wireformats*: protocol defined for the various messages that are sent amongst the network.  
  * Event.java
  * EventFactory.java
  * LinkWeights.java
  * Message.java
  * MessagingNodeList.java
  * Protocol.java
  * Register.java
  * RegisterResponse.java
  * TaskComplete.java
  * TaskInitiate.java
  * TaskSummaryRequest.java
  * TaskSummaryResponse.java

## Statistics  
Messages are sent between randomly chosen nodes for *R* rounds containing a specific payload.  Specifically, each packet will contain:  
* *message-type*
* *payload*: random integer with a range from 2147483647 to -2147483648 
* *position*: position of where, if not delivered, to send the packet to next
* *routing-path*: an array of the routing path for that specific packet

Every node maintains a count for the number of sent, received, and relayed messages.  As well as a continuous summation of the payload on the sending and receiving nodes.  These values are delivered to the Registry once all messages have been sent.  Once collected they are totaled and displayed to tracking patterns for the overlay - for example, an overlay containg of 12 nodes each running 2,000 rounds for a total of sending 10,000 had the following statistics:  

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

## Startup  
Gradle is used for build automation, and can be executing manually with ```gradle clean; gralde build```. The application is constructed within a multi-layer package under **cs455.overlay**. Thus, the build directory will be constructed with the compiled class files under `/build/classes/java/main`, and then the command-line arguments and the order in which they should be specified for the Registry and the Messaging node can be run by:  

* ```java cs455.overlay.node.Registry registry-port```
* ```java cs455.overlay.node.MessagingNode registry-host registry-port```  

Once the Registry is started on the `registry-host`, multiple Messaging Nodes can be instantiated on multiple or single machine. Each TCP connection will be mapped to a unique open port for robustness. To simplify the process of instantiating multiple instances, one of the provided run scripts can be used.  The `osx.sh` script is designed to be executed on MacOS, and the `run.sh` script is used for Linux (but configured to run in the lab at Colorado State University). Execution of the two scripts are nearly identical, but with subtle differences.

Within each of these scripts, it is possible to configure the **registry-host** and **registry-port** to start the Registry on.  It is important that the Registry, and thus the startup scripts be executed on the defined host, with an associated open port.

### MacOS
Open up a terminal in the working directory of the project;
```console
mars:dijkstras-shortest-paths$ ./osx.sh 
Project has 3357 total lines

BUILD SUCCESSFUL in 0s
1 actionable task: 1 executed

BUILD SUCCESSFUL in 0s
2 actionable tasks: 2 executed
cs455.overlay.node.Registry(main:80) [INFO] - Registry starting up at: Thu Feb 14 20:04:41 MST 2019
cs455.overlay.node.Registry(interact:102) [INFO] - Input a command to interact with processes.  Input 'help' for a list of commands.
```
This will spawn a new terminal, where the `osx.sh` script can be run again **once** to spawn *N* number of tabs where each Messaging Node is instantiated. An example seen from the originating tab is:

```console
mars:dijkstras-shortest-paths$ ./osx.sh 
cs455.overlay.node.MessagingNode(main:91) [INFO] - Messaging Node starting up at: Thu Feb 14 20:13:43 MST 2019
cs455.overlay.node.MessagingNode(registerNode:122) [INFO] - MessagingNode Identifier: Jasons-MacBook-Pro.local:50638
cs455.overlay.node.MessagingNode(interact:141) [INFO] - Input a command to interact with processes.  Input 'help' for a list of commands.
```
At this point, there will be two terminals open; one dedicated to the Registry, and another with tabs corresponding to each Messaging Node Instance. 

### Linux
Open up a terminal on the host specified the `run.sh` script.  From the working directory of the project;
```console
mars:dijkstras-shortest-paths$ ./run.sh 
Project has 3357 total lines

BUILD SUCCESSFUL in 0s
1 actionable task: 1 executed

BUILD SUCCESSFUL in 0s
2 actionable tasks: 2 executed
```
A new gnome-terminal, on the current host, will open and launch the Registry.  The following message will be shown:

```console
mars:dijkstras-shortest-paths$
cs455.overlay.node.Registry(main:80) [INFO] - Registry starting up at: Thu Feb 14 20:04:41 MST 2019
cs455.overlay.node.Registry(interact:102) [INFO] - Input a command to interact with processes.  Input 'help' for a list of commands.
```
Now, to spawn the Messaging Nodes, in the original terminal, execute the `run.sh` script again.  Another new gnome-terminal will open with a tab corresponding to that specific node instance. At this point, there will be three terminals open; the original terminal, gnome-terminal for the Registry, and a gnome-terminal with tabs for each Messaging Node instance.

## Interactions
Upon the Messaging Nodes launching, they will auto register themselves with the Registry.  It is now possible to configure the network overlay and begin sending messages between the nodes. The Registry and each Messaging Node will have a set of commands that can be executed in the foreground process.

### Registry
* *list-messaging-nodes*: this results in information about the messaging nodes (hostname, and port-number) being displayed.  
* *list-weights*: information about the links comprising the overlay are displayed, each representing a bidirectional connection and the weight for that link.  
* *setup-overlay k*: results in the registry setting up the overlay to produce a *k*-regular graph of order *N*, where *k* defines the number of bidirectional connections a node will have.  Ensuring that the topology created produces no partitions.  
* *send-overlay-link-weights*: a message is sent to all the registered nodes in the overlay with information about each connection and their link weights.  This allows all the nodes in the system to be aware of not just its immediate neighbors, but the complete set of links and nodes.  
* *start R*: results in nodes exchanging messages within the overlay.  Each node will send *R* rounds of messages to randomly chosen nodes (excluding itself).  The packet format is specified in the statistics section of this description.

### Messaging Node
* *print-shortest-path*: the shortest paths that have been computed using Dijkstra’s algorithm is displayed.  The listing indicates the path from the source to every node in the overlay with the respective link weights.  
* *exit-overlay*: allows a messaging node to exit the overlay.  This must occur prior to the overlay being constructed on the registry.  
