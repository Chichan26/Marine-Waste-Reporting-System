# DEBUGGING GUIDE

| Label | What this block does | What error appears if its line is deleted | How to fix it |
|---|---|---|---|
| F01-B03 / F01-B04 | Adds JavaFX and identifies `Main` | Missing JavaFX packages or `javafx:run` cannot find the main class | Restore the controls dependency or the `<mainClass>ph.bantaydagat.Main</mainClass>` element |
| F04-B05 | Defines the next state for each status | Removing a case makes the switch expression non-exhaustive | Restore the missing `case`, including `case RESOLVED -> null;` for the final state |
| F07-B03 / F10-B06 | Rejects out-of-range and non-finite coordinates | An invalid report may be accepted, or a partial expression will not compile | Restore both inclusive range checks and both `Double.isFinite` checks |
| F10-B02 / F13-B02 | Declares serialization versions for the base classes | A restart with an existing save may fail with `InvalidClassException` | Restore the original `private static final long serialVersionUID = 1L;` |
| F10-B05 | Validates and assigns the first status | A report bypasses validation or enters the queue with a null status | Restore `validate();` and `status = Status.SUBMITTED;` in that order |
| F11-B03 / F12-B03 | Initializes the inherited report fields | `constructor Report ... cannot be applied to given types` | Restore the labeled `super(location, severity, reporterId, category, description);` as the first constructor statement |
| F13-B04 / F13-B06 | Initializes and compares the salted password hash | A final field may be uninitialized or valid logins may stop working | Restore the hash assignment or the return using `MessageDigest.isEqual` |
| F14-B04 / F15-B04 | Assigns permissions for each role | Missing method implementation error or the wrong tabs open | Restore `getPermissions()` and its role's `List.of(...)` |
| F16-B04 / F16-B23 | Loads previously saved reports and accounts | Existing data disappears from the app after restart or valid accounts cannot sign in | Restore `load();` and read reports first, then users |
| F16-B09 / F16-B11 / F16-B15 | Saves registration, reporting and status changes | A change disappears after restart. If a checked `catch` is left empty, compilation fails | Restore `save();` inside the labeled `try` block |
| F16-B12 | Rejects skipping, repeating or reversing statuses | Illegal transitions may succeed | Restore the comparison with `current.next()` and the exception |
| F16-B14 / F16-B15 | Persists cleanup notes and before/after photo paths with the resolved status | A report may remain dispatched or lose cleanup details | Restore `persistStatus(report, Status.RESOLVED, log);` and the save call |
| F16-B16 | Copies a selected file to `uploads/` | A copy may never appear. Removing only the call also leaves an invalid checked `catch` | Restore `Files.copy(source, uploads.resolve(name));` |
| F16-B21 | Writes both lists in load order | `EOFException` or a list type mismatch on restart | Restore `out.writeObject(reports);` followed by `out.writeObject(users);` |
| F17-B04 | Adds the new citizen to the shared account store | Registration reports success but login fails | Restore `manager.addUser(citizen);` |
| F21-B05 / F22-B06 | Connects buttons to login, registration, guest mode and submission | The affected button appears but does nothing | Restore its `setOnAction(...)` line |
| F22-B09 / F23-B04 | Saves a report and refreshes the correct citizen's history | A report never saves or the new row does not appear | Restore the manager call and `onSaved.run();`. Keep filtering by `citizenId` |
| F24-B12 / F24-B18 | Refreshes the filtered queue and dashboard after updates | The table or chart still shows the old status | Restore the table refresh and `onChanged.run();` |
| F25-B05 / F25-B07 | Updates the category chart | The category bars are missing or stale | Restore the update call and `categoryChart.getData().add(series);` |
| F27-B04 | Attaches and shows the JavaFX scene | No window appears, or a shown window is blank | Restore `stage.setScene(new Scene(root, 1060, 690));` and `stage.show();` |
| F27-B06 / F27-B08 | Keeps guest reports anonymous and citizen history private to the signed-in account | Wrong tabs appear or reports link to the wrong account | Pass `null` for a guest and `user.getId()` for a citizen |
| F28-B03 | Prints numbered source lines | The utility runs but prints no source | Restore the `System.out.printf(...)` call inside the loop |

## COMMON DELETED-LINE ERRORS

1. **Missing import.** A class name typically reports `cannot find symbol`. Restore the matching import in the file's `B01` block.
2. **Missing semicolon or brace.** Look just above the first compiler error. A missing semicolon often gives `';' expected`. A missing brace can cause `illegal start of expression` or `reached end of file while parsing`.
3. **Missing `super(...)`.** A subclass implicitly calls `super()` if the explicit call is removed. Restore the labeled parent constructor call as the first statement.
4. **Missing `@Override`.** Deleting the annotation alone usually still compiles. Restore it above the method so the compiler checks the method signature.
5. **Missing `serialVersionUID` or `Serializable`.** The former can break compatibility with a prior save and the latter can cause `NotSerializableException`. Restore the exact version or the `implements Serializable` declaration in the class being stored.
6. **Missing `save()`.** A change might work until restart, or the compiler can flag a checked exception handler that can no longer be reached. Restore `save();` inside the corresponding `try`.
7. **Removed event handler.** A visible button may do nothing. Restore the labeled `setOnAction(...)` line or the resolution dialog's action filter.
8. **Missing window show call.** JavaFX displays a `Stage`. Restore `stage.show();` after `stage.setScene(...)`.

## Verification checklist

- [ ] **Register.** Create a new citizen account, log in, submit a report, and verify that it appears as Submitted in My Reports.
- [ ] **Report anonymously.** Sign out, enter guest mode, and submit a report. Verify in the admin queue that its Reporter is Anonymous. Try latitude 91 first to check validation.
- [ ] **Resolve an incident.** Log in as admin, select the new report, then move it through Submitted  Under Review  Dispatched  Resolved. Enter cleanup notes and select before/after photos. Reopen the row and check its log and photos.
- [ ] **Check the dashboard.** Compare the charts before and after resolution. Active falls by one, Resolved rises by one, and the category total remains the same. A new submission increases its category total.
- [ ] **Restart.** Close the window and rerun `mvn clean javafx:run` from the same folder. Confirm the new account, statuses, notes and copied evidence are still there.

