package bibliotheque.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    public static String hasher(String motDePasse) {
        return ENCODER.encode(motDePasse);
    }

    public static boolean verifier(String motDePasse, String hash) {
        if (motDePasse == null || hash == null) return false;
        try {
            return ENCODER.matches(motDePasse, hash);
        } catch (Exception e) {
            return false;
        }
    }

    private PasswordUtil() {}
}