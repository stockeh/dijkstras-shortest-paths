————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
—														       —
—			   Using Dijkstra’s Shortest Paths to Route Packets in a Network Overlay		       —
—														       —
—														       —
—				      Jason D Stock - stock - 830635765 - Feb 14, 2019	    			       —
—														       —
————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————


This README.txt contains the following sections:

	- OVERVIEW

	- STARTUP

	- EXECUTION

	- NOTES

	- STRUCTURE


——————OVERVIEW——————

Gradle is used for build automation, and can be executing manually with 'gradle clean; gralde build'. The application
is constructed within a multi-layer package under 'cs455.overlay'. Thus, the build directory will be constructed with
the compiled class files under '/build/classes/java/main', and then the command-line arguments and the order in which
they should be specified for the Registry and the Messaging node can be run by:  

	java -cp . cs455.overlay.node.Registry registry-port
	java -cp . cs455.overlay.node.MessagingNode registry-host registry-port 

Once the Registry is started on the 'registry-host', multiple Messaging Nodes can be instantiated on multiple or
single machine. Each TCP connection will be mapped to a unique open port for robustness. 


——————STARTUP——————

To simplify the process of instantiating multiple instances, the provided run script can be used. Within this script,
it is possible to configure the 'registry-host' and 'registry-port' to start the Registry on. It is important that 
the Registry, and thus the startup script be executed on the defined host, with an associated open port.

	1. Open the 'run.sh' script, and modify the 'registry-host' and 'registry-port' of the Registry

		HOST=phoenix
		PORT=5001
	
	2. Add or remove desired Messaging Nodes to the application. Each machine should be on a new line,
	and can all be unique or the same

		vim machine_list

	3. Using the terminal, execute the run script to start the Registry

		./run.sh
	
	4. A new gnome-terminal, on the current host, will open and launch the Registry.

	5. Now, to spawn the Messaging Nodes, in the original terminal, execute the 'run.sh' script again.
	Another new gnome-terminal will open with a tab corresponding to that specific node instance.

		./run.sh 

At this point, there will be three terminals open; the original terminal, gnome-terminal for the Registry, and a 
gnome-terminal with tabs for each Messaging Node instance.


——————EXECUTION——————

From within the Registry, or any instance of the Messaging Node, the 'help' command can be used.
	
	Registry:
		
		list-messaging-nodes		: show the nodes connected with the overlay.

		setup-overlay k			: setup a k-regular graph of order N.

		list-weights			: display the link weights associated with the network topology.

		send-overlay-link-weights	: send the topology overlay weights to the connected nodes.

		start r				: notify the connected nodes to initialize r rounds of message sending.

	Messaging Nodes:

		print-shortest-path		: print shortest path from this node to all others.

		exit-overlay			: leave the overlay prior to starting.


——————NOTES——————

To allow all messages that are already in transit to reach their destination nodes, there is a 15 second delay after
receiving all TASK_COMPLETE messages on the Registry before issuing a PULL_TRAFFIC_SUMMARY message.

The Messaging Nodes will display a message after connecting to the Registry. Enter list-messaging-nodes to view
connections on the Registry

Once the overlay is configured on the Registry, the the Messaging Nodes will begin to connect to their associated
connections. The number of outgoing connections established per node is displayed - this number can very.


——————STRUCTURE——————

cs455.overlay.dijkstra: consists of routing cache, and classes too compute the shortest paths from the overlay.

	- RoutingCache.java

		Contains the <code>routes</code> for the each node that
		instantiates this class to avoid recomputing routes.
	 
		In order to get the routing cache for a node, the link weights need
		to be supplied to each of the nodes in the overlay. From here, the
		shortest bath can be built.
	
		@author stock

	- ShortestPath.java

		Compute the shortest path given the link weights to all other
		connections.

		This class will transform the link weights to a two-dimensional
		graph where representing each connection as an index and the
 		weights as bidirectional values.
 
		@author stock

cs455.overlay.node: fundamental classes for the Messaging Node, Registry, and Node classes.

	- MessagingNode.java

		Messaging nodes initiate and accept both communications and
		messages within the system.
 
		@author stock

	- Node.java

		Interface for the MessagingNode and Registry, so underlying
 		communication is indistinguishable, i.e., Nodes send messages to
		Nodes.
 
		@author stock

 	- Registry.java

		Maintains information about the registered messaging nodes.

		The registry is the brain of the network overlay. It is in charge
 		of registering / deregistering messaging nodes, setting up the
		overlay, sending schematics to the nodes, and starting the process
		of message sending. There will only be <b>one</> instance of the
 		registry in the network - this is tied to a specific port number
 		upon startup.

 		@author stock

