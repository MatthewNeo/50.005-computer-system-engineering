package CSElabs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;


public class DesSolution {
    public static void main(String[] args) throws Exception {
        // Read the text file and save to String data
        String fileName = "C:\\Users\\Esmond\\Desktop\\NSLab2\\smallSize.txt";
        String data = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        while ((line = bufferedReader.readLine()) != null) {
            data = data + "\n" + line;
        }
        System.out.println("Original text: " + data);
        System.out.println("\nOriginal text in byte[] format: " + Arrays.toString(data.getBytes()));

        // generate secret key using DES algorithm
        SecretKey desKey = KeyGenerator.getInstance("DES").generateKey();

        // create cipher object, initialize the ciphers with the given key, choose encryption mode as DES
        Cipher desCipherEncrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipherEncrypt.init(Cipher.ENCRYPT_MODE, desKey);

        // do encryption, by calling method Cipher.doFinal()
        byte[] encryptedBytes = desCipherEncrypt.doFinal(data.getBytes());
        System.out.println("\nCypher text in byte[] format: " + Arrays.toString(encryptedBytes));
        System.out.println("\nCypher text as String: " + new String(encryptedBytes));

        // print the length of output encrypted byte[], compare the length of file smallSize.txt and largeSize.txt
        System.out.println("\nLength of output encrypted byte[]: " + encryptedBytes.length);

        // do format conversion. Turn the encrypted byte[] format into base64format String using DatatypeConverter
        String encryptedString = DatatypeConverter.printBase64Binary(encryptedBytes);

        // print the encrypted message (in base64format String format)
        System.out.println("\nCipher text: " + encryptedString);

        // create cipher object, initialize the ciphers with the given key, choose decryption mode as DES
        Cipher desCipherDecrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipherDecrypt.init(Cipher.DECRYPT_MODE, desKey);

        // do decryption, by calling method Cipher.doFinal()
        byte[] decryptedBytes = desCipherDecrypt.doFinal(encryptedBytes);
        System.out.println("\nDecrypted text in byte[] format: " + Arrays.toString(decryptedBytes));

        // do format conversion. Convert the decrypted byte[] to String, using "String a = new String(byte_array);"
        String decryptedString = new String(decryptedBytes);

        // print the decrypted String text and compare it with original text
        System.out.println("\nDecrypted text: " + decryptedString);
    }
}