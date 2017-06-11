package tfm.uoc.edu.criptofoto.security;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Interfície amb els mètodes del sistema de seguretat
 */
public interface SecurityAPI {

    /**
     * Mètode per xifrar les imatges amb AES CBC de 256 bits
     * @throws Throwable
     */
    byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes) throws Throwable;

    /**
     * Mètode per desxifrar les imatges amb AES CBC de 256 bits
     * @throws Throwable
     */
    byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes) throws Throwable;

    /**
     * Mètode per guardar imatge xifrades
     * @throws Throwable
     */
    void saveEncryptedImage(byte[] ivBytes, Bitmap image, String imagePath, byte[] keyBytes)  throws Throwable;

    /**
     * Mètode per obtindre imatges xifrades
     * @throws Throwable
     */
    Bitmap loadEncryptedImage(byte[] ivBytes, File image, byte[] keyBytes)  throws Throwable;

    /**
     * Mètode per obtindre imatges xifrades preparades per a la pantalla completa
     * @throws Throwable
     */
    Bitmap loadEncryptedImageFullScreen(byte[] ivBytes, File image, byte[] keyBytes, AppCompatActivity context) throws Throwable;

    /**
     * Mètode per obtindre imatges xifrades preparades per a la pantalla completa en mode horitzontal
     * @throws Throwable
     */
    Bitmap loadEncryptedImageFullScreenLandscape(byte[] ivBytes, File image, byte[] keyBytes, AppCompatActivity context) throws Throwable;

    /**
     * Mètode per obtindre IV de forma segura
     * @throws Throwable
     */
    IvParameterSpec generateIV(byte[] byteIV) throws Throwable;

    /**
     * Mètode per obtindre bytes per als IV de forma segura
     * @throws Throwable
     */
    public byte[] generateBytesForIV() throws Throwable;

    /**
     * Mètode per generar la clau de la base de dades
     * @throws Throwable
     */
    SecretKey generateDatabaseKey(char[] userKey) throws Throwable;

    /**
     * Mètode per generar claus de tipus AES per als repositoris o les imatges del registre d'intrusions
     * @throws Throwable
     */
    String generateAESKey() throws Throwable;

    /**
     * Mètode per convertir una String en una SecretKey
     * @throws Throwable
     */
    SecretKey stringToSecretKey(String stringKey) throws Throwable;

    /**
     * Mètode per convertir una SecretKey en una String
     * @throws Throwable
     */
    String secretKeyToString(SecretKey secretKey) throws Throwable;

    /**
     * Mètode per genera les claus del criptosistema asimetric
     * @throws Throwable
     */
    String generatePublicPrivateKeys() throws Throwable;

    /**
     * Mètode per obtindre la clau publica del criptosistema asimetric
     * @throws Throwable
     */
    PublicKey getPublicKey() throws Throwable;

    /**
     * Mètode per desar la clau privada del criptosistema asimetric
     * @throws Throwable
     */
    void setPrivateKey(String privateKey) throws Throwable;

    /**
     * Mètode per encriptar text mitjançant la clau pública del criptosistema asimetric
     * @throws Throwable
     */
    String encryptTextRSA(String msg, PublicKey key) throws Throwable;

    /**
     * Mètode per desencriptar text mitjançant la clau privada del criptosistema asimetric
     * @throws Throwable
     */
    String decryptTextRSA(String msg, PrivateKey key) throws Throwable;

    /**
     * Mètode per encriptar un fitxer mitjançant la clau pública del criptosistema asimetric
     * @throws Throwable
     */
    void encryptFileRSA(byte[] input, File output, PublicKey key)throws Throwable;

    /**
     * Mètode per desencriptar un fitxer mitjançant la clau privada del criptosistema asimetric
     * @throws Throwable
     */
    void decryptFileRSA(byte[] input, File output, PrivateKey key) throws Throwable;

}
