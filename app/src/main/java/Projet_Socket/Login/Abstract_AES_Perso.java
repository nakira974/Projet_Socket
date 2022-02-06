package Projet_Socket.Login;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.US_ASCII;

public abstract class Abstract_AES_Perso {
    protected static final byte[] SALTED = "Salted__".getBytes(US_ASCII);

    /**
     * Internal encrypt function
     *
     * @param input      Input text to encrypt
     * @param passphrase The passphrase
     * @return Encrypted data
     * @throws Exception Throws exceptions
     */
    @NotNull
    protected static byte[] _encrypt(byte[] input, byte[] passphrase) throws Exception {
        var salt = (new SecureRandom()).generateSeed(8);
        var keyIv = deriveKeyAndIv(passphrase, salt);

        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec((byte[]) keyIv[0], "AES"), new IvParameterSpec((byte[]) keyIv[1]));

        var enc = cipher.doFinal(input);
        return concat(concat(SALTED, salt), enc);
    }

    /**
     * Internal decrypt function
     *
     * @param data       Text in bytes to decrypt
     * @param passphrase The passphrase
     * @return Decrypted data in bytes
     * @throws Exception Throws exceptions
     */
    protected static byte[] _decrypt(byte[] data, byte[] passphrase) throws Exception {
        var salt = Arrays.copyOfRange(data, 8, 16);

        if (!Arrays.equals(Arrays.copyOfRange(data, 0, 8), SALTED)) {
            throw new IllegalArgumentException("Invalid crypted data");
        }

        var keyIv = deriveKeyAndIv(passphrase, salt);

        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec((byte[]) keyIv[0], "AES"), new IvParameterSpec((byte[]) keyIv[1]));
        return cipher.doFinal(data, 16, data.length - 16);
    }

    /**
     * Derive key and iv
     *
     * @param passphrase Passphrase
     * @param salt       Salt
     * @return Array of key and iv
     * @throws Exception Throws exceptions
     */
    @NotNull
    @Contract("_, _ -> new")
    protected static Object[] deriveKeyAndIv(byte[] passphrase, byte[] salt) throws Exception {
        var md5 = MessageDigest.getInstance("MD5");
        var passSalt = concat(passphrase, salt);
        var dx = new byte[0];
        var di = new byte[0];

        for (var i = 0; i < 3; i++) {
            di = md5.digest(concat(di, passSalt));
            dx = concat(dx, di);
        }

        return new Object[]{Arrays.copyOfRange(dx, 0, 32), Arrays.copyOfRange(dx, 32, 48)};
    }

    /**
     * Concatenate bytes
     *
     * @param a First array
     * @param b Second array
     * @return Concatenated bytes
     */
    @NotNull
    protected static byte[] concat(@NotNull byte[] a, @NotNull byte[] b) {
        var c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}