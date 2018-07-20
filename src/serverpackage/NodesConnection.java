package serverpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;


public class NodesConnection extends Thread {
	public static ReentrantLock lock = new ReentrantLock();
	// Socket for the connection
	private Socket processNode;

	// Node id as read from the user itself
	private String nodeId;

	// Stores the numeric node id of the node id sent by user
	private Integer node;

	// Input stream for the connection
	private BufferedReader is;

	// Output stream for the connection
	private PrintWriter os;

	// stores the messages received during the I/O operations, one at a time
	private String message;

	// Used to stores the tempId, generated for the new connection
	private Integer tempId;

	// coordinator flag for the node
	private Boolean coordinator = true;

	// Node disconnection tracker
	private Boolean nodeDisconnected;

	public NodesConnection(Socket processNode, Integer tempId) {
		this.processNode = processNode;
		this.tempId = tempId;

	}

	public synchronized Boolean getCoordinator() {
		return this.coordinator;
	}

	public synchronized void setCoordinator(Boolean flag) {
		this.coordinator = flag;
	}

	public synchronized Socket getSocket() {
		return this.processNode;
	}

	private synchronized void sendOkMessage(Integer sendOkTo) {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// get the I/O stream to the election call node
		NodesConnection pt = BullyServer.threadMap.get(sendOkTo);
		Socket demo = pt.getSocket();

		// writing the OK to the calling node
		try {
			PrintWriter pw = new PrintWriter(demo.getOutputStream(), true);
			pw.println("ok");
			pw.println("Message from [" + node + "] : OK");
			System.out.println("Message from [" + node + "] to [" + sendOkTo + "] : OK");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * This method intercepts the alive message from the coordinator, and
	 * broadcast it to all the active nodes in the network
	 */
	private synchronized void sendAliveMessage() {

		System.out.println("Node [" + node + "] : COORDINATOR ALIVE");
		System.out.println("Node [" + node + "] : COORDINATOR ALIVE");
		System.out.print("Broadcasting to:");

		// iterating the thread map to traverse all active connections
		for (Integer sendAliveTo : BullyServer.threadMap.keySet()) {

			if (sendAliveTo != node) {

				// extracting the socket connection information to the node
				NodesConnection pt = BullyServer.threadMap.get(sendAliveTo);
				Socket demo = pt.getSocket();

				// writing the alive message to the node
				try {
					PrintWriter pw = new PrintWriter(demo.getOutputStream(), true);
					pw.println("Message from [" + node + "] : COORDINATOR ALIVE");
					System.out.print(" [" + sendAliveTo + "]");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("\n");
	}

	/*
	 * get connection to all active nodes and Broadcasts the new coordinator
	 * message to all of active nodes
	 */
	private synchronized void broadcastCoordinatorMessage() {
		Integer newCoordinator = null;
		try {
			newCoordinator = Integer.parseInt(is.readLine());
			System.out.println("New coordinator: " + newCoordinator);
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		BullyServer.coordinatorNode = newCoordinator;

		// iterating the thread map to traverse all active connections
		for (Integer broadcastCoordinator : BullyServer.threadMap.keySet()) {

			// extracting the socket connection information to the node
			NodesConnection pt = BullyServer.threadMap.get(broadcastCoordinator);
			Socket demo = pt.getSocket();

			// writing the new coordinator message to the node
			try {
				PrintWriter pw = new PrintWriter(demo.getOutputStream(), true);
				pw.println("coordinator");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				pw.println(newCoordinator);
				pw.println("Message from [" + node + "] : COORDINATOR");
				pw.println(BullyServer.threadMap.size());
				System.out.println("[" + node + "] : COORDINATOR TO [" + broadcastCoordinator + "]\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/* gets invoked when a election request is received from a node to
	 * the server, which is them simply forwarded to all the
	 * active nodes
	 * 
	 * Invoked by startElection method
	 */
	public synchronized void broadcastElectionMessage() {

		System.out.println("");
		
		// iterate through the map, to get sockets, to the active nodes
		for(Integer sendElection: BullyServer.threadMap.keySet()) {
				//os.println("Message to [" + sendElection + "] : ELECTION!");
				
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// storing the socket information in temporary variable
				NodesConnection pt = BullyServer.threadMap.get(sendElection);
				Socket demo = pt.getSocket();
				
				// getting output stream, and writing to the node, one a t a time
				try {
					PrintWriter pw = new PrintWriter(demo.getOutputStream(), true);
					pw.println("election");
					pw.println(node);
					pw.println("Message from [" + node + "] : ELECTION");
					System.out.println("Message from [" + node + "] to [" + sendElection + "] : ELECTION");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		}
		
		// affirm the caller that election has been broadcasted
		os.println("electionBroadcasted");
	}
	/* This is the main block, that handles the initiating of an election,
	 * where it sends the election messages to all the active network
	 * nodes
	 */
	private synchronized void startElection() {
		
		/* sends the message back to caller, to affirm the initiating of an election
		 * and logs the messages accordingly
		 */
		os.println("Starting Election!");
		System.out.println("Node [" + node + "] : Starting Election!");
		
		// blocks the broadcasting to one node only,
		// i.e. when one process has a lock, only that that start
		// election. This is done to handle multiple multiple 
		// election requests received concurrently
		ReentrantLock lock = new ReentrantLock();
		synchronized (message) {
			lock.lock();
			
			/* this block opens output streams to the active nodes and send election message
			 * only to nodes higher than calling node
			 */
			broadcastElectionMessage();
			lock.unlock();
		}
		//System.out.println("Node [" + node + "] SEND ELECTION LOCK : " + lock.isHeldByCurrentThread());
	}

	public void run() {
		try {
			// Opens the socket I/O streams to the particular connected node
			is = new BufferedReader(new InputStreamReader(processNode.getInputStream()));
			os = new PrintWriter(processNode.getOutputStream(),true);
		} catch(IOException ie) {
			ie.printStackTrace();
		}
		
		try {
			
			// reads the first message i.e. the the nodeID which is send by the client
			message = is.readLine();
			nodeId = message;
			
			// sets the name of this thread to the nodeID received
			Thread.currentThread().setName(nodeId);
			
			// Updates the node key in the HashMap, which has a record for all active nodes
			synchronized (BullyServer.threadMap) {
				lock.lock();
				node = Integer.parseInt(nodeId);
				NodesConnection pt = BullyServer.threadMap.get(tempId);
				BullyServer.threadMap.remove(tempId);
				BullyServer.threadMap.put(node, pt);
				lock.unlock();	
			}

			// printing connection message to the node
			os.println("Network Connection Successful");
			System.out.println("Node [" + node + "] : Connected");
			System.out.println("Active Nodes: " + BullyServer.threadMap.keySet());
			
			/* This loop sets the server to always listen to the incoming node messages,
			 * intercepts them and takes the action accordingly.
			 */
			while(true) {
					message = is.readLine();
					System.out.println("Message: " + message);
					
					synchronized (message) {
						lock.lock();
						nodeDisconnected = false;

						if(message.equals("election")) {

							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							startElection();
							
						} else if(message.equals("alive") && BullyServer.coordinatorNode.equals(node) && BullyServer.serverFlag == 1) {

							sendAliveMessage();
								
							
						} else if(message.equals("sendOK")) {
							
							String sendOkToNode = is.readLine();
							sendOkMessage(Integer.parseInt(sendOkToNode));
							
							
						} else if(message.equals("coordinator")) {
							
							broadcastCoordinatorMessage();
							
						} else if(message.equals("exit")) {
							
							BullyServer.threadMap.remove(node);
							processNode.close();
							nodeDisconnected = true;
							
						}
						lock.unlock();
					}
					
					
					if(nodeDisconnected.equals(true)) {
						if(BullyServer.coordinatorNode.equals(node)) {
                         System.out.println("Here");
						}
		
						// checks if any active nodes in system, and displays the message as below
						if(BullyServer.threadMap.size() == 0) {
							System.out.println("Here1");
							
						}
						System.out.println("Node [" + node + "] : Disconnected\n");
						break;
					}
			}
		} catch (IOException e) {
			System.out.println("\nServer Down\n");
			//e.printStackTrace();
			
			/* The exception below occurs, when the server is waiting for a request from node
			 * and node abruptly closes, throwing a null pointer exception on read operation
			 */
		} catch(NullPointerException npe) {
			BullyServer.threadMap.remove(node);
			System.out.println("Node [" + node + "] : Crashed");
		}
	}

}
