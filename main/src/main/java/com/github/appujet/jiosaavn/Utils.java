package com.github.appujet.jiosaavn;


import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Utils {

    private static final String KEY = "38346591";
    private static final String ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";
    
    /**
     * Generates a download link by decrypting the encrypted media URL.
     *
     * @param encryptedMediaUrl The encrypted media URL.
     * @return The download link or null if not valid.
     */
    public static String decryptUrl(String url) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(url);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new FriendlyException("Failed to decrypt URL", FriendlyException.Severity.COMMON, e);
        }
    }
}
