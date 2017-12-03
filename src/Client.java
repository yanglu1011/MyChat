import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class Client {

	private class Chat implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					String str = (String) inputFromOther.readObject();
					System.out.println("Other said: " + str);
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private class PortListen implements Runnable {

		private int port;
		private ServerSocket ss;

		public PortListen(int port) {
			this.port = port;
			try {
				ss = new ServerSocket(this.port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				/**
				 * @Todo implement deny access possibly have to let server ask about the request
				 */
				System.out.println("may wait for connection");
				other = ss.accept();
				System.out.println("accepted");
				canTalk = true;
				outputToOther = new ObjectOutputStream(other.getOutputStream());
				inputFromOther = new ObjectInputStream(other.getInputStream());
				outputToOther.writeObject(canTalk);
				System.out.println("start");
				Thread talk = new Thread(new Chat());
				talk.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// output stream to server
	private ObjectOutputStream outputToServer;
	private Socket other;
	private ObjectOutputStream outputToOther = null;
	private ObjectInputStream inputFromOther = null;
	private boolean canTalk = false;

	public static void main(String[] args) {
		new Client();
	}

	public Client() {
		connectToServer();
	}

	@SuppressWarnings("unchecked")
	private void connectToServer() {
		Socket myConnect = null;

		try {
			myConnect = new Socket("192.168.2.7", 9999);

			// outputToServer is used to write PaintObjects over to server
			// after the second mouse click.
			outputToServer = new ObjectOutputStream(myConnect.getOutputStream());

			ObjectInputStream inputFromServer = new ObjectInputStream(myConnect.getInputStream());
			Scanner s = new Scanner(System.in);

			outputToServer.writeObject("set");

			int port = (int) inputFromServer.readObject();

			Thread listen = new Thread(new PortListen(port));
			listen.start();

			Socket connectClient = null;
			while (s.hasNextLine()) {
				String command = s.nextLine();

				// request connection
				if (command.equals("request")) {
					// request for available ports
					outputToServer.writeObject(command);
					Vector<Integer> availableClients = (Vector<Integer>) inputFromServer.readObject();
					System.out.println("available client ports: ");
					for (Integer i : availableClients) {
						System.out.println("-- " + i);
					}
				} else if (command.equals("connect")) {
					System.out.println("enter a port");
					int num = s.nextInt();
					outputToServer.writeObject("connect " + num);
					String host = (String) inputFromServer.readObject();
					connectClient = new Socket(host, num);
					outputToOther = new ObjectOutputStream(connectClient.getOutputStream());
					inputFromOther = new ObjectInputStream(connectClient.getInputStream());
					canTalk = (boolean) inputFromOther.readObject();
					System.out.println("start");
					Thread talk = new Thread(new Chat());
					talk.start();
				} else if (command.equals("talk")) {
					System.out.println("say something");
					String str1 = s.nextLine();
					// outputToOther.reset();
					outputToOther.writeObject(str1);
					// outputToOther.flush();
				} else {
					System.out.println("try a valid command");
					continue;
				}
			}

			// loop until client end itself

			// Socket me;
			// ObjectOutputStream outputToOther = new
			// ObjectOutputStream(otherConnect.getOutputStream());
			// ObjectInputStream inputFromOther = new
			// ObjectInputStream(otherConnect.getInputStream());

			// while (s.hasNextLine()) {
			//
			// }

			// while (s.hasNextLine()) {
			// String command = s.nextLine();
			//
			// if (command.equals("s")) {
			// System.out.println("who do you want to connect to?");
			// String str = s.nextLine();
			// outputToServer.writeObject(str);
			// int port = (int) inputFromServer.readObject();
			// ss = new ServerSocket(port);
			//
			// me = new Socket(InetAddress.getLocalHost(), port);
			// ss.accept();
			// outputToOther = new ObjectOutputStream(me.getOutputStream());
			// inputFromOther = new ObjectInputStream(me.getInputStream());
			//// oo = new ObjectOutputStream(((SocketChannel)
			// inputFromServer.readObject()).socket().getOutputStream());
			//// System.out.println("outputstream: " + oo.toString());
			// } else if (command.equals("c")) {
			// System.out.println("what do you want to say?");
			// String str = s.nextLine();
			// outputToOther.writeObject(str);
			//// oo.writeObject(str);
			// } else if (command.equals("q")){
			// int port = (int) inputFromServer.readObject();
			// ss = new ServerSocket(port);
			// me = new Socket("use to chat", port);
			// ss.accept();
			// outputToOther = new ObjectOutputStream(me.getOutputStream());
			// inputFromOther = new ObjectInputStream(me.getInputStream());
			// }
			// else {
			// String str = (String) inputFromOther.readObject();
			// System.out.println("from someone: " + str);
			// }
			//// if (oo)
			//
			// }

			// s.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
