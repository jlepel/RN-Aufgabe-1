package fileManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MailReader {

	public static void printMailLine(String fileName, DataOutputStream writeTo) throws IOException {
		FileReader fr = new FileReader(fileName);
		
		BufferedReader br = new BufferedReader(fr);
		
		boolean eMailend = false;

		while (!eMailend) {
			String read = br.readLine();
			writeTo.writeBytes(read + '\r' + '\n');
			if (read.equals(".")) {
					
				eMailend = true;
				break;
			}
		}
	
		br.close();

	}
}
