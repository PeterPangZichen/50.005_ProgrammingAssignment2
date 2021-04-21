public class ClientRunnerCP1 {

	public static void main(String args[]){
		String[] filenames = new String[]{"100.txt","200.txt","500.txt","1000.txt","5000.txt","10000.txt","50000.txt","100000.txt"};
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