cs455.overlay.transport: underlaying TCP structure for new connections, receiving, and sending threads.

  	- TCPConnection.java

		This class is used to establish a connection by starting a new
 		TCPSenderThread and TCPReceiverThread.
 		
 		@author stock

  	- TCPReceiverThread.java

		The TCP Receiving Thread to acknowledge new wireformat messages
		received on the specified connection.

 		The thread is blocked waiting to read an integer (the protocol for
		each message). This ensures the thread is not running unless there
		is something to be read.
		
		@author stock

  	- TCPSenderThread.java

		Class used to send data, via <code>byte[]</code> to the receiver.
 
 		Running as a thread, the TCPConnection holds an instance to the
 		sender for new messages. This makes use of a linked blocking queue
 		to buffer the rate at which messages are being sent.
 
		@author stock

  	- TCPServerThread.java

		A new TCP Server Thread is setup on the Registry and each new
		Messaging Node to accept new connections.
 
		Upon a new connection being made a TCP Connection is established on
		to send and receive messages as a response. The thread is blocked
		on the accept statement untill these new connections are established.
 
		@author stock

cs455.overlay.util: utility classes to assist the implementation across the network / application.

	- Logger.java

		Custom Logger class used to print <b>info</b> <b>debug</b> and
		<b>error</b> logs to the console. Calling location is displayed.

		@author stock

	- OverlayCreator.java

		The network topology and connections are established for the
 		overlay.
 
 		Each messaging node will get an event alluding to the topology
 		connections and link weights between said connections.
  
 		@author stock

	- OverlayNode.java

		Class to maintain a nodes properties while constructing the
		topology for the networks overlay.
 	 
		@author stock

	- StatisticsCollectorAndDisplay.java

		Holds the information that pertains to tracking communications
		between nodes.
	
		@author stock

cs455.overlay.wireformats: protocol defined for the various messages that are sent amongst the network.  

	- Event.java

		Public interface that each message will implement.
 	
		@author stock

	- EventFactory.java
		
		Singleton class in charge of creating objects, i.e., messaging
		types, from reading the first byte of a message.
 	
		@author stock

	- LinkWeights.java

		Defines the weights between connections for the network overlay.
	
		In order to create the links between networks, it is expected that
		the topology is created before creating an instance of this class.
		This is done by registering new messaging nodes with the registry
		and invoking the {@link OverlayCreator} class via the command line
		at the registry.

		@author stock

	- Message.java

		A message that will be passed between nodes from a source to a
		<i>sink</i>.
	
		The message will be of the following format:
		
		<ul>
		<li>Message Type : ( this ) Message</li>
		<li>Payload : ( negative ) 2147483648 to 2147483647</li>
		<li>Position : index of connection in the routing path</li>
		<li>Routing Path : array of connections, host:port, host:port,
		etc.</li>
		</ul>
	
		@author stock

	- MessagingNodeList.java

		Messaging Node List wireformat type is used to provide a peer-list
		to the connected nodes for setting up the overlay.
	
		@author stock

	- Protocol.java

		Interface defining the wireformats between messaging nodes and the
		registry.
	
		@author stock

	- Register.java

		Register message type to initialize itself with another node.

		This is a reusable class for registering, and deregistering
		messaging nodes with the registry. As well as connecting messaging
		nodes to other messaging nodes to construct the overlay.
	
		@author stock

	- RegisterResponse.java

		Register Response message type to respond to message node with the
		status and information from the registry.
	
		@author stock

	- TaskComplete.java

		Upon completion of sending messages, a node will inform the
		registry of its task being complete.
	
		@author stock

	- TaskInitiate.java
	
		The registry informs nodes in the overlay when they should start
		sending messages to each other. It does so via the TaskInitiate
		control message.
	
		@author stock

	- TaskSummaryRequest.java

		Upon receipt of all task complete messages to the registry a
		request for the task summary is delivered to each node in the
		system.
		 
		@author stock

	- TaskSummaryResponse.java

		Response to the registry node of task statistics.
		
		This message is delivered up receiving a TaskSummaryRequest.
	 
		@author stock

