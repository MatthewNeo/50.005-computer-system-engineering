package CSElabs;

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.BufferedImage;
import java.nio.*;
import javax.crypto.*;


public class DesImageSolution {
    public static void main(String[] args) throws Exception {
        int image_width = 200;
        int image_height = 200;

        // read image file and save pixel value into int[][] imageArray
        BufferedImage img = ImageIO.read(new File("C:\\Users\\Esmond\\Desktop\\NSLab2\\SUTD.bmp"));

        image_width = img.getWidth();
        image_height = img.getHeight();

        // byte[][] imageArray = new byte[image_width][image_length];
        int[][] imageArray = new int[image_width][image_height];
        for (int idx = 0; idx < image_width; idx++) {
            for (int idy = 0; idy < image_height; idy++) {
                int color = img.getRGB(idx, idy);
                imageArray[idx][idy] = color;
            }
        }

        // generate secret key using DES algorithm
        SecretKey desKey = KeyGenerator.getInstance("DES").generateKey();

        // Create cipher object, initialize the ciphers with the given key, choose encryption algorithm/mode/padding,
        // you need to try both ECB and CBC mode, use PKCS5Padding padding method
        Cipher desCipherEncrypt = Cipher.getInstance("DES/CBC/PKCS5Padding");
        desCipherEncrypt.init(Cipher.ENCRYPT_MODE, desKey);

        // define output BufferedImage, set size and format
        BufferedImage outImage = new BufferedImage(image_width, image_height, BufferedImage.TYPE_3BYTE_BGR);

        for (int idx = 0; idx < image_width; idx++) {
            // convert each column int[] into a byte[] (each_width_pixel)
            byte[] eachWidthPixel = new byte[4 * image_height];
            for (int idy = 0; idy < image_height; idy++) {
                ByteBuffer dbuf = ByteBuffer.allocate(4);
                dbuf.putInt(imageArray[idx][idy]);
                byte[] bytes = dbuf.array();
                System.arraycopy(bytes, 0, eachWidthPixel, idy * 4, 4);
            }

            // encrypt each column or row bytes
            byte[] encryptedImageBytes = desCipherEncrypt.doFinal(eachWidthPixel);
            byte[] encryptedPixel = new byte[4];
            for (int idy = 0; idy <image_height; idy++) {
                System.arraycopy(encryptedImageBytes, idy * 4, encryptedPixel, 0, 4);
                ByteBuffer wrapped = ByteBuffer.wrap(encryptedPixel);

                // convert the encrypted byte[] back into int[] and write to outImage (use setRGB)
                int newColor = wrapped.getInt();
                outImage.setRGB(idx, idy, newColor);
            }
        }

        // write outImage into file
        ImageIO.write(outImage, "BMP", new File("C:\\Users\\Esmond\\Desktop\\NSLab2\\EnSUTD2.bmp"));
    }
}