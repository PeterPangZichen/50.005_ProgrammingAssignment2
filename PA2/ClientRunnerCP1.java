import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ClientRunnerCP1 {

	public static void main(String args[]){
		try {
			ClientCP1 client = new ClientCP1();
			client.startConnection();
			client.sendNonce();
			client.receiveEncryptedNonce();
			client.receiveCertificate();
			client.readAndVerifyServerPublicKey();
			client.verifyNonce();
			System.out.println("Handshake is built, start sending files...");
			//client.sendSessionKey();
			client.sendFiles();
			client.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
