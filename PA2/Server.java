import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;

public class Server {

    int port = 4321;

    PublicKey publishKey;
    PrivateKey privateKey;

    X509Certificate serverCert;

    byte[] nonce = new byte[32];
    byte[] encryptedNonce = new byte[128];

    ServerSocket welcomeSocket = null;
    Socket connectionSocket = null;
    DataOutputStream toClient = null;
    DataInputStream fromClient = null;

    FileOutputStream fileOutputStream = null;
    BufferedOutputStream bufferedFileOutputStream = null;

    public Server() throws Exception {
        publishKey = PublicKeyReader.get("publish_key.der");
        privateKey = PrivateKeyReader.get("private_key.der");

        InputStream fis = new FileInputStream("certificate.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        serverCert = (X509Certificate)cf.generateCertificate(fis);
    }

    public void startConnection() throws IOException {
        welcomeSocket = new ServerSocket(port);
        System.out.println("Wait for client's connection...");
        connectionSocket = welcomeSocket.accept();
        // Wait until socket is connected
        System.out.println("Establishing connection to client...");
        fromClient = new DataInputStream(connectionSocket.getInputStream());
        toClient = new DataOutputStream(connectionSocket.getOutputStream());
    }

    public void receiveNonce() throws IOException {
        System.out.println("Receiving nonce from client...");
        fromClient.read(nonce);
    }

    public void receiveFiles() throws IOException {
        while (!connectionSocket.isClosed()) {

            int packetType = fromClient.readInt();

            // If the packet is for transferring the filename
            if (packetType == 0) {

                System.out.println("Receiving file...");

                int numBytes = fromClient.readInt();
                byte [] filename = new byte[numBytes];
                // Must use read fully!
                // See: https://stackoverflow.com/questions/25897627/datainputstream-read-vs-datainputstream-readfully
                fromClient.readFully(filename, 0, numBytes);

                fileOutputStream = new FileOutputStream("recv_"+new String(filename, 0, numBytes));
                bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

            // If the packet is for transferring a chunk of the file
            } else if (packetType == 1) {

                int numBytes = fromClient.readInt();
                byte [] block = new byte[numBytes];
                fromClient.readFully(block, 0, numBytes);

                if (numBytes > 0)
                    bufferedFileOutputStream.write(block, 0, numBytes);

                if (numBytes < 117) {
                    break;
                }

            // If the packet is for transferring certificate
            } else if (packetType == 2) {

            } else if (packetType == 3){

            }
        }
    }

    public void closeConnection() throws IOException {
        System.out.println("Closing connection...");
        if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
        if (bufferedFileOutputStream != null) fileOutputStream.close();
        fromClient.close();
        toClient.close();
        welcomeSocket.close();
        connectionSocket.close();
    }

    public void encryptNonce() throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        System.out.println("Encrypting nonce...");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        encryptedNonce = cipher.doFinal(nonce);
    }

    public void sendEncryptedNonce() throws IOException {
        System.out.println("Sending encrypted nonce to client...");
        toClient.write(encryptedNonce);
        toClient.flush();
    }

    public void sendCertificate() throws IOException, CertificateEncodingException {
        System.out.println("Sending certificate to client...");
        toClient.write(serverCert.getEncoded());
        toClient.flush();
    }
}
