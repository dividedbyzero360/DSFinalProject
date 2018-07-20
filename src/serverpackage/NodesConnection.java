package serverpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;

public class NodesConnection implements Runnable {
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

	public void run() {

	}

}
