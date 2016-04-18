package CSElabs;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class ClientCP1 {
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
            InputStream fis = new FileInputStream("C:\\Users\\Esmond\\Desktop\\NSAssignment\\CA.crt");
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(fis);
            PublicKey caPublicKey = caCert.getPublicKey();
            System.out.println("CA public key extracted");

            // retrieve signed certificate from server
            String certByteArrayLength = stringIn.readLine();
            stringOut.println("Ready to receive signed certificate");
            stringOut.flush();
            byte[] certByteArray = new byte[Integer.parseInt(certByteArrayLength)];
            byteIn.read(certByteArray, 0, certByteArray.length);
//            System.out.println(new String(certByteArray));
            InputStream certInputStream = new ByteArrayInputStream(certByteArray);
            X509Certificate signedCertificate = (X509Certificate) certificateFactory.generateCertificate(certInputStream);

            // check validity and verify signed certificate
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
            System.out.println("File transfer initiated");
            byte[] encryptedFile = encryptFile(fileName, rsaCipherEncrypt);

            // send encrypted file
            stringOut.println(encryptedFile.length);
            byteOut.write(encryptedFile, 0, encryptedFile.length);
            byteOut.flush();

            System.out.println(stringIn.readLine());

            closeConnections(byteOut, byteIn, stringOut, stringIn, socket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateNonce() throws NoSuchAlgorithmException {
        // create secure random number generator
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

        // get 64 random bytes
        byte[] nonce = new byte[64];
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    private static byte[] encryptFile(String fileName, Cipher rsaCipherEncrypt) throws Exception {
        Path filePath = Paths.get(fileName);
        byte[] fileData = Files.readAllBytes(filePath);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int count = 0;
        while (count < fileData.length) {
            byte[] placeHolder;
            if (fileData.length - count >= 117) {
                placeHolder = rsaCipherEncrypt.doFinal(fileData, count, 117);
            } else {
                placeHolder = rsaCipherEncrypt.doFinal(fileData, count, fileData.length - count);
            }
            byteArrayOutputStream.write(placeHolder, 0, placeHolder.length);
            count += 117;
        }
        byte[] encryptedFile = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();

        return encryptedFile;
    }

    private static void closeConnections(OutputStream byteOut, InputStream byteIn, PrintWriter stringOut, BufferedReader stringIn, Socket socket) throws IOException {
        byteOut.close();
        byteIn.close();
        stringOut.close();
        stringIn.close();
        socket.close();
    }
}
