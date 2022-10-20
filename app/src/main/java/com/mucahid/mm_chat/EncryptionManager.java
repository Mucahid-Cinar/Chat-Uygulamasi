
package com.mucahid.mm_chat;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.NO_WRAP;

import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionManager {

    private static EncryptionManager instance = null;

    MessageDigest digest = null;
    String hash;

    private EncryptionManager() {

    }

    public static EncryptionManager getInstance() {
        if(instance == null){
            instance = new EncryptionManager();
        }
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String plain, String token) {

        try {

            byte[] EncryptionKey = new byte[16];
            new SecureRandom().nextBytes(EncryptionKey);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8),
                            "AES"), new IvParameterSpec(EncryptionKey));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] ivAndCipherText = getCombinedArray(EncryptionKey, cipherText);
            return android.util.Base64.encodeToString(ivAndCipherText,NO_WRAP);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("77777777777" + e.getMessage());
            return "şifrelenemedi";
        }
    }

    public static String decrypt(String encode, String token) {

        try {

            byte [] ivAndCipherText = Base64.decode(encode, NO_WRAP);
            byte [] iv = Arrays.copyOfRange(ivAndCipherText, 0,16);
            byte [] cipherText = Arrays.copyOfRange(ivAndCipherText, 16,ivAndCipherText.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new
                            SecretKeySpec(token.getBytes(StandardCharsets.UTF_8),"AES"),
                    new IvParameterSpec(iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("?????????????" + e.getMessage());
            return "!!!!!!!!!!!!!!";
        }

    }

    private static byte[] getCombinedArray(byte[] one, byte[] two) {

        byte[] combined = new byte[one.length + two.length];
        for (int i=0; i< combined.length; i++){

            combined[i] = i < one.length ? one[i] : two[i - one.length];

        }
        return combined;
    }

    private static String bytesToHexString(byte[] bytes) {

        StringBuffer sb = new StringBuffer();
        for (int i=0; i < bytes.length; i++){

            String hex = Integer.toHexString(0xFF & bytes[i]);
            if(hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public String getSHA256(String data) {

        try {

            digest = MessageDigest.getInstance("SHA-256");
            digest.update(data.getBytes());

            hash = bytesToHexString(digest.digest());
            return hash;
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
        return "";
    }

    String encodeBase64(String data) {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return android.util.Base64.encodeToString(data.getBytes(StandardCharsets.UTF_8), NO_WRAP);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    String decodeBase64(String data) {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return new String(android.util.Base64.decode(data, NO_WRAP), StandardCharsets.UTF_8);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}