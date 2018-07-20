package serverpackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Random;

public class BullyServer extends Thread {
	// final port number, where server listens for new connections
	private static final Integer port = 7478;
	// defines the server socket
	private static ServerSocket server;

	// server status tracking flag
	public static Integer serverFlag = 0;

	// temporary id assigned to the new connection on the network
	private static Integer tempId;

	// Object of ProcessThread class
	private static NodesConnection pt;

	// Map that stores the key-Value pairs of the nodes connected, with their
	// sockets
	public static HashMap<Integer, NodesConnection> threadMap = new HashMap<Integer, NodesConnection>();
	public static Integer coordinatorNode;

	public void startServer() {
		System.out.println("Starting bully server on port: " + port + "\n");
		// starts the server on the port and sets the server flag
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverFlag = 1;
		System.out.println("Server Started!\nWaiting for connection...\n");

		Random random = new Random();

		// runs indefinitely listening for new connections on the network
		while (true) {
			tempId = 100 + random.nextInt(1000);
			pt = null;
			try {
				// stores the new accepted connection in the process thread
				// object and saves them in the map
				pt = new NodesConnection(server.accept(), tempId);
				pt.start();
				threadMap.put(tempId, pt);
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
	}
	
	public void run(){
		System.out.println("HHD");
		startServer();
	}
}
