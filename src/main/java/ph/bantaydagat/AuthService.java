// FILE 17 of 28: src/main/java/ph/bantaydagat/AuthService.java
// [F17-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F17-B01] Registration may fail to save
import java.util.Locale; // [F17-B01] Consistent case-insensitive usernames
import java.util.Optional; // [F17-B01] A login may have no matching account

public final class AuthService {
    // [F17-B02] Shared account store
    private final IncidentManager manager;

    // [F17-B03] Connect authentication to the incident store
    public AuthService(IncidentManager manager) {
        this.manager = manager;
    }

    // [F17-B04] Register a citizen account
    public CitizenUser register(String username, String password) throws IOException {
        String name = normalize(username);
        if (!name.matches("[a-z0-9_]{3,24}")) {
            throw new IllegalArgumentException("Use 3-24 letters, digits or underscores for the username.");
        }
        if (password == null || password.length() < 8 || password.length() > 128) {
            throw new IllegalArgumentException("Use 8-128 characters for the password.");
        }
        if (findUser(name).isPresent()) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        CitizenUser citizen = new CitizenUser(name, password);
        manager.addUser(citizen); // [F17-B04] Registration persists before reporting success
        return citizen;
    }

    // [F17-B05] Verify credentials and return the actual account subclass
    public Optional<User> login(String username, String password) {
        return findUser(normalize(username)).filter(user -> user.matchesPassword(password));
    }

    // [F17-B06] Look up a normalized username
    private Optional<User> findUser(String name) {
        return manager.getUsers().stream().filter(user -> user.getUsername().equals(name)).findFirst();
    }

    // [F17-B07] Trim and normalize the username
    private String normalize(String username) {
        return username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
    }
}

