package CSE_Project;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerCP2 {
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

            // encrypt nonce
            byte[] encryptedNonce = rsaCipherEncrypt.doFinal(nonce);
            stringOut.println(Integer.toString(encryptedNonce.length));
            byteOut.write(encryptedNonce, 0, encryptedNonce.length);
            byteOut.flush();
            System.out.println("Encrypted nonce sent");

            // wait for client response
            System.out.println(stringIn.readLine());

            // send signed certificate
            String certificateFileName = "D:\\Library\\Documents\\SUTD\\50.005 Computer System Engineering\\NSProjectRelease\\NSProjectRelease\\Signed Certificate - 1001294.crt";
            File certificateFile = new File(certificateFileName);
            byte[] certByteArray = new byte[(int) certificateFile.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certificateFile));
            bis.read(certByteArray, 0, certByteArray.length);


            stringOut.println(Integer.toString(certByteArray.length));
            System.out.println(stringIn.readLine());
            byteOut.write(certByteArray, 0, certByteArray.length);
            byteOut.flush();



            System.out.println("DONE, INITIALIZING FILE TRANSFER");



            // get encrypted AES session key from client
            String encryptedAESKeyBytesLength = stringIn.readLine();
            System.out.println(encryptedAESKeyBytesLength);
            byte[] encryptedAESKeyBytes = new byte[Integer.parseInt(encryptedAESKeyBytesLength)];
            int position = 0;
            do {
                int bytesRead = byteIn.read(encryptedAESKeyBytes, position, encryptedAESKeyBytes.length - position);

                if (bytesRead == -1){
                    break;
                } else {
                    position += bytesRead;
                }
            } while (position < encryptedAESKeyBytes.length);
//            byteIn.read(encryptedAESKeyBytes, 0, encryptedAESKeyBytes.length);
            System.out.println("received encrypted aes");


            // get encrypted file from client
            String encryptedFileBytesLength = stringIn.readLine();
            System.out.println(encryptedFileBytesLength);
            byte[] encryptedFileBytes = new byte[Integer.parseInt(encryptedFileBytesLength)];
            position = 0;
            do {
                int bytesRead = byteIn.read(encryptedFileBytes, position, encryptedFileBytes.length - position);

                if (bytesRead == -1){
                    break;
                } else {
                    position += bytesRead;
                }
            } while (position < encryptedFileBytes.length);
//            byteIn.read(encryptedFileBytes, 0, encryptedFileBytes.length);
            System.out.println("received encrypted file");


            // create cipher object for decryption of AES key
            Cipher rsaCipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);

            // decrypt the encrypted AES to get AES key bytes
            byte[] aesKeyBytes = rsaCipherDecrypt.doFinal(encryptedAESKeyBytes);

            // recreate AES key from the byte array
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");

            // create cipher object for decryption of file
            Cipher aesDeCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesDeCipher.init(Cipher.DECRYPT_MODE, aesKey);

            // decrypt the AES encrypted file
            byte[] fileBytes = aesDeCipher.doFinal(encryptedFileBytes);

            // create new file and write to file
            FileOutputStream fileOut = new FileOutputStream("data.txt");
            fileOut.write(fileBytes, 0, fileBytes.length);

            System.out.println("DONE");


            Thread.sleep(2000);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PrivateKey loadPrivateKey() throws Exception{
        String privateKeyFileName = "D:\\Library\\Documents\\SUTD\\50.005 Computer System Engineering\\NSProjectRelease\\NSProjectRelease\\privateServer.der";
        Path privateKeyPath = Paths.get(privateKeyFileName);
        byte[] privateKeyByteArray = Files.readAllBytes(privateKeyPath);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        return privateKey;
    }

    private static void closeConnections(OutputStream byteOut, InputStream byteIn, PrintWriter stringOut, BufferedReader stringIn, Socket socket) throws IOException {
        byteOut.close();
        byteIn.close();
        stringOut.close();
        stringIn.close();
        socket.close();
    }
}
