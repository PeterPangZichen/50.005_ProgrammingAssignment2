public class ServerRunnerCP1 {

	public static void main(String args[]){
		try {
			ServerCP2 serverCP2 = new ServerCP2();
			serverCP2.startConnection();
			serverCP2.receiveNonce();
			serverCP2.encryptNonce();
			serverCP2.sendEncryptedNonce();
			serverCP2.sendCertificate();
			// Handshake built
			serverCP2.receiveSessionKey();
			serverCP2.receiveFileNum();
			serverCP2.receiveFiles();
			serverCP2.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
