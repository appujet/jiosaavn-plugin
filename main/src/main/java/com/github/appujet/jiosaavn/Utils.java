package com.github.appujet.jiosaavn;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class Utils {

    private static final String KEY = "38346591";
    private static final String ALGORITHM = "DES/ECB/NoPadding";
    private static final String[] QUALITIES = { "_320", "_160", "_96", "_48", "_12" };
    
    /**
     * Generates a download link by decrypting the encrypted media URL.
     *
     * @param encryptedMediaUrl The encrypted media URL.
     * @return The download link or null if not valid.
     */
    public static String getDownloadLink(String encryptedMediaUrl) {
        if (encryptedMediaUrl == null || encryptedMediaUrl.isEmpty())
            return null;

        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMediaUrl);
            Key key = new SecretKeySpec(KEY.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedLink = new String(decryptedBytes).trim();

            for (String quality : QUALITIES) {
                String downloadUrl = decryptedLink.replace("_96", quality);
                if (isValidUrl(downloadUrl)) {
                    return downloadUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to check if a URL is valid
    private static boolean isValidUrl(String url) {
        return url.startsWith("https://aac.saavncdn.com/") && url.endsWith(".mp4");
    }
}
