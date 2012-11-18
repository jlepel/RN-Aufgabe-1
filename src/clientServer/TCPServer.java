package clientServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import User.UserData;
import fileManager.MailReader;
import fileManager.TxtLogging;

public class TCPServer extends Thread {
	/* Server, der Verbindungsanfragen entgegennimmt */
	public static final int SERVER_PORT = 11000;
	private ManagerAbs manager;
	
	public TCPServer(ManagerAbs manager) {
		this.manager = manager;
	}

	public void run() {
		ServerSocket welcomeSocket; // TCP-Server-Socketklasse
		Socket connectionSocket; // TCP-Standard-Socketklasse
		
		int counter = 0; // Z�hlt die erzeugten Bearbeitungs-Threads

		// -------------------------------

		try {
			/* Server-Socket erzeugen */
			welcomeSocket = new ServerSocket(SERVER_PORT);
			welcomeSocket.setReuseAddress(true); //zum debuggen!!!!!
			
			while (true) { // Server laufen IMMER

				connectionSocket = welcomeSocket.accept();

				/* Neuen Arbeits-Thread erzeugen und den Socket �bergeben */
				(new TCPServerThread(++counter, connectionSocket, manager))
						.start();
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}

class TCPServerThread extends Thread {
	/*
	 * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
	 * erh�lt
	 */
	public TCPServerThread(int num, Socket sock, ManagerAbs manager) {
		this.name = num;
		this.socket = sock;
		this.manager = manager;
	}
	final TxtLogging log = new TxtLogging(true);
	private ManagerAbs manager;
	private int name;
	private Socket socket;
	private final UserData authenticUser = new UserData("mett@bla.de", "mett");
	private boolean loggedIn;
	boolean serviceRequested = true;

	private BufferedReader readIn;
	private DataOutputStream writeTo;



	public void run() {
		try {
			readIn = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writeTo = new DataOutputStream(socket.getOutputStream());
			writeToClient("+OK POP3 server ready");

			if (loggedIn == false) {
				userLogIn();
			}

			String reply = "";
			
			
			while (serviceRequested && loggedIn) {
				reply = readFromClient();
				
				
				
				//***********************************************************
				
				
				boolean oversize = false;
				int args = 0;

				if (reply.length() < 4) {
					writeToClient("-ERR no valid command");
					continue;
				} else {

					// Integer.parseInt("x"); geht nicht -> parseInt wirft exception
					// args ist immer int

					if (reply.length() >= 6) {
						oversize = true;
						args = Integer.parseInt(reply.substring(5));
						reply = reply.substring(0, 4);
					}
					reply = reply.toUpperCase();

					switch (reply) {
					
					case "STAT":
						//TODO: stat imme alle oder nur ohne die gelöschten?
						writeToClient("+OK " + manager.getSize() + " <" + manager.getAllOctets() + " octets>");
						break;
					
					case "QUIT":
						writeToClient("+OK POP3 Server singing off");
						manager.deleteAbs();
						serviceRequested = false;
						break;
					
					case "UIDL":
						// ohne Argumente
						if(oversize == false){
							writeToClient("+OK");
							for(int i = 1; i <= manager.getSize(); i++){
								if(manager.getElem(i) == null){
									continue;
								}
								writeToClient("" + i + manager.getElem(i));
							}
							writeToClient(".");
							break;
						}
						
						
						if (oversize == true && args != 0 && args <= manager.getSize()) {
							if(manager.getElem(args) == null){
								writeToClient("-ERR mail with sequence number " + args +  " was deleted earlier");
								break;
							} else {
							writeToClient("+OK " + args + " " + manager.getElem(args));
							break;
							}
						} else if(args == 0) {
							writeToClient("-ERR invalid sequence number: 0");
							break;
						}
							else{
							writeToClient("-ERR no such message, only "
										+ manager.getSize() + " messages in maildrop");
							break;
						}
								

					case "RETR": // kriegt args -> split
						if(args > 0 && manager.getSize()!= 0){
							if(manager.getElem(args) == null){
								writeToClient("-ERR mail with sequence number " + args +  " was deleted earlier");
								break;
							} 
							writeToClient("+OK");
							MailReader.printMailLine(manager.getElem(args), writeTo);	
						}
						else{
							writeToClient("-ERR 1 argument required");
						}
						
						break;
					
					case "DELE":
						if (args == 0) {
							writeToClient("-ERR no valid command");
						} else if (args > manager.getSize()) {
							writeToClient("-ERR no such message");
						} else if (manager.getElem(args) == null) { 
							writeToClient("-ERR message " + args + " already deleted");
						} else {
							manager.delete(args);
							writeToClient("+OK message " + args + " deleted");
						}
						break;

					case "NOOP":
						writeToClient("+OK");
						break;

					case "LIST":
						//ohne argumente
						
						List<Long> octets = manager.getOneOctet();

						if (args == 0) {
							writeToClient("+OK " + manager.getSize() + " messages " + " <" + manager.getAllOctets() + " octets>");
							
						
							
							for(int i = 1; i <= manager.getSize(); i++){
								if(manager.getElem(i) == null){
									continue;
								}
								
								if(octets.isEmpty()){
									writeToClient("+OK");
								}
								else{
								writeToClient("" + i + " " +  octets.get(i-1));
								}
							}

							writeToClient(".");
						//mit argument
						} else if (args > manager.getSize()) {
							writeToClient("-ERR no such message, only " + manager.getSize() + " messages in maildrop");
						} else if (manager.getElem(args) == null) { 
							writeToClient("-ERR message " + args + " already deleted");
						} else {
							// mit genau 1 arg: list <nummer der mail> (Integer) -> anzahl
							writeToClient("+OK " + args + " " + manager.getElem(args).length() + " <" + manager.getAllOctets() + " octets>");
							writeToClient(".");
						}
						

						break;
					case "RSET":
						// keine args
						// falls mail als gel�scht markiert, machs r�ckg�ngig
						manager.rset();
						writeToClient("+OK maildrop has" + " " + manager.getElem(args).length() + " <" + manager.getAllOctets() + " octets>");
						// -ERR nicht vorhanden nach rfc
						break;

					default:
						writeToClient("-ERR unknown command");
						break;
					}

				}
				
				//********************************************************

			}

			
			socket.close();
		} catch (IOException e) {
			System.err.println(e.toString());
			System.exit(1);
		}
	}

	private void userLogIn() throws IOException {

		boolean userLogIn = false;

		while (loggedIn == false) {

			String postfix = "";
			String reply = readFromClient();
			
			
			if (reply.length() < 4) {
				writeToClient("-ERR no valid command");
			}

			if (reply.length() >= 6) {
				postfix = reply.substring(5);
				reply = reply.substring(0, 4).toUpperCase();
				
				
			}

			reply = reply.toUpperCase();


			if (reply.equals("CAPA") || reply.equals("AUTH")) {
				writeToClient("-ERR no valid command");
			}

			else if (reply.equals("USER") && userLogIn == false) {
			
				if (postfix.equals(authenticUser.getUserName())) {
					
					writeToClient("+OK password required for user " + "'" + authenticUser.getUserName() + "'");
					userLogIn = true;
				} else {
					writeToClient("-ERR unknown user");
				}
			} else if (reply.equals("PASS") && userLogIn == true) {
				if (postfix.equals(authenticUser.getPassword())) {
					manager.clearList();
					manager.getMail();
					writeToClient("+OK " + authenticUser.getUserName()
							+ " has " + manager.getSize() + " massages" + " <" + manager.getAllOctets() + " octets>");
					loggedIn = true;
					
					break;
				} else {
					writeToClient("-ERR wrong password");
				}
			} else if (reply.equals("QUIT")) {
				writeToClient("+OK POP server signing off");
				serviceRequested = false;
				break;
			} else {
				// // PASS, CAPA, AUTH geh�rt auch dazu, da PASS nur in
				// Verbindung
				// // mit USER einen g�ltigen Befehl abgibt
				writeToClient("-ERR no valid command");
			}
		}
	}




	private String readFromClient() {
		String request = "";

		try {
			request = readIn.readLine();
			log.logToFile("TCP Server Thread detected job: " + request);
		} catch (IOException e) {
			System.err.println("Connection aborted by client!");
			serviceRequested = false;
		}
		
		return request;

	}

	private void writeToClient(String message) {
		/* Sende den String als Antwortzeile (mit newline) zum Client */
		try {
			writeTo.writeBytes(message + '\r' + '\n');
			log.logToFile("TCP Server Thread " + name +
	                " has written the message: " + message);
		} catch (IOException e) {
			System.err.println(e.toString());
			serviceRequested = false;
		}
			
		}

}