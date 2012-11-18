package User;

public class UserData {

	private String userName;
	private String password;
	private String serverIp;
	private int port;

	public UserData(String userName, String password, String serverIp, int port) {
		this.userName = userName;
		this.password = password;
		this.serverIp = serverIp;
		this.port = port;
	}
	
	public UserData(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getServerIp() {
		return serverIp;
	}

	public int getPort() {
		return port;
	}
	
	
}
