import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

	// a vector of output streams to update on all clients
	private Vector<ObjectOutputStream> clientOutputStreams;
	private Vector<Integer> clientPorts;
	private Request temp = null;
	
	public static void main(String[] args) {
		new Server();
	}

	public Server() {
		clientOutputStreams = new Vector<ObjectOutputStream>();
		clientPorts = new Vector<Integer>();
		
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSock = new ServerSocket(9999);

			while (true) {
				System.out.println("waiting for connection...");
				Socket clientSocket = serverSock.accept();
//				System.out.println("port: " + clientSocket.getPort());
//				System.out.println("Local port: " + clientSocket.getLocalPort());
//				System.out.println("inet Address: " + clientSocket.getInetAddress());

				ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);
				
				clientPorts.add(clientOutputStreams.size());
				
				System.out.println("Client: " + (clientOutputStreams.size() - 1));
				
				// use the existing paint objects to update the new client
				temp = new Request(clientSocket, clientPorts, clientOutputStreams, clientOutputStreams.size() - 1);

//				clientPorts = temp.getClientPorts();
				// start the new threads, calls run()
				Thread t = new Thread(temp);
				t.start();
				System.out.println("got a connection");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
}
