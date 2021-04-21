import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
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
import java.util.Arrays;
import java.util.Random;

public class Server {

    int port = 4321;
    int filenum = 1;

    PublicKey publishKey;
    PrivateKey privateKey;
    SecretKey sessionKey;

    X509Certificate serverCert;

    byte[] nonce = new byte[32];
    byte[] encryptedNonce = new byte[128];
    byte[] encryptedSessionKey;
    byte[] decryptedSessionKey;

    ServerSocket welcomeSocket = null;
    Socket connectionSocket = null;
    DataOutputStream toClient = null;
    DataInputStream fromClient = null;

    FileOutputStream fileOutputStream = null;

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

    public void receiveFileNum() throws IOException {
        filenum = fromClient.readInt();
    }

    public void receiveFile(){
        try{
            while (!connectionSocket.isClosed()) {

                int packetType = fromClient.readInt();

                // If the packet is for transferring the filename
                if (packetType == 0) {

                    System.out.println("Receiving file...");

                    // Initialize cipher
                    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey);

                    int numBytes = fromClient.readInt();
                    byte[] encryptedFilenameBytes = new byte[numBytes];
                    fromClient.readFully(encryptedFilenameBytes, 0, numBytes);

                    // Decrypt filename
                    byte[] filenameBytes;
                    filenameBytes = cipher.doFinal(encryptedFilenameBytes);

                    fileOutputStream = new FileOutputStream("recv_" + new String(filenameBytes, 0, filenameBytes.length));

                    // If the packet is for transferring a chunk of the file
                } else if (packetType == 1) {

                    // Initialize cipher
                    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey);

                    int numBytes = fromClient.readInt();
                    byte[] encryptedBlock = new byte[numBytes];
                    fromClient.readFully(encryptedBlock, 0, numBytes);

                    // Decrypt the file
                    byte[] fileBytes = cipher.doFinal(encryptedBlock);
                    System.out.println("File size: "+fileBytes.length);

                    fileOutputStream.write(fileBytes);
                    fileOutputStream.close();
                    break;
                    // If the packet is for transferring the session key
                } else if (packetType == 2) {

                    int numBytes = fromClient.readInt();
                    encryptedSessionKey = new byte[numBytes];
                    fromClient.readFully(encryptedSessionKey, 0, numBytes);
                    break;

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void receiveFiles(){
        for(int i=0;i<filenum;i++){
            receiveFile();
        }
    }

    public void closeConnection() throws IOException {
        System.out.println("Closing connection...");
        fileOutputStream.close();
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

    public void receiveSessionKey() throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        // Receive session key from client
        receiveFile();
        // Decrypt session key
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        decryptedSessionKey = cipher.doFinal(encryptedSessionKey);
        // Build session key instance
        sessionKey = new SecretKeySpec(decryptedSessionKey,0,decryptedSessionKey.length,"AES");
    }

    public void encryptFragment(){

    }
}
