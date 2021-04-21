public class ServerRunnerCP1 {

	public static void main(String args[]){
		try {
			ServerCP1 server = new ServerCP1();
			server.startConnection();
			server.receiveNonce();
			server.encryptNonce();
			server.sendEncryptedNonce();
			server.sendCertificate();
			//server.receiveSessionKey();
			server.receiveFiles();
			server.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
