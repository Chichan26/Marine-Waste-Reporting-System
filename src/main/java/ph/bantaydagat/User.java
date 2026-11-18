// FILE 13 of 28: src/main/java/ph/bantaydagat/User.java
// [F13-B01] Package and dependencies
package ph.bantaydagat;

import java.io.Serializable; // [F13-B01] Accounts are stored in data.ser
import java.security.GeneralSecurityException; // [F13-B01] Wrap unavailable crypto algorithms
import java.security.MessageDigest; // [F13-B01] Constant-time hash comparison
import java.security.SecureRandom; // [F13-B01] Individual password salts
import java.util.List; // [F13-B01] Permission contract
import java.util.UUID; // [F13-B01] Stable account ID
import javax.crypto.SecretKeyFactory; // [F13-B01] Standard Java PBKDF2
import javax.crypto.spec.PBEKeySpec; // [F13-B01] Password hashing parameters

public abstract class User implements Serializable {
    // [F13-B02] Identity and role permissions
    private static final long serialVersionUID = 1L;
    public static final String REPORT = "report";
    public static final String MY_REPORTS = "my_reports";
    public static final String MANAGE = "manage";
    private final String id = UUID.randomUUID().toString();
    private final String username;

    // [F13-B03] Store salted hash bytes only
    private final byte[] salt = new byte[16];
    private final byte[] passwordHash;

    // [F13-B04] Initialize account and hash its password
    protected User(String username, String password) {
        this.username = username;
        new SecureRandom().nextBytes(salt);
        passwordHash = hash(password);
    }

    // [F13-B05] Recompute the hash using this account's salt
    private byte[] hash(String password) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Java password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }

    // [F13-B06] Verify login credentials
    public final boolean matchesPassword(String password) {
        return password != null && MessageDigest.isEqual(passwordHash, hash(password));
    }

    // [F13-B07] Stable reporter ID
    public final String getId() { return id; }
    // [F13-B08] Account username
    public final String getUsername() { return username; }
    // [F13-B09] Subclasses define their role permissions
    public abstract List<String> getPermissions();
}

