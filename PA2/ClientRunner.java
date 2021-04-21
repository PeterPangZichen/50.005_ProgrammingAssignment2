import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class ClientRunner {

	public static void main(String args[]){
		String[] filenames = new String[]{"picture.jpg"};
		if(args.length!=0) filenames = args;
		try {
			Client client = new Client();
			client.startConnection();
			client.sendNonce();
			client.receiveEncryptedNonce();
			client.receiveCertificate();
			client.readAndVerifyServerPublicKey();
			client.verifyNonce();
			System.out.println("Handshake is built, start sending files...");
			client.sendSessionKey();
			client.sendFileNum(filenames.length);
			client.sendFiles(filenames);
			client.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
