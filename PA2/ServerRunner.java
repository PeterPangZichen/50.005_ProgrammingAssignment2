import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunner {

	public static void main(String args[]){
		try {
			Server server = new Server();
			server.startConnection();
			server.receiveNonce();
			server.encryptNonce();
			server.sendEncryptedNonce();
			server.sendCertificate();
			// Handshake built
			server.receiveSessionKey();
			server.receiveFileNum();
			server.receiveFiles();
			server.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
