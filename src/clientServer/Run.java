package clientServer;
import java.io.IOException;
import java.net.UnknownHostException;


public class Run {
	public static final ManagerAbs manager = new ManagerAbs();
	/**
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @uml.property  name="logger"
	 * @uml.associationEnd  
	 */
		
	public static void main(String[] args) throws UnknownHostException, IOException {
		
//	MailReader.printMailLine("0LeOC9-1T3BhM3xKH-00qlSl.txt");


		TCPServer server = new TCPServer(manager);				
		System.out.println("Server gestartet");
		server.run();
		
		

	}
	
	
}
