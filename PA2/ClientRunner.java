import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ClientRunner {

	public static void main(String args[]){
		try {
			Client client = new Client();
			client.startConnection();
			client.receiveCertificate();
			client.readAndVerifyServerPublicKey();
			client.sendNonce();
			client.closeConnection();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
