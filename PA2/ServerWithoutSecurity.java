import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWithoutSecurity {

	static int port = 4321;

	static ServerSocket welcomeSocket = null;
	static Socket connectionSocket = null;
	static DataOutputStream toClient = null;
	static DataInputStream fromClient = null;

	static FileOutputStream fileOutputStream = null;
	static BufferedOutputStream bufferedFileOutputStream = null;

	public static void startConnection() throws IOException {
		welcomeSocket = new ServerSocket(port);
		connectionSocket = welcomeSocket.accept();
		// Wait until socket is connected
		fromClient = new DataInputStream(connectionSocket.getInputStream());
		toClient = new DataOutputStream(connectionSocket.getOutputStream());

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
					System.out.println("Closing connection...");

					if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
					if (bufferedFileOutputStream != null) fileOutputStream.close();
					fromClient.close();
					toClient.close();
					connectionSocket.close();
				}

				// If the packet is for transferring certificate
			} else if (packetType == 2) {

			}

		}
	}

	public static void main(String[] args) {

    	if (args.length > 0) port = Integer.parseInt(args[0]);

		try {
			startConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
