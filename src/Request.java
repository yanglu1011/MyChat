import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Vector;

public class Request implements Runnable {

	// the client that it is from
	private Socket clientSocket;
	private int clientID;
	/* Yang: is it necessary to output to everyone? */
	/* Yang: but vector is need to be thread safe */
	private Vector<ObjectOutputStream> clientOutputStreams;
	/* Yang: possibly used to make private connections */
	private Vector<Integer> clientPorts;
	// the input stream from clientSocket
	private ObjectInputStream reader;

	public Request(Socket clientSocket, Vector<Integer> clientPorts, Vector<ObjectOutputStream> clientOutputStreams,
			int clientID) {
		this.clientSocket = clientSocket;
		this.clientOutputStreams = clientOutputStreams;
		this.clientID = clientID;
		this.clientPorts = clientPorts;

		try {
			reader = new ObjectInputStream(this.clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("from client " + clientID);

				String str = (String) reader.readObject();

				System.out.println("Client: " + str);

				if (str.equals("request")) {
					clientOutputStreams.get(clientID).reset();
					clientOutputStreams.get(clientID).writeObject(clientPorts);
					clientOutputStreams.get(clientID).flush();
				} else if (str.equals("set")) {
					clientOutputStreams.get(clientID).reset();
					// must be clientID + 1 because port 0 doesn't work
					clientOutputStreams.get(clientID).writeObject(clientID + 1);
					clientOutputStreams.get(clientID).flush();
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("connection reset");
			e.printStackTrace();
		}
	}

	public Vector<Integer> getClientPorts() {
		return clientPorts;
	}

}