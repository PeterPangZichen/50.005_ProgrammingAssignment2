import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Random;

public class ClientCP1 {

    String filename = "100.txt";
    String serverAddress = "localhost";
    int port = 4321;

    PublicKey serverKey;

    X509Certificate serverCert;

    private byte[] nonce = new byte[32];
    private byte[] decryptedNonce = new byte[32];
    private byte[] encryptedNonce = new byte[128];

    Socket clientSocket = null;
    DataOutputStream toServer = null;
    DataInputStream fromServer = null;

    FileInputStream fileInputStream = null;
    BufferedInputStream bufferedFileInputStream = null;

    public void setFilename(String filename){
        this.filename = filename;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void startConnection() throws IOException {
        // Connect to server and get the input and output streams
        System.out.println("Establishing connection to server...");
        clientSocket = new Socket(serverAddress, port);
        toServer = new DataOutputStream(clientSocket.getOutputStream());
        fromServer = new DataInputStream(clientSocket.getInputStream());
    }

    public void closeConnection() throws IOException {
        System.out.println("Closing connection...");
        if (bufferedFileInputStream != null) bufferedFileInputStream.close();
        if (fileInputStream != null) fileInputStream.close();
        fromServer.close();
        toServer.close();
        clientSocket.close();
    }

    public void sendFile() {
        int numBytes = 0;
        long timeStarted = System.nanoTime();

        try {
            System.out.println("Sending file...");

            // Initialize cipher
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);

            // Encrypt filename
            byte[] filenameBytes = filename.getBytes();
            byte[] encryptedFilenameBytes;
            encryptedFilenameBytes = cipher.doFinal(filenameBytes);

            // Send the filename
            toServer.writeInt(0);
            toServer.writeInt(encryptedFilenameBytes.length);
            toServer.write(encryptedFilenameBytes);
            toServer.flush();

            // Open the file
            fileInputStream = new FileInputStream(filename);
            bufferedFileInputStream = new BufferedInputStream(fileInputStream);

            byte [] fromFileBuffer = new byte[117];

            // Send the file
            for (boolean fileEnded = false; !fileEnded;) {
                numBytes = bufferedFileInputStream.read(fromFileBuffer);
                // Encrypt the file
                byte[] fromFileBufferEncrypted=cipher.doFinal(fromFileBuffer);
                fileEnded = numBytes < 117;

                toServer.writeInt(1);
                toServer.writeInt(numBytes);
                toServer.writeInt(fromFileBufferEncrypted.length);
                toServer.write(fromFileBufferEncrypted);
                toServer.flush();
            }
        } catch (Exception e) {e.printStackTrace();}

        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
    }

    public void sendFileNum(int filenum) throws IOException {
        toServer.writeInt(filenum);
        toServer.flush();
    }

    public void sendFiles(String[] filenames){
        for(int i=0;i<filenames.length;i++){
            setFilename(filenames[i]);
            System.out.println("Sending file: "+filename+"...");
            sendFile();
        }
    }

    public void sendNonce() throws IOException {
        System.out.println("Sending nonce to server...");
        Random random = new Random();
        random.nextBytes(nonce);
        toServer.write(nonce);
        toServer.flush();
    }

    public void receiveEncryptedNonce() throws IOException {
        fromServer.read(encryptedNonce);
    }

    public void verifyNonce() throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, DifferentNonceException {
        System.out.println("Verifying server's encrypted nonce...");
        decryptNonce();
        boolean check = Arrays.equals(decryptedNonce, nonce);
        if(!check){
            throw new DifferentNonceException();
        }
        System.out.println("Nonce check pass!");
    }

    public void receiveCertificate() throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        serverCert =(X509Certificate)cf.generateCertificate(fromServer);
    }

    public void decryptNonce() throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, serverKey);
        decryptedNonce = cipher.doFinal(encryptedNonce);
    }

    public void readAndVerifyServerPublicKey() throws FileNotFoundException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        System.out.println("Verifying server's certificate...");
        // Get CA's public key from CA's certificate
        InputStream fis = new FileInputStream("cacsertificate.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate CAcert =(X509Certificate)cf.generateCertificate(fis);

        //public key of CA
        PublicKey CAKey = CAcert.getPublicKey();
        //public key of server
        serverKey = serverCert.getPublicKey();

        //Check and verify with CA public key
        serverCert.checkValidity();
        serverCert.verify(CAKey);

        System.out.println("Certificate check pass!");
    }

    class DifferentNonceException extends Exception{

    }
}
