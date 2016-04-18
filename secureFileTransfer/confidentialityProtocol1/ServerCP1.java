package CSElabs;

import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerCP1 {
    private static final int PORT_NUMBER = 1234;

    public static void main(String[] args) {
        try {
            // create TCP ServerSocket to listen
            // then create TCP ClientSocket upon accepting incoming TCP request
            ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
            System.out.println("... expecting connection ...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("... connection established...");

            // channels for sending and receiving bytes
            OutputStream byteOut = clientSocket.getOutputStream();
            InputStream byteIn = clientSocket.getInputStream();

            // channels for sending and receiving plain text
            PrintWriter stringOut = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader stringIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // wait for client to initiate conversation
            System.out.println(stringIn.readLine());

            // reply to client
            stringOut.println("Hello, this is SecStore!");
            stringOut.flush();
            System.out.println("Sent to client: Hello, this is SecStore!");

            // retrieve nonce from client
            String nonceLength = stringIn.readLine();
            byte[] nonce = new byte[Integer.parseInt(nonceLength)];
            byteIn.read(nonce, 0, nonce.length);
            System.out.println("Nonce received");

            // load private key from .der
            PrivateKey privateKey = loadPrivateKey();

            // Create cipher object and initialize is as encrypt mode, use PRIVATE key.
            Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, privateKey);

            // encrypt nonce and send encrypted nonce
            byte[] encryptedNonce = rsaCipherEncrypt.doFinal(nonce);
            stringOut.println(Integer.toString(encryptedNonce.length));
            byteOut.write(encryptedNonce, 0, encryptedNonce.length);
            byteOut.flush();
            System.out.println("Encrypted nonce sent");

            // wait for client response
            System.out.println(stringIn.readLine());

            // send signed certificate
            String certificateFileName = "C:\\Users\\Esmond\\Desktop\\NSAssignment\\Signed Certificate - 1001294.crt";
            File certificateFile = new File(certificateFileName);
            byte[] certByteArray = new byte[(int) certificateFile.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certificateFile));
            bis.read(certByteArray, 0, certByteArray.length);

            stringOut.println(Integer.toString(certByteArray.length));
            System.out.println(stringIn.readLine());
            byteOut.write(certByteArray, 0, certByteArray.length);
            byteOut.flush();

            // create cipher object and initialize is as encrypt mode, use PUBLIC key.
            Cipher rsaCipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);

            // start file transfer
            System.out.println("File transfer initiated");
            String encryptedFileLength  = stringIn.readLine();
            byte[] encryptedFile = new byte[Integer.parseInt(encryptedFileLength)];
            byteIn.read(encryptedFile, 0, encryptedFile.length);

            byte[] decryptedFile = decryptFile(encryptedFile, rsaCipherDecrypt);
            System.out.println(new String(decryptedFile));

            stringOut.println("File transfer successful");
            stringOut.flush();
            System.out.println("File transfer successful");

            closeConnections(byteOut, byteIn, stringOut, stringIn, clientSocket, serverSocket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PrivateKey loadPrivateKey() throws Exception{
        String privateKeyFileName = "C:\\Users\\Esmond\\Desktop\\NSAssignment\\privateServer.der";
        Path privateKeyPath = Paths.get(privateKeyFileName);
        byte[] privateKeyByteArray = Files.readAllBytes(privateKeyPath);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        return privateKey;
    }

    private static byte[] decryptFile(byte[] encryptedFile, Cipher rsaCipherDecrypt) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int count = 0;
        while (count < encryptedFile.length) {
            byte[] placeHolder;
            if (encryptedFile.length - count >= 128) {
                placeHolder = rsaCipherDecrypt.doFinal(encryptedFile, count, 128);
            } else {
                placeHolder = rsaCipherDecrypt.doFinal(encryptedFile, count, encryptedFile.length - count);
            }
            byteArrayOutputStream.write(placeHolder, 0, placeHolder.length);
            count += 128;
        }
        byte[] decryptedFile = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();

        return decryptedFile;
    }

    private static void closeConnections(OutputStream byteOut, InputStream byteIn, PrintWriter stringOut, BufferedReader stringIn, Socket socket, ServerSocket serverSocket) throws IOException {
        byteOut.close();
        byteIn.close();
        stringOut.close();
        stringIn.close();
        socket.close();
        serverSocket.close();
    }
}
