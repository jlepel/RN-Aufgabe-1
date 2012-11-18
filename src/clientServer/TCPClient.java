//fhtestmailer@web.de
//PWD: fhtestmailer123 

package clientServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;




import User.UserData;

import fileManager.MailWriter;
import fileManager.TxtLogging;

public class TCPClient {
	private Socket clientSocket; // TCP-Standard-Socketklasse
	final TxtLogging log = new TxtLogging(true);
	private DataOutputStream writeOut; // Ausgabestream zum Server
	private BufferedReader readIn; // Eingabestream vom Server
	private UserData user;
	

	public TCPClient(UserData user) {
		this.user = user;

	}

	
	 public static void main(String[] args){
		 UserData testUser = new UserData("fhtestmailer@web.de",
					"fhtestmailer123", "pop3.web.de", 110);
		 
		 UserData fhmailer = new UserData("fhmailer@web.de",
					"fhmailer123", "pop3.web.de", 110);
		 
		 TCPClient client1 = new TCPClient(testUser); 
		 TCPClient client2 = new TCPClient(fhmailer); 		
		 
		 while (true) {
			 		try {
						client1.startJob();
						client2.startJob();
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
			 		
					
					try {
						System.out.println("Client gestartet");	
						Thread.sleep(40000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
		}
	 }

	public void startJob() throws UnknownHostException, IOException {

		/* Socket erzeugen --> Verbindungsaufbau mit dem Server */
		clientSocket = new Socket(user.getServerIp(), user.getPort());

		/* Socket-Basisstreams durch spezielle Streams filtern */
		readIn = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));

		writeOut = new DataOutputStream(clientSocket.getOutputStream());
		String reply = "";

		// Willkommensnachricht des Servers
		reply = readFromServer();
		System.out.println(reply);
		log.logToFile(reply);

		int retriev;
		// Benutzerlogin

		if (userLogIn(user.getUserName(), user.getPassword())) {

			retriev = checkForMails(); // Rückgabe der Emailanzahl
			if (retriev > 0) {
				saveMails(retriev); // Speichern der Mails in Datei und löschen
			} else {
				closeConnection();

			}
		}

	}

	private void saveMails(int retriev) throws IOException {
		String reply = "";
		String replylocaleFileName = "";

		for (int i = 1; i <= retriev; i++) {
			// holt sich die Unique ID der Mail

			writeToServer("UIDL " + i); // anfragen der uidl

			replylocaleFileName = readFromServer(); // server schickt dann uidl
													// zurück, wir speichern
													// diese ab
			System.out.println(replylocaleFileName);

			String[] subelem = replylocaleFileName.split(" "); // von stat das
																// 2. elem ist
																// anzahl mails,
																// rest
																// unwichtig
			String fileName = (subelem[2]);
			// manager.getMail(fileName);

			// "mailserver#user","UIDL","Local File Name","Download Date Time","Flags"

			writeToServer("RETR " + i);
			reply = readFromServer();

			if (reply.startsWith("-Err")) {
				System.out.println("Can't read Mail " + i);
				closeConnection();
			} else {

				boolean eMailend = false;

				while (!eMailend) {
					reply = readFromServer();
					System.out.println(reply);
					if (reply.equals(".")) {
						MailWriter.writeMail(fileName, reply);
						eMailend = true;
						break;
					}

					MailWriter.writeMail(fileName, reply);

				}
			}
			writeToServer("DELE " + i);
			if (readFromServer().contains("+OK")) {
				System.out.println("Message " + i + " deleted");
			} else {
				System.out.println("Could not delete message " + i);
			}
		}

		closeConnection();

	}

	private int checkForMails() throws IOException {
		String reply = "";
		writeToServer("STAT");// stat gibt anzahl mails aufm server zurück ->
								// string mit oktalzahlen
		reply = readFromServer();
		if (reply.startsWith("-ERR")) {
			System.out.println("Error while reading");
			closeConnection();

		}
		String[] subelem = reply.split(" "); // von stat das 2. elem ist anzahl
												// mails, rest unwichtig
		System.out.println("Items in your Mailbox: " + subelem[1]);
		return Integer.parseInt(subelem[1]);

	}

	private boolean userLogIn(String userName, String pwd) throws IOException {
		String reply = "";
		boolean statusLogin = false;
		writeToServer("USER " + userName);
		reply = readFromServer();
		if (reply.contains("-ERR")) {

			System.out.println("Wrong or incorrect Username");
			closeConnection();
			return statusLogin = false;

		} else {
			System.out.println("Username correct");
			statusLogin = true;
		}

		writeToServer("PASS " + pwd);
		reply = readFromServer();

		if (reply.contains("-ERR")) {
			System.out.println("Wrong or incorrect PWD");
			closeConnection();
			return statusLogin = false;
		} else {
			System.out.println("PWD correct");
			statusLogin = true;
		}
		return statusLogin;
	}

	private String readFromServer() throws IOException {

		String reply = "";

		try {
			reply = readIn.readLine();
		} catch (IOException e) {
			System.err.println("Connection aborted by server!");

		}
		log.logToFile(reply);
		return reply;
	}

	private void writeToServer(String request) throws IOException {
		/* Sende den request-String als eine Zeile (mit newline) zum Server */
		
		 try {
			 	writeOut.writeBytes(request + '\n');
		    } catch (IOException e) {
		      System.err.println(e.toString());
		      
		    }
		 log.logToFile("TCP Client has sent the message: " + request);

	}

	private void closeConnection() throws IOException {
		writeToServer("QUIT");
		clientSocket.close();

		System.out.println("============Connection closed===================");
	}
}
