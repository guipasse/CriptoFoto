package tfm.uoc.edu.criptofoto.security;


import tfm.uoc.edu.criptofoto.constants.MainConstants.GeneralConstants;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;
import java.io.File;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;


/**
 * Implementació amb els mètodes del sistema de seguretat
 */
public class SecurityManager implements SecurityAPI {

    private SecureRandom r = new SecureRandom();
    //byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes) throws Throwable{
        try{
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = null;
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, generateIV(ivBytes));
            return cipher.doFinal(textBytes);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes) throws Throwable{
        try{
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, generateIV(ivBytes));
            return cipher.doFinal(textBytes);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveEncryptedImage(byte[] ivBytes, Bitmap image, String imagePath, byte[] keyBytes) throws Throwable {
        ByteArrayOutputStream baos = null;
        FileOutputStream stream = null;
        try{
            baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap object
            byte[] b = baos.toByteArray();
            byte[] encryptedData = encrypt(ivBytes, keyBytes, b);
            File file = new File(imagePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            stream = new FileOutputStream(imagePath);
            stream.write(encryptedData);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            //baos.close();
            stream.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap loadEncryptedImage(byte[] ivBytes, File image, byte[] keyBytes) throws Throwable {
        FileInputStream fis = null;
        try{
            byte[] bytesArray = new byte[(int) image.length()];
            fis = new FileInputStream(image);
            fis.read(bytesArray);
            byte[] decryptedImage = decrypt(ivBytes, keyBytes, bytesArray);
            return BitmapFactory.decodeByteArray(decryptedImage, 0, decryptedImage.length);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            fis.close();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap loadEncryptedImageFullScreen(byte[] ivBytes, File image, byte[] keyBytes, AppCompatActivity context) throws Throwable {
        FileInputStream fis = null;
        try{
            byte[] bytesArray = new byte[(int) image.length()];
            fis = new FileInputStream(image);
            fis.read(bytesArray);
            byte[] decryptedImage = decrypt(ivBytes, keyBytes, bytesArray);
            Bitmap bmp = BitmapFactory.decodeByteArray(decryptedImage, 0, decryptedImage.length);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int targetW = size.x;
            int targetH = size.y;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;

            int photoW = size.x;
            int photoH = size.y;

            if(bmp.getWidth()<bmp.getHeight()){
                photoW = bmp.getWidth();
                photoH = bmp.getHeight();
            }else{
                photoW = bmp.getHeight();
                photoH = bmp.getWidth();
            }

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            return BitmapFactory.decodeByteArray(decryptedImage, 0, decryptedImage.length, bmOptions);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            fis.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap loadEncryptedImageFullScreenLandscape(byte[] ivBytes, File image, byte[] keyBytes, AppCompatActivity context) throws Throwable {
        FileInputStream fis = null;
        try{
            byte[] bytesArray = new byte[(int) image.length()];
            fis = new FileInputStream(image);
            fis.read(bytesArray);
            byte[] decryptedImage = decrypt(ivBytes, keyBytes, bytesArray);
            Bitmap bmp = BitmapFactory.decodeByteArray(decryptedImage, 0, decryptedImage.length);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int targetW = size.y;
            int targetH = size.x;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;

            int photoW = size.y;
            int photoH = size.x;

            if(bmp.getWidth()<bmp.getHeight()){
                photoW = bmp.getWidth();
                photoH = bmp.getHeight();
            }else{
                photoW = bmp.getHeight();
                photoH = bmp.getWidth();
            }

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            return BitmapFactory.decodeByteArray(decryptedImage, 0, decryptedImage.length, bmOptions);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            fis.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IvParameterSpec generateIV(byte[] byteIV) throws Throwable {
        try{
            return new IvParameterSpec(byteIV);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateBytesForIV() throws Throwable {
        //https://codereview.stackexchange.com/questions/85396/encrypting-a-string-using-aes-cbc-pkcs5padding
        try{
            byte[] newSeed = r.generateSeed(16);
            r.setSeed(newSeed);
            byte[] byteIV = new byte[16];
            r.nextBytes(byteIV);
            return byteIV;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SecretKey generateDatabaseKey(char[] userKey) throws Throwable {
        try{
            final byte[] salt = GeneralConstants.salt.getBytes();
            // Number of PBKDF2 hardening rounds to use. Larger values increase
            // computation time. You should select a value that causes computation
            // to take >100ms.
            final int iterations = 1000;
            // Generate a 256-bit key
            final int outputKeyLength = 256;
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(userKey, salt, iterations, outputKeyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            return secretKey;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateAESKey() throws Throwable {
        // http://stackoverflow.com/questions/28622438/aes-256-password-based-encryption-decryption-in-java
        //https://codereview.stackexchange.com/questions/85396/encrypting-a-string-using-aes-cbc-pkcs5padding
        try{
            byte[] newSeed = r.generateSeed(32);
            r.setSeed(newSeed);
            KeyGenerator keyGen = KeyGenerator.getInstance("AES"); // A
            keyGen.init(256, r); // Initialize Random Number Generator
            return secretKeyToString(keyGen.generateKey());
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SecretKey stringToSecretKey(String stringKey) throws Throwable {
        try{
            byte[] encodedKey     = Base64.decode(stringKey, Base64.DEFAULT);
            return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String secretKeyToString(SecretKey secretKey) throws Throwable {
        try{
            return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generatePublicPrivateKeys() throws Throwable {
        try{
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            Key pub = kp.getPublic();
            Key pvt = kp.getPrivate();
            savePublicKeyToFile(pub);
            return Base64.encodeToString(pvt.getEncoded(), Base64.DEFAULT);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKey getPublicKey() throws Throwable {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;
        try {
            if(GeneralConstants.publicKey!=null){
                return GeneralConstants.publicKey;
            }else{
                File file = new File(Environment.getExternalStorageDirectory().getPath().toString() +  GeneralConstants.publicKeyPath);
                if(file.exists()){
                    bytesArray = new byte[(int) file.length()];
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(bytesArray);
                    X509EncodedKeySpec ks = new X509EncodedKeySpec(bytesArray);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    GeneralConstants.publicKey = kf.generatePublic(ks);
                    return GeneralConstants.publicKey;
                }else{
                    return null;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrivateKey(String privateKey) throws Throwable {
        try{
            byte[] encodedPrivateKey = Base64.decode(privateKey, Base64.DEFAULT);
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(encodedPrivateKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            GeneralConstants.privateKey =  kf.generatePrivate(ks);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encryptTextRSA(String msg, PublicKey key) throws Throwable {
        try{
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeToString(cipher.doFinal(msg.getBytes("UTF-8")), Base64.DEFAULT);
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decryptTextRSA(String msg, PrivateKey key) throws Throwable {
        try{
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.decode(msg, Base64.DEFAULT)));
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encryptFileRSA(byte[] input, File output, PublicKey key)throws Throwable {
        try{
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            writeToFile(output, cipher.doFinal(input));
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decryptFileRSA(byte[] input, File output, PrivateKey key) throws Throwable {
        try{
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            writeToFile(output, cipher.doFinal(input));
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    private void writeToFile(File output, byte[] toWrite) throws Throwable {
        try{
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(toWrite);
            fos.flush();
            fos.close();
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }


    private void savePublicKeyToFile(Key publicKey) throws Throwable {
        FileOutputStream out = null;
        try{
            File path = new File(Environment.getExternalStorageDirectory().getPath().toString() + "/CriptoFoto");
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    throw new Throwable("Error creant el directori CriptoFoto");
                }
            }
            out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath().toString() +  GeneralConstants.publicKeyPath);
            out.write(publicKey.getEncoded());
            out.close();
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

}
