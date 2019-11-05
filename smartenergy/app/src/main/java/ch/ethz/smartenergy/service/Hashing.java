package ch.ethz.smartenergy.service;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hashing {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    private static byte[] currentSalt;

    static String encrypt(String value) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(currentSalt, HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            return bytesToBase64(mac.doFinal(value.getBytes()));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void generateKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        currentSalt =  bytes;
    }

    private static String bytesToBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
