package CSE_Project;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class ClientCP2 {
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final String fileName = "C:\\Users\\Esmond\\Desktop\\NSAssignment\\smallFile.txt";

    public static void main(String[] args) {
        try {
            // create TCP socket for server at specified port
            Socket socket = new Socket(SERVER_NAME, SERVER_PORT);

            // channels for sending and receiving bytes
            OutputStream byteOut = socket.getOutputStream();
            InputStream byteIn = socket.getInputStream();

            // channels for sending and receiving plain text
            PrintWriter stringOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stringIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // initiate conversation with server
            stringOut.println("Hello SecStore, please prove your identity!");
            stringOut.flush();
            System.out.println("Sent to server: Hello SecStore, please prove your identity!");

            // wait for server to respond
            String firstResponse = stringIn.readLine();
            System.out.println(firstResponse);

            // send a nonce
            byte[] nonce = generateNonce();
            if (firstResponse.contains("this is SecStore")) {
                stringOut.println(Integer.toString(nonce.length));
                byteOut.write(nonce);
                byteOut.flush();
                System.out.println("Fresh nonce sent");
            }

            // retrieve encrypted nonce from server
            String encryptedNonceLength = stringIn.readLine();
            byte[] encryptedNonce = new byte[Integer.parseInt(encryptedNonceLength)];
            byteIn.read(encryptedNonce, 0, encryptedNonce.length);
            System.out.println("Encrypted nonce received");

            // ask for certificate
            stringOut.println("Give me your certificate signed by CA");
            stringOut.flush();
            System.out.println("Sent to server: Give me your certificate signed by CA");

            // extract public key from CA certificate
            InputStream fis = new FileInputStream("D:\\Library\\Documents\\SUTD\\50.005 Computer System Engineering\\NSProjectRelease\\NSProjectRelease\\CA.crt");
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(fis);
            PublicKey caPublicKey = caCert.getPublicKey();
            System.out.println("CA public key extracted");

            // retrieve signed certificate from server
            String certByteArrayLength = stringIn.readLine();
            stringOut.println("getting certificate");
            stringOut.flush();
            byte[] certByteArray = new byte[Integer.parseInt(certByteArrayLength)];
            byteIn.read(certByteArray, 0, certByteArray.length);


//            System.out.println(new String(certByteArray));
            InputStream certInputStream = new ByteArrayInputStream(certByteArray);
            X509Certificate signedCertificate = (X509Certificate) certificateFactory.generateCertificate(certInputStream);

            signedCertificate.checkValidity();
            signedCertificate.verify(caPublicKey);
            System.out.println("Signed certificate received and validity checked");

            // create cipher object and initialize is as decrypt mode, using PUBLIC key.
            PublicKey serverPublicKey = signedCertificate.getPublicKey();
            Cipher cipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipherDecrypt.init(Cipher.DECRYPT_MODE, serverPublicKey);

            // decrypt nonce
            byte[] decryptedNonce = cipherDecrypt.doFinal(encryptedNonce);
            if (Arrays.equals(nonce, decryptedNonce)) {
                System.out.println("Server's identity verified");
            } else {
                System.out.println("Identity verification unsuccessful, closing all connections");
                closeConnections(byteOut, byteIn, stringOut, stringIn, socket);
            }

            // create cipher object and initialize is as encrypt mode, use PUBLIC key.
            Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, serverPublicKey);

            // start file transfer
            System.out.println("DONE, INITIALIZING FILE TRANSFER");


            // generate AES key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecretKey aesKey = keyGen.generateKey();

            // convert secret key to byte array
            byte[] aesKeyBytes = aesKey.getEncoded();

            // encrypt AES key
            byte[] encryptedAESKeyBytes = rsaCipherEncrypt.doFinal(aesKeyBytes);


            // create cipher object for file encryption
            Cipher aesEnCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesEnCipher.init(Cipher.ENCRYPT_MODE, aesKey);

            // get bytes of file needed for transfer
            File file = new File("D:\\Library\\Documents\\SUTD\\50.005 Computer System Engineering\\NSProjectRelease\\NSProjectRelease\\sampleData\\largeFile.txt");
            byte[] fileBytes = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(fileBytes, 0, fileBytes.length);

            // encrypt file with AES key
            byte[] encryptedFileBytes = aesEnCipher.doFinal(fileBytes);

            // send encrypted AES session key
            stringOut.println(encryptedAESKeyBytes.length);
            System.out.println(encryptedAESKeyBytes.length);
            byteOut.write(encryptedAESKeyBytes, 0, encryptedAESKeyBytes.length);
            byteOut.flush();
            System.out.println("sent encrypted aes");



            // upload encrypted file to server
            stringOut.println(encryptedFileBytes.length);
            System.out.println(encryptedFileBytes.length);
            byteOut.write(encryptedFileBytes, 0, encryptedFileBytes.length);
            byteOut.flush();
            System.out.println("sent encrypted file");

            System.out.println("DONE");




            closeConnections(byteOut, byteIn, stringOut, stringIn, socket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateNonce() throws NoSuchAlgorithmException {
        // create secure random number generator
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

        // get 1024 random bytes
        byte[] nonce = new byte[64];
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    private static void closeConnections(OutputStream byteOut, InputStream byteIn, PrintWriter stringOut, BufferedReader stringIn, Socket socket) throws IOException {
        byteOut.close();
        byteIn.close();
        stringOut.close();
        stringIn.close();
        socket.close();
    }
}
