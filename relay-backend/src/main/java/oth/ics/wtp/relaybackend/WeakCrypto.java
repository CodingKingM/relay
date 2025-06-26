package oth.ics.wtp.relaybackend;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WeakCrypto {
    private static final String SHA_256 = "SHA-256";
    private static MessageDigest digest;

    public static String base64decode(String authHeader) {
        return new String(Base64.getDecoder().decode(authHeader), UTF_8);
    }

    public static synchronized String hashPassword(String password) {
        try {
            if (digest == null) {
                digest = MessageDigest.getInstance(SHA_256);
            }
            digest.reset();
            byte[] hashedBytes = digest.digest(password.getBytes(UTF_8));
            return new String(hashedBytes, UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public static String base64encode(String plainText) {
        return Base64.getEncoder().encodeToString(plainText.getBytes(UTF_8));
    }

    public static String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + base64encode(credentials);
    }
}