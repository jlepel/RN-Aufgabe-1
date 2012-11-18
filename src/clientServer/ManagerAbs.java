package clientServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManagerAbs {
	List<String> list;
	private List<String> deleList;
	String absPath = System.getProperty("user.dir");

	public ManagerAbs() {
		list = new ArrayList<String>();
		deleList = new ArrayList<String>();
	}

	public void getMail() {
		File dir = new File(absPath);

		File[] fileList = dir.listFiles();
		for (File f : fileList) {
			if (f.toString().endsWith(".txt") && !list.contains(f.toString())) {
				list.add(f.getName());
			}
		}

	}

	public void clearList(){
		list.clear();
	}
	
	public int getSize() {
		return list.size();
		
	}
	
	public int getOctets() {
		File dir = new File(absPath);
		int octets = 0;
		
		
		File[] fileList = dir.listFiles();
		for (File f : fileList) {
			if (f.toString().endsWith(".txt") && !list.contains(f.toString())) {
				octets += f.length();
			}
		}
		return octets;
	}

	public String getElem(int index) {
		return list.get(index - 1);
	}

	public void delete(int index) {
		String elem = list.get(index - 1);
		// speichert den arrayindex am ende der liste, damit rset ihn
		// wiederherstellen kann
		deleList.add(elem.concat("" + (index - 1)));
		list.set(index - 1, null);
	}

	public void rset() {
		for (String elem : deleList) {
			String lastElem = elem.substring(elem.length() - 1);
			list.set((Integer.parseInt(lastElem)),
					elem.substring(0, elem.length() - 1));
		}
	}

	/*
	 * public void deleteAbs(int index) {
	 * 
	 * String elem = list.get(index-1) ; File dir = new File(absPath);
	 * 
	 * list.set(index - 1, null);
	 * 
	 * for (File f : dir.listFiles()) { if(f.toString().contains(elem)){ } }
	 * 
	 * }
	 */

	public void deleteAbs() {

		File dir = new File(absPath);
		// dir.listFiles() muss immer gleichlang wie deleList sein
		for (File f : dir.listFiles()) {
			for (int i = 0; i < deleList.size(); i++) {
				String elem = deleList.get(i);
				if (f.toString().contains(elem.substring(0, elem.length() - 1))) {
					f.delete();
					break;
				}
			}
		}
		deleList.clear();
	}

	public void showAll() {
		for (String elem : list) {
			System.out.println(elem);
		}

	}

}
