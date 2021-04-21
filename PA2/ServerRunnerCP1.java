public class ServerRunnerCP1 {

	public static void main(String args[]){
		try {
			ServerCP1 serverCP1 = new ServerCP1();
			serverCP1.startConnection();
			serverCP1.receiveNonce();
			serverCP1.encryptNonce();
			serverCP1.sendEncryptedNonce();
			serverCP1.sendCertificate();
			//server.receiveSessionKey();
			serverCP1.receiveFiles();
			serverCP1.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
