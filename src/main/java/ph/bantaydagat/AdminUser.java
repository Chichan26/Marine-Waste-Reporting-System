// FILE 15 of 28: src/main/java/ph/bantaydagat/AdminUser.java
// [F15-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F15-B01] Immutable permissions list

public final class AdminUser extends User {
    // [F15-B02] Serialized administrator account version
    private static final long serialVersionUID = 1L;

    // [F15-B03] Initialize the base account
    public AdminUser(String username, String password) {
        super(username, password); // [F15-B03] User has no zero-argument constructor
    }

    // [F15-B04] Administrators can manage the incident queue
    @Override
    public List<String> getPermissions() {
        return List.of(MANAGE);
    }
}

