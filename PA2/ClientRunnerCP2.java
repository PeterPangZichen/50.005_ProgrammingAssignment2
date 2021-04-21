public class ClientRunnerCP2 {

	public static void main(String args[]){
		String[] filenames = new String[]{"100.txt","100.txt","200.txt","500.txt","1000.txt","5000.txt","10000.txt","50000.txt","100000.txt"};
		if(args.length!=0) filenames = args;
		try {
			ClientCP2 clientCP2 = new ClientCP2();
			clientCP2.startConnection();
			clientCP2.sendNonce();
			clientCP2.receiveEncryptedNonce();
			clientCP2.receiveCertificate();
			clientCP2.readAndVerifyServerPublicKey();
			clientCP2.verifyNonce();
			System.out.println("Handshake is built, start sending files...");
			clientCP2.sendSessionKey();
			clientCP2.sendFileNum(filenames.length);
			clientCP2.sendFiles(filenames);
			clientCP2.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
