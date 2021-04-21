import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunner {

	public static void main(String args[]){
		try {
			Server server = new Server();
			server.startConnection();
			server.sendCertificate();
			server.receiveNonce();
			server.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
