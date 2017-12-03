import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class Client extends JFrame {

	private class Chat implements Runnable {

		private int clientID;

		public Chat(int i) {
			this.clientID = i;
		}

		@Override
		public void run() {
			try {
				while (true) {
					String str = (String) inputFromOther.get(clientID).readObject();
					String[] strs = str.split("/");
					if (strs[0].equals("disconnect")) {
						System.out.println((clientID == Integer.parseInt(strs[1])));
						outputToOther.remove(clientID);
						inputFromOther.remove(clientID);
						other.remove(clientID);
						chatArea.append("Client " + strs[1] + " disconnected\n");
						break;
					} else {
						System.out.println("Client " + strs[1] + " said: " + strs[0]);
						chatArea.append("Client " + strs[1] + " said: " + strs[0] + "\n");
					}
					// replyNum = Integer.parseInt(strs[1]);
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
				while (true) {
					System.out.println("may wait for connection");
					other.add(ss.accept());
					System.out.println("accepted");
//					canTalk = true;
					outputToOther.add(new ObjectOutputStream(other.get(other.size() - 1).getOutputStream()));
					inputFromOther.add(new ObjectInputStream(other.get(other.size() - 1).getInputStream()));

					int i = (int) inputFromOther.get(other.size() - 1).readObject();

					chatArea.append("connection from Client " + i + "\n");
					// outputToOther.writeObject(canTalk);
					System.out.println("start");
					Thread talk = new Thread(new Chat(other.size() - 1));
					talk.start();
				}
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private class ButtonListener implements ActionListener {

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			String button = ((JButton) e.getSource()).getText();
			Socket connectClient = null;
			try {
				if (button.equals(requestButton.getText())) {
					outputToServer.writeObject("request");

					Vector<Integer> availableClients = (Vector<Integer>) inputFromServer.readObject();
					System.out.println("available clients: ");
					clients.setText("");
					clients.append("available clients: \n");
					for (Integer i : availableClients) {
						System.out.println("-- " + i);
						clients.append("Client " + (i - 1) + " Port " + i + "\n");
					}

					if (otherFrame == null) {
						otherFrame = new JFrame();
						otherFrame.add(clients);
						otherFrame.setSize(500, 400);
						otherFrame.setVisible(true);
						otherFrame.setLocation(getLocation());
					} else {
						otherFrame.setLocation(getLocation());
						otherFrame.setVisible(true);
					}
				} else if (button.equals(connectButton.getText())) {
					System.out.println("enter a port");
					port = Integer.parseInt(portText.getText());
					outputToServer.writeObject("connect " + port);
					String host = (String) inputFromServer.readObject();
					connectClient = new Socket(host, port);
					outputToOther.add(new ObjectOutputStream(connectClient.getOutputStream()));
					inputFromOther.add(new ObjectInputStream(connectClient.getInputStream()));
					other.add(connectClient);
					outputToOther.get(0).writeObject(id);
					// canTalk = (boolean) inputFromOther.get(inputFromOther.size() -
					// 1).readObject();
					System.out.println("start");
					chatArea.append("connected to Client " + (port - 1) + "\n");
					Thread talk = new Thread(new Chat(other.size() - 1));
					talk.start();
				} else if (button.equals(sendButton.getText())) {
					System.out.println("say something");
					String str1 = chatField.getText();
					// outputToOther.reset();
					// write who it is from so reply is possible
					outputToOther.get(0).writeObject(str1 + "/" + id);
				} else if (button.equals(disconnectButton.getText())) {
					chatArea.append("disconnected to Client " + (port - 1) + "\n");
					outputToOther.get(0).writeObject("disconnect/" + id);
					outputToOther.remove(0);
					inputFromOther.remove(0);
					other.remove(0);
				}
			} catch (IOException | ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	// output stream to server
	private ObjectOutputStream outputToServer;
	private ObjectInputStream inputFromServer;
	private Vector<Socket> other;
	private Vector<ObjectOutputStream> outputToOther = null;
	private Vector<ObjectInputStream> inputFromOther = null;
	
	// possibly used for group chatting
	private Vector<Socket> group;
	private Vector<ObjectOutputStream> outputToGroup = null;
	private Vector<ObjectInputStream> inputFromGroup = null;
	
//	private boolean canTalk = false;
	// my id
	private int id;
//	private int replyNum = 0;

	// Buttons
	private JButton requestButton = new JButton("Request");
	private JButton connectButton = new JButton("Connect");
//	private JButton talkButton = new JButton("Talk");
	private JButton groupButton = new JButton("Group");
	private JButton disconnectButton = new JButton("Disconnect");

	private JFrame otherFrame = null;
	private JTextArea clients = new JTextArea();
	private JLabel portLabel = new JLabel("Enter the port");
	private JTextField portText = new JTextField();
	private JButton enterButton = new JButton("Enter");
	private JButton sendButton = new JButton("send");
	private JTextArea chatArea = new JTextArea();
	private JTextField chatField = new JTextField();

	private int port;
	private static String serverIP;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Must have 1 argument as server ip!");
			System.exit(1);
		}
		serverIP = args[0];
		new Client();
	}

	public Client() {
		connectToServer();

		this.setTitle("Client " + id);
		this.setSize(500, 500);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		addButtons();

		// enterCommands();
	}

	private void addButtons() {
		portText.setEditable(true);
		portText.setSize(200, 100);
		JPanel northPane = new JPanel(new GridLayout(3, 1));
		northPane.add(portLabel);
		northPane.add(portText);
		JPanel north3Pane = new JPanel(new GridLayout(1, 3));
		north3Pane.add(connectButton);
		north3Pane.add(requestButton);
		disconnectButton.addActionListener(new ButtonListener());
		north3Pane.add(disconnectButton);
		northPane.add(north3Pane);
		this.add(northPane, BorderLayout.NORTH);
		enterButton.addActionListener(new ButtonListener());
		JPanel southPane = new JPanel(new GridLayout(2, 1));
		JPanel northSouthPane = new JPanel();
		sendButton.addActionListener(new ButtonListener());

		chatField.setEditable(true);
		northSouthPane.setLayout(new GridLayout(1, 2));
		northSouthPane.add(chatField);
		northSouthPane.add(sendButton);
		southPane.add(northSouthPane);
		southPane.add(groupButton);
		requestButton.addActionListener(new ButtonListener());
		this.add(southPane, BorderLayout.SOUTH);
		connectButton.addActionListener(new ButtonListener());
		JScrollPane jsp = new JScrollPane(chatArea);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		chatArea.setEditable(false);
		this.add(jsp, BorderLayout.CENTER);
	}

	// @SuppressWarnings("unchecked")
	// private void enterCommands() {
	// Socket connectClient = null;
	// Scanner s = new Scanner(System.in);
	// try {
	// while (s.hasNextLine()) {
	// String command = s.nextLine();
	//
	// // request people that can connect
	// if (command.equals("request")) {
	// // request for available ports
	// outputToServer.writeObject(command);
	// Vector<Integer> availableClients = (Vector<Integer>)
	// inputFromServer.readObject();
	// System.out.println("available client ports: ");
	// for (Integer i : availableClients) {
	// System.out.println("-- " + i);
	// }
	// }
	// // request for connection on the person
	// else if (command.equals("connect")) {
	// System.out.println("enter a port");
	// int num = s.nextInt();
	// outputToServer.writeObject("connect " + num);
	// String host = (String) inputFromServer.readObject();
	// connectClient = new Socket(host, num);
	// outputToOther.add(new ObjectOutputStream(connectClient.getOutputStream()));
	// inputFromOther.add(new ObjectInputStream(connectClient.getInputStream()));
	// other.add(connectClient);
	// // canTalk = (boolean) inputFromOther.get(inputFromOther.size() -
	// // 1).readObject();
	// System.out.println("start");
	// Thread talk = new Thread(new Chat(other.size() - 1));
	// talk.start();
	// }
	// // talk to the person
	// else if (command.equals("talk")) {
	// System.out.println("say something");
	// String str1 = s.nextLine();
	// // outputToOther.reset();
	// // write who it is from so reply is possible
	// outputToOther.get(0).writeObject(str1 + "/" + id);
	// // outputToOther.flush();
	// } else {
	// System.out.println("try a valid command");
	// continue;
	// }
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	private void connectToServer() {
		Socket myConnect = null;
		other = new Vector<Socket>();
		outputToOther = new Vector<ObjectOutputStream>();
		inputFromOther = new Vector<ObjectInputStream>();
		
		group = new Vector<Socket>();
		outputToGroup = new Vector<ObjectOutputStream>();
		inputFromGroup = new Vector<ObjectInputStream>();

		try {
			myConnect = new Socket(serverIP, 9999);

			// outputToServer is used to write PaintObjects over to server
			// after the second mouse click.
			outputToServer = new ObjectOutputStream(myConnect.getOutputStream());

			inputFromServer = new ObjectInputStream(myConnect.getInputStream());

			outputToServer.writeObject("set");

			int port = (int) inputFromServer.readObject();
			id = port - 1;

			Thread listen = new Thread(new PortListen(port));
			listen.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
