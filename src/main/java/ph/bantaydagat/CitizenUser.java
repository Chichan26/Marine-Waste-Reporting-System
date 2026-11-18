// FILE 14 of 28: src/main/java/ph/bantaydagat/CitizenUser.java
// [F14-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F14-B01] Immutable permissions list

public final class CitizenUser extends User {
    // [F14-B02] Serialized citizen account version
    private static final long serialVersionUID = 1L;

    // [F14-B03] Initialize the base account
    public CitizenUser(String username, String password) {
        super(username, password); // [F14-B03] User has no zero-argument constructor
    }

    // [F14-B04] Citizens can report and view their own reports
    @Override
    public List<String> getPermissions() {
        return List.of(REPORT, MY_REPORTS);
    }
}

