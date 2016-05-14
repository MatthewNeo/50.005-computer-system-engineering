package CSElabs;

import javax.xml.bind.DatatypeConverter;
import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.*;


public class DigitalSignatureSolution {

    public static void main(String[] args) throws Exception {
        // Read the text file and save to String data
        String fileName = "C:\\Users\\Esmond\\Desktop\\NSLab2\\smallSize.txt";
        String data = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        while ((line = bufferedReader.readLine()) != null) {
            data = data + "\n" + line;
        }
        System.out.println("Original content: " + data);

        // create and update message digest object
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data.getBytes());
        System.out.println("\nSize of message digest: " + md.getDigestLength());

        // Calculate message digest, using MD5 hash function
        byte[] digest = md.digest();

        // print the length of output digest byte[], compare the length of file smallSize.txt and largeSize.txt
        System.out.println("\nLength of output digest byte[]: " + digest.length);

        // generate a RSA keypair, initialize as 1024 bits, get public key and private key from this keypair.
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();

        // Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as encrypt mode, use PRIVATE key.
        Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, privateKey);

        // encrypt digest message
        byte[] encryptedBytes = rsaCipherEncrypt.doFinal(digest);
        System.out.println("\nSize of encryptedBytes array: " + encryptedBytes.length);

        // print the encrypted message (in base64format String using DatatypeConverter)
        System.out.println("\nCipher text: " + DatatypeConverter.printBase64Binary(encryptedBytes));

        // Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as decrypt mode, use PUBLIC key.
        Cipher rsaCipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipherDecrypt.init(Cipher.DECRYPT_MODE, publicKey);

        // decrypt message
        byte[] decryptedBytes = rsaCipherDecrypt.doFinal(encryptedBytes);
        System.out.println("\nSize of decryptedBytes array: " + decryptedBytes.length);

        // print the decrypted message (in base64format String using DatatypeConverter), compare with origin digest
        System.out.println("\nDecrypted text: " + DatatypeConverter.printBase64Binary(decryptedBytes));
    }
}