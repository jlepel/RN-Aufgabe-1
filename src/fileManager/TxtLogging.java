package fileManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class TxtLogging {

	private static boolean active;
	private static FileWriter writer;
	
	
	public TxtLogging(boolean active){
		TxtLogging.active = active;
	}
	
	

	public void logToFile(String inPut) throws IOException {
		
		if (active == true) {
		
		Timestamp tstamp = new Timestamp(System.currentTimeMillis()); 
		String[] subelem = tstamp.toString().split(" ");
		String fileName = "log ";
		String concatFile = fileName.concat(subelem[0] + ".log");
		File file = new File(concatFile);
		
		
		writer = new FileWriter(file, true);
		writer.append('\n');
		writer.append(tstamp.toString() + " ");
		writer.append(inPut);
		writer.flush();
		writer.close();
		}
		else{
			File file = new File("LoggingNotActive.txt");
			writer = new FileWriter(file, true);
			writer.append("Logging nicht aktiv");
		}
	}
}
