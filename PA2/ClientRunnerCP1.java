public class ClientRunnerCP1 {

	public static void main(String args[]){
		String[] filenames = new String[]{"picture.jpg","music.mp3"};
		if(args.length!=0) filenames = args;
		try {
			ClientCP1 clientCP1 = new ClientCP1();
			clientCP1.startConnection();
			clientCP1.sendNonce();
			clientCP1.receiveEncryptedNonce();
			clientCP1.receiveCertificate();
			clientCP1.readAndVerifyServerPublicKey();
			clientCP1.verifyNonce();
			System.out.println("Handshake is built, start sending files...");
			clientCP1.sendFileNum(filenames.length);
			clientCP1.sendFiles(filenames);
			clientCP1.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
