package oth.ics.wtp.relaybackend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Password hashing and Basic Auth utilities.
 * Passwords are hashed with BCrypt (work factor 12), which is salted and
 * slow by design — making brute-force attacks computationally infeasible.
 */
public class WeakCrypto {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * Hashes a plain-text password with BCrypt.
     * Each call produces a different hash (BCrypt embeds a random salt).
     */
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        return encoder.matches(rawPassword, storedHash);
    }

    public static String base64decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), UTF_8);
    }

    public static String base64encode(String plainText) {
        return Base64.getEncoder().encodeToString(plainText.getBytes(UTF_8));
    }

    public static String createBasicAuthHeader(String username, String password) {
        return "Basic " + base64encode(username + ":" + password);
    }
}
