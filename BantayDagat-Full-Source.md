# BantayDagat - Full Source

Every file is printed in full. The headings use dependency order for reading. Maven compiles the Java sources together.

### FILE 01 of 28: pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- FILE 01 of 28: pom.xml -->
<!-- [F01-B01] Maven project and artifact coordinates -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>ph.bantaydagat</groupId>
    <artifactId>bantaydagat</artifactId>
    <version>1.0.0</version>

    <!-- [F01-B02] Java release and JavaFX dependency version -->
    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>17.0.17</javafx.version>
    </properties>

    <!-- [F01-B03] JavaFX controls, tables and charts -->
    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
    </dependencies>

    <!-- [F01-B04] Compile for Java 17 and point javafx:run to Main -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>ph.bantaydagat.Main</mainClass> <!-- [F01-B04] JavaFX entry point -->
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Model

### FILE 02 of 28: src/main/java/ph/bantaydagat/WasteCategory.java

```java
// FILE 02 of 28: src/main/java/ph/bantaydagat/WasteCategory.java
// [F02-B01] Package and dependencies
package ph.bantaydagat;

public enum WasteCategory {
    // [F02-B02] Values used by the form, filters and chart
    PLASTIC("Plastic"), FISHING_GEAR("Fishing gear"), OIL_CHEMICAL("Oil / chemical"),
    GLASS_METAL("Glass / metal"), ORGANIC("Organic"), OTHER("Other");

    // [F02-B03] Display label
    private final String label;

    // [F02-B04] Constructor
    WasteCategory(String label) {
        this.label = label;
    }

    // [F02-B05] Text shown in JavaFX controls
    @Override
    public String toString() {
        return label;
    }
}
```

### FILE 03 of 28: src/main/java/ph/bantaydagat/Severity.java

```java
// FILE 03 of 28: src/main/java/ph/bantaydagat/Severity.java
// [F03-B01] Package and dependencies
package ph.bantaydagat;

public enum Severity {
    // [F03-B02] Order matters when sorting from highest to lowest
    LOW("Low"), MEDIUM("Medium"), HIGH("High"), CRITICAL("Critical");

    // [F03-B03] Display label
    private final String label;

    // [F03-B04] Constructor
    Severity(String label) {
        this.label = label;
    }

    // [F03-B05] Text shown in JavaFX controls
    @Override
    public String toString() {
        return label;
    }
}
```

### FILE 04 of 28: src/main/java/ph/bantaydagat/Status.java

```java
// FILE 04 of 28: src/main/java/ph/bantaydagat/Status.java
// [F04-B01] Package and dependencies
package ph.bantaydagat;

public enum Status {
    // [F04-B02] Incident workflow states
    SUBMITTED("Submitted"), UNDER_REVIEW("Under Review"),
    DISPATCHED("Dispatched"), RESOLVED("Resolved");

    // [F04-B03] Display label
    private final String label;

    // [F04-B04] Constructor
    Status(String label) {
        this.label = label;
    }

    // [F04-B05] The only legal next state
    public Status next() {
        return switch (this) {
            case SUBMITTED -> UNDER_REVIEW;
            case UNDER_REVIEW -> DISPATCHED;
            case DISPATCHED -> RESOLVED;
            case RESOLVED -> null;
        }; // [F04-B05] Switch expression needs its semicolon
    }

    // [F04-B06] Text shown in JavaFX controls
    @Override
    public String toString() {
        return label;
    }
}
```

### FILE 05 of 28: src/main/java/ph/bantaydagat/InvalidReportException.java

```java
// FILE 05 of 28: src/main/java/ph/bantaydagat/InvalidReportException.java
// [F05-B01] Package and dependencies
package ph.bantaydagat;

public class InvalidReportException extends Exception {
    // [F05-B02] Serialization version inherited from Exception
    private static final long serialVersionUID = 1L;

    // [F05-B03] Pass the validation message to Exception
    public InvalidReportException(String message) {
        super(message); // [F05-B03] Without this line the UI loses the message
    }
}
```

### FILE 06 of 28: src/main/java/ph/bantaydagat/InvalidStatusTransitionException.java

```java
// FILE 06 of 28: src/main/java/ph/bantaydagat/InvalidStatusTransitionException.java
// [F06-B01] Package and dependencies
package ph.bantaydagat;

public class InvalidStatusTransitionException extends Exception {
    // [F06-B02] Serialization version inherited from Exception
    private static final long serialVersionUID = 1L;

    // [F06-B03] Describe the attempted status jump
    public InvalidStatusTransitionException(Status from, Status to) {
        super("Cannot move from " + from + " to " + to + ".");
    }
}
```

### FILE 07 of 28: src/main/java/ph/bantaydagat/Location.java

```java
// FILE 07 of 28: src/main/java/ph/bantaydagat/Location.java
// [F07-B01] Package and dependencies
package ph.bantaydagat;

import java.io.Serializable; // [F07-B01] Required inside a serialized Report
import java.util.Locale; // [F07-B01] Format coordinates consistently

public record Location(double latitude, double longitude) implements Serializable {
    // [F07-B02] Version for the stored coordinate pair
    private static final long serialVersionUID = 1L;

    // [F07-B03] Bounds check also rejects NaN and infinity
    public boolean isValid() {
        return Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
                && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180;
    }

    // [F07-B04] Readable coordinate pair for tables and export
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%.6f, %.6f", latitude, longitude);
    }
}
```

### FILE 08 of 28: src/main/java/ph/bantaydagat/ResolutionLog.java

```java
// FILE 08 of 28: src/main/java/ph/bantaydagat/ResolutionLog.java
// [F08-B01] Package and dependencies
package ph.bantaydagat;

import java.io.Serializable; // [F08-B01] The report stores its cleanup log
import java.time.LocalDateTime; // [F08-B01] Record the cleanup time

public record ResolutionLog(LocalDateTime resolvedAt, String notes,
                            String beforePhotoPath, String afterPhotoPath) implements Serializable {
    // [F08-B02] Stored fields come from the record header
    private static final long serialVersionUID = 1L;
}
```

### FILE 09 of 28: src/main/java/ph/bantaydagat/Reportable.java

```java
// FILE 09 of 28: src/main/java/ph/bantaydagat/Reportable.java
// [F09-B01] Package and dependencies
package ph.bantaydagat;

public interface Reportable {
    // [F09-B02] Submission and validation contract
    void submit() throws InvalidReportException;
    void validate() throws InvalidReportException;

    // [F09-B03] Build a plain text report summary
    String exportReport();
}
```

### FILE 10 of 28: src/main/java/ph/bantaydagat/Report.java

```java
// FILE 10 of 28: src/main/java/ph/bantaydagat/Report.java
// [F10-B01] Package and dependencies
package ph.bantaydagat;

import java.io.Serializable; // [F10-B01] Makes subclasses eligible for data.ser
import java.time.LocalDateTime; // [F10-B01] Submission time
import java.util.UUID; // [F10-B01] Unique report identifier

public abstract class Report implements Reportable, Serializable {
    // [F10-B02] Persistent identity and report content
    private static final long serialVersionUID = 1L;
    private final String reportID = UUID.randomUUID().toString();
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final Location location;
    private final Severity severityLevel;
    private final String reporterId;
    private final WasteCategory category;
    private final String description;

    // [F10-B03] Workflow state and cleanup log
    private Status status;
    private ResolutionLog resolutionLog;

    // [F10-B04] Constructor shared by both report types
    protected Report(Location location, Severity severityLevel, String reporterId,
                     WasteCategory category, String description) {
        this.location = location;
        this.severityLevel = severityLevel;
        this.reporterId = reporterId;
        this.category = category;
        this.description = description;
    }

    // [F10-B05] Validate and enter the queue once
    @Override
    public final void submit() throws InvalidReportException {
        if (status != null) {
            throw new InvalidReportException("This report has already been submitted.");
        }
        validate(); // [F10-B05] Calls TextReport or MediaReport validation
        status = Status.SUBMITTED; // [F10-B05] The first state after successful validation
    } // [F10-B05] Ends submit()

    // [F10-B06] Check fields shared by both report types
    protected final void validateFields() throws InvalidReportException {
        if (category == null || severityLevel == null) {
            throw new InvalidReportException("Select a waste category and severity.");
        }
        if (location == null || !location.isValid()) {
            throw new InvalidReportException("Use latitude -90 to 90 and longitude -180 to 180.");
        }
    }

    // [F10-B07] Export the report and any attached details as plain text
    @Override
    public String exportReport() {
        String summary = "Report ID: " + reportID + "\nSubmitted: " + timestamp
                + "\nLocation: " + location + "\nCategory: " + category
                + "\nSeverity: " + severityLevel + "\nStatus: " + status
                + "\nReporter: " + (reporterId == null ? "Anonymous" : reporterId)
                + "\nDescription: " + description;
        if (getMediaPath() != null) {
            summary += "\nAttachment: " + getMediaPath();
        }
        return summary + exportResolution();
    }

    // [F10-B08] Include cleanup notes and stored photo paths when resolved
    private String exportResolution() {
        if (resolutionLog == null) {
            return "";
        }
        return "\nResolved: " + resolutionLog.resolvedAt() + "\nCleanup notes: "
                + resolutionLog.notes() + "\nBefore photo: " + photoLabel(resolutionLog.beforePhotoPath())
                + "\nAfter photo: " + photoLabel(resolutionLog.afterPhotoPath());
    }

    // [F10-B09] Represent an optional photo consistently
    private String photoLabel(String path) {
        return path == null ? "None" : path;
    }

    // [F10-B10] IncidentManager applies or rolls back the workflow state
    final void setWorkflow(Status status, ResolutionLog resolutionLog) {
        this.status = status;
        this.resolutionLog = resolutionLog;
    }

    // [F10-B11] Default for a report with no attachment
    public String getMediaPath() { return null; }

    // [F10-B12] Read report ID
    public String getReportID() { return reportID; }
    // [F10-B13] Read time
    public LocalDateTime getTimestamp() { return timestamp; }
    // [F10-B14] Read coordinates
    public Location getLocation() { return location; }
    // [F10-B15] Read severity
    public Severity getSeverityLevel() { return severityLevel; }
    // [F10-B16] Read workflow state
    public Status getStatus() { return status; }
    // [F10-B17] Read reporter ID (null for anonymous)
    public String getReporterId() { return reporterId; }
    // [F10-B18] Read waste category
    public WasteCategory getCategory() { return category; }
    // [F10-B19] Read description
    public String getDescription() { return description; }
    // [F10-B20] Read cleanup log
    public ResolutionLog getResolutionLog() { return resolutionLog; }
}
```

### FILE 11 of 28: src/main/java/ph/bantaydagat/TextReport.java

```java
// FILE 11 of 28: src/main/java/ph/bantaydagat/TextReport.java
// [F11-B01] Package and dependencies
package ph.bantaydagat;

public final class TextReport extends Report {
    // [F11-B02] Version for serialized text reports
    private static final long serialVersionUID = 1L;

    // [F11-B03] Pass report fields to the parent class
    public TextReport(Location location, Severity severity, String reporterId,
                      WasteCategory category, String description) {
        super(location, severity, reporterId, category, description); // [F11-B03] Required parent constructor
    }

    // [F11-B04] Text reports require a description
    @Override
    public void validate() throws InvalidReportException {
        validateFields();
        if (getDescription() == null || getDescription().isBlank()) {
            throw new InvalidReportException("Enter a description.");
        }
    }
}
```

### FILE 12 of 28: src/main/java/ph/bantaydagat/MediaReport.java

```java
// FILE 12 of 28: src/main/java/ph/bantaydagat/MediaReport.java
// [F12-B01] Package and dependencies
package ph.bantaydagat;

public final class MediaReport extends Report {
    // [F12-B02] Persist only the path of the copied attachment
    private static final long serialVersionUID = 1L;
    private final String mediaPath;

    // [F12-B03] Initialize the report and its attachment path
    public MediaReport(Location location, Severity severity, String reporterId,
                       WasteCategory category, String description, String mediaPath) {
        super(location, severity, reporterId, category, description); // [F12-B03] Required parent constructor
        this.mediaPath = mediaPath;
    }

    // [F12-B04] Media reports require a description and an upload path
    @Override
    public void validate() throws InvalidReportException {
        validateFields();
        if (getDescription() == null || getDescription().isBlank()) {
            throw new InvalidReportException("Enter a description for the attachment.");
        }
        if (mediaPath == null || !mediaPath.startsWith("uploads/") || mediaPath.length() <= 8) {
            throw new InvalidReportException("Choose a photo or video to attach.");
        }
    }

    // [F12-B05] Return the stored attachment path
    @Override
    public String getMediaPath() { return mediaPath; }
}
```

### FILE 13 of 28: src/main/java/ph/bantaydagat/User.java

```java
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
```

### FILE 14 of 28: src/main/java/ph/bantaydagat/CitizenUser.java

```java
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
```

### FILE 15 of 28: src/main/java/ph/bantaydagat/AdminUser.java

```java
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
```

## Services

### FILE 16 of 28: src/main/java/ph/bantaydagat/IncidentManager.java

```java
// FILE 16 of 28: src/main/java/ph/bantaydagat/IncidentManager.java
// [F16-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F16-B01] File errors reach the UI
import java.io.ObjectInputStream; // [F16-B01] Load serialized lists
import java.io.ObjectOutputStream; // [F16-B01] Save serialized lists
import java.nio.file.AtomicMoveNotSupportedException; // [F16-B01] Portable save fallback
import java.nio.file.Files; // [F16-B01] Copy and save files
import java.nio.file.Path; // [F16-B01] Resolve application paths
import java.nio.file.StandardCopyOption; // [F16-B01] Replace data atomically
import java.time.LocalDateTime; // [F16-B01] Resolution time
import java.util.ArrayList; // [F16-B01] Mutable in-memory data
import java.util.Comparator; // [F16-B01] Queue sorting
import java.util.List; // [F16-B01] Report and user collections
import java.util.Locale; // [F16-B01] Normalize file extensions
import java.util.UUID; // [F16-B01] Upload names omit original filenames

public final class IncidentManager {
    // [F16-B02] One data store and one upload directory
    private final List<Report> reports = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final Path dataFile;
    private final Path uploads;

    // [F16-B03] Supported upload extensions
    private static final List<String> PHOTOS = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final List<String> VIDEOS = List.of(".mp4", ".mov", ".m4v", ".webm");

    // [F16-B04] Initialize from disk or seed once
    public IncidentManager(Path directory) throws IOException {
        Files.createDirectories(directory);
        dataFile = directory.resolve("data.ser");
        uploads = directory.resolve("uploads");
        if (Files.exists(dataFile)) {
            load(); // [F16-B04] Restore saved state on startup
        } else {
            DemoData.seed(reports, users);
            save(); // [F16-B04] Commit the first-run dataset
        }
    }

    // [F16-B05] Return read-only snapshots of collection membership
    public List<Report> getReports() { return List.copyOf(reports); }
    // [F16-B06] AuthService looks up accounts here
    List<User> getUsers() { return List.copyOf(users); }

    // [F16-B07] Citizen history excludes anonymous reports
    public List<Report> reportsByReporter(String reporterId) {
        if (reporterId == null) {
            return List.of();
        }
        return reports.stream().filter(report -> reporterId.equals(report.getReporterId())).toList();
    }

    // [F16-B08] Null filters mean all, then order the result
    public List<Report> filterReports(Severity severity, Status status, WasteCategory category,
                                      Comparator<Report> order) {
        return reports.stream()
                .filter(report -> severity == null || report.getSeverityLevel() == severity)
                .filter(report -> status == null || report.getStatus() == status)
                .filter(report -> category == null || report.getCategory() == category)
                .sorted(order)
                .toList();
    }

    // [F16-B09] Register and save, rolling back a failed write
    void addUser(User user) throws IOException {
        users.add(user);
        try {
            save(); // [F16-B09] Keeps new accounts after a restart
        } catch (IOException e) {
            users.remove(user);
            throw e;
        }
    }

    // [F16-B10] Create the right report type and clean up failed uploads
    public Report submitReport(Location location, Severity severity, String reporterId,
                               WasteCategory category, String description, Path attachment)
            throws IOException, InvalidReportException {
        String mediaPath = copyUpload(attachment, false);
        Report report = mediaPath == null
                ? new TextReport(location, severity, reporterId, category, description)
                : new MediaReport(location, severity, reporterId, category, description, mediaPath);
        try {
            addReport(report); // [F16-B10] Validates and saves to the queue
            return report;
        } catch (IOException | InvalidReportException e) {
            discardUpload(mediaPath, e);
            throw e;
        }
    }

    // [F16-B11] Validate, add and persist the report
    private void addReport(Report report) throws IOException, InvalidReportException {
        report.submit(); // [F16-B11] Sets the initial status after validation
        reports.add(report);
        try {
            save(); // [F16-B11] Persists each submitted report
        } catch (IOException e) {
            reports.remove(report);
            throw e;
        }
    }

    // [F16-B12] Apply exactly one status step and require notes for resolution
    public void updateStatus(Report report, Status target, String notes, Path before, Path after)
            throws IOException, InvalidStatusTransitionException, InvalidReportException {
        if (!reports.contains(report)) {
            throw new IllegalArgumentException("Select an incident from the queue.");
        }
        Status current = report.getStatus();
        if (target == null || current.next() != target) {
            throw new InvalidStatusTransitionException(current, target); // [F16-B12] Reject skips
        }
        if (target == Status.RESOLVED) {
            resolve(report, notes, before, after);
        } else {
            persistStatus(report, target, null);
        }
    } // [F16-B12] Ends updateStatus()

    // [F16-B13] Validate notes and stage the before photo
    private void resolve(Report report, String notes, Path before, Path after)
            throws IOException, InvalidReportException {
        if (notes == null || notes.isBlank()) {
            throw new InvalidReportException("Enter cleanup notes before resolving.");
        }
        String beforePath = copyUpload(before, true);
        try {
            saveResolution(report, notes.strip(), beforePath, after);
        } catch (IOException | InvalidReportException e) {
            discardUpload(beforePath, e);
            throw e;
        }
    }

    // [F16-B14] Save cleanup details and the after photo with the new status
    private void saveResolution(Report report, String notes, String beforePath, Path after)
            throws IOException, InvalidReportException {
        String afterPath = copyUpload(after, true);
        try {
            ResolutionLog log = new ResolutionLog(LocalDateTime.now(), notes, beforePath, afterPath);
            persistStatus(report, Status.RESOLVED, log); // [F16-B14] One save for status and log
        } catch (IOException e) {
            discardUpload(afterPath, e);
            throw e;
        }
    }

    // [F16-B15] Restore the previous workflow state if disk saving fails
    private void persistStatus(Report report, Status target, ResolutionLog log) throws IOException {
        Status previous = report.getStatus();
        ResolutionLog previousLog = report.getResolutionLog();
        report.setWorkflow(target, log);
        try {
            save(); // [F16-B15] Persists status and cleanup log
        } catch (IOException e) {
            report.setWorkflow(previous, previousLog);
            throw e;
        }
    }

    // [F16-B16] Copy an optional upload under a random name
    private String copyUpload(Path source, boolean photoOnly) throws IOException, InvalidReportException {
        if (source == null) {
            return null;
        }
        String extension = uploadExtension(source, photoOnly);
        Files.createDirectories(uploads);
        String name = UUID.randomUUID() + extension;
        String storedPath = "uploads/" + name;
        try {
            Files.copy(source, uploads.resolve(name)); // [F16-B16] Keep an independent local copy
        } catch (IOException e) {
            discardUpload(storedPath, e);
            throw e;
        }
        return storedPath;
    }

    // [F16-B17] Allow photos for cleanup and photos/videos for reports
    private String uploadExtension(Path source, boolean photoOnly) throws InvalidReportException {
        String name = source.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot);
        if (!Files.isRegularFile(source)
                || (!PHOTOS.contains(extension) && (photoOnly || !VIDEOS.contains(extension)))) {
            throw new InvalidReportException(photoOnly ? "Choose a supported photo."
                    : "Choose a supported photo or video.");
        }
        return extension;
    }

    // [F16-B18] Resolve a stored path for the admin to open
    Path attachmentPath(String storedPath) {
        return dataFile.resolveSibling(storedPath);
    }

    // [F16-B19] Remove a staged upload after a failed submission
    private void discardUpload(String storedPath, Exception failure) {
        if (storedPath != null) {
            try {
                Files.deleteIfExists(attachmentPath(storedPath));
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
        }
    }

    // [F16-B20] Write both lists to a temp file before replacing the save
    private void save() throws IOException {
        Path temporary = Files.createTempFile(dataFile.toAbsolutePath().getParent(), "bantaydagat-", ".tmp");
        try {
            writeLists(temporary);
            replaceSave(temporary); // [F16-B20] Keep the previous file until writing succeeds
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }
            throw e;
        }
    }

    // [F16-B21] Serialize reports first, then users
    private void writeLists(Path temporary) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporary))) {
            out.writeObject(reports);
            out.writeObject(users);
        }
    }

    // [F16-B22] Replace the saved file after the stream closes
    private void replaceSave(Path temporary) throws IOException {
        try {
            Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // [F16-B23] Load the two lists in the same order used by writeLists()
    private void load() throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(dataFile))) {
            readList(in.readObject(), Report.class, reports);
            readList(in.readObject(), User.class, users);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("The saved data has an incompatible format.", e);
        }
    }

    // [F16-B24] Check the stored type of each list element
    private <T> void readList(Object object, Class<T> type, List<T> destination) {
        for (Object entry : (List<?>) object) {
            destination.add(type.cast(entry));
        }
    }
}
```

### FILE 17 of 28: src/main/java/ph/bantaydagat/AuthService.java

```java
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
```

### FILE 18 of 28: src/main/java/ph/bantaydagat/DemoData.java

```java
// FILE 18 of 28: src/main/java/ph/bantaydagat/DemoData.java
// [F18-B01] Package and dependencies
package ph.bantaydagat;

import java.time.LocalDateTime; // [F18-B01] Timestamp the demo cleanup log
import java.util.List; // [F18-B01] Fill the manager's new collections

final class DemoData {
    // [F18-B02] Utility constructor
    private DemoData() {}

    // [F18-B03] Create the two demo accounts and seed reports
    static void seed(List<Report> reports, List<User> users) {
        CitizenUser citizen = new CitizenUser("citizen", "change-citizen-password");
        users.add(new AdminUser("admin", "change-admin-password"));
        users.add(citizen);
        addActive(reports, citizen.getId());
        addRemaining(reports, citizen.getId());
    }

    // [F18-B04] Seed the first four reports
    private static void addActive(List<Report> reports, String citizen) {
        reports.add(example(13.75, 121.04, WasteCategory.PLASTIC, Severity.MEDIUM,
                citizen, "Plastic bottles on the Batangas Bay shoreline.", Status.SUBMITTED));
        reports.add(example(13.80, 120.90, WasteCategory.FISHING_GEAR, Severity.HIGH,
                null, "Discarded net tangled near the reef.", Status.UNDER_REVIEW));
        reports.add(example(14.58, 120.97, WasteCategory.OIL_CHEMICAL, Severity.CRITICAL,
                null, "Oil sheen beside the Manila Bay seawall.", Status.DISPATCHED));
        reports.add(example(13.49, 120.95, WasteCategory.GLASS_METAL, Severity.LOW,
                citizen, "Glass fragments along the beach access.", Status.RESOLVED));
    }

    // [F18-B05] Seed four more reports across categories and states
    private static void addRemaining(List<Report> reports, String citizen) {
        reports.add(example(14.62, 120.94, WasteCategory.ORGANIC, Severity.LOW,
                null, "Food waste collecting near the harbor.", Status.UNDER_REVIEW));
        reports.add(example(13.67, 120.89, WasteCategory.PLASTIC, Severity.HIGH,
                citizen, "Plastic sacks along the rocky coast.", Status.SUBMITTED));
        reports.add(example(13.84, 120.61, WasteCategory.OTHER, Severity.MEDIUM,
                null, "Broken foam crates beside the landing area.", Status.RESOLVED));
        reports.add(example(14.44, 120.90, WasteCategory.FISHING_GEAR, Severity.MEDIUM,
                null, "Loose fishing lines floating beside a pier.", Status.DISPATCHED));
    }

    // [F18-B06] Construct a validated report and set its demo status
    private static Report example(double lat, double lon, WasteCategory category, Severity severity,
                                  String citizen, String description, Status status) {
        Report report = new TextReport(new Location(lat, lon), severity, citizen, category, description);
        try {
            report.submit();
        } catch (InvalidReportException e) {
            throw new IllegalStateException("Invalid demo report.", e);
        }
        ResolutionLog log = status == Status.RESOLVED
                ? new ResolutionLog(LocalDateTime.now(), "Demo cleanup crew collected and sorted the debris.",
                        null, null) : null;
        report.setWorkflow(status, log);
        return report;
    }
}
```

### FILE 19 of 28: src/main/java/ph/bantaydagat/HotlinesDirectory.java

```java
// FILE 19 of 28: src/main/java/ph/bantaydagat/HotlinesDirectory.java
// [F19-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F19-B01] Fixed hotline entries

public final class HotlinesDirectory {
    // [F19-B02] A contact row and the official source used to check it
    public record Hotline(String service, String number, String source) {}

    // [F19-B03] National emergency and environmental contacts
    private static final Hotline EMERGENCY = new Hotline("National Emergency", "911",
            "https://ehotlines.e.gov.ph/");
    private static final Hotline COAST_GUARD = new Hotline("Philippine Coast Guard", "(02) 8527-3877",
            "https://ehotlines.e.gov.ph/");
    private static final Hotline EMB = new Hotline("DENR-EMB CALABARZON (pollution)", "(02) 8653-3496",
            "https://calabarzon.emb.gov.ph/contact-us/");

    // [F19-B04] Example LGU and maritime rescue contacts
    private static final Hotline CALAMBA = new Hotline("Calamba City Public Order and Safety",
            "0917-148-9813", "https://gad.calambacity.gov.ph/hotlines/");
    private static final Hotline BATANGAS = new Hotline("Batangas City CDRRMO", "(043) 702-3902",
            "https://batangascity.gov.ph/web/cdrrmoprofile");
    private static final Hotline MARINE_RESCUE = new Hotline("PCG Maritime Search and Rescue Group",
            "0992-016-0892", "https://www.coastguard.gov.ph/images/2024/ARTA/PCG_CITIZENS_CHARTER_2024.pdf");

    // [F19-B05] Fixed display order for the directory
    public List<Hotline> getHotlines() {
        return List.of(EMERGENCY, COAST_GUARD, EMB, CALAMBA, BATANGAS, MARINE_RESCUE);
    }
}
```

## JavaFX UI

### FILE 20 of 28: src/main/java/ph/bantaydagat/ViewSupport.java

```java
// FILE 20 of 28: src/main/java/ph/bantaydagat/ViewSupport.java
// [F20-B01] Package and dependencies
package ph.bantaydagat;

import java.io.File; // [F20-B01] FileChooser returns a File
import java.nio.file.Path; // [F20-B01] Open a copied attachment
import java.util.function.Function; // [F20-B01] Extract each column value
import javafx.beans.property.ReadOnlyObjectWrapper; // [F20-B01] Adapt a model value to JavaFX
import javafx.geometry.Insets; // [F20-B01] View padding
import javafx.scene.Node; // [F20-B01] Nodes passed into layouts
import javafx.scene.control.Alert; // [F20-B01] Show validation and file errors
import javafx.scene.control.Label; // [F20-B01] Titles
import javafx.scene.control.TableColumn; // [F20-B01] Table column definitions
import javafx.scene.control.TableView; // [F20-B01] Shared table setup
import javafx.scene.layout.VBox; // [F20-B01] Shared vertical layout
import javafx.stage.FileChooser; // [F20-B01] Pick report and cleanup files
import javafx.stage.Window; // [F20-B01] Owner of the file picker

final class ViewSupport {
    // [F20-B02] Utility constructor
    private ViewSupport() {}

    // [F20-B03] Standard padding for each view
    static VBox page(Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(20));
        return box;
    }

    // [F20-B04] Standard view heading
    static Label title(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #13566a;");
        return label;
    }

    // [F20-B05] Show a readable error popup
    static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait(); // [F20-B05] Present the error to the user
    }

    // [F20-B06] Attach columns and an empty state to a report table
    static TableView<Report> reportTable() {
        TableView<Report> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No matching reports."));
        table.getColumns().add(column("Report ID", report -> report.getReportID().substring(0, 8)));
        table.getColumns().add(column("Category", Report::getCategory));
        table.getColumns().add(column("Severity", Report::getSeverityLevel));
        table.getColumns().add(column("Status", Report::getStatus));
        table.getColumns().add(column("Location", Report::getLocation));
        return table;
    }

    // [F20-B07] Connect a table column to a getter
    private static <T> TableColumn<Report, T> column(String name, Function<Report, T> getter) {
        TableColumn<Report, T> column = new TableColumn<>(name);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getter.apply(cell.getValue())));
        return column;
    }

    // [F20-B08] Open the native file picker with supported extensions
    static Path chooseFile(Window owner, boolean photoOnly) {
        FileChooser picker = new FileChooser();
        picker.setTitle(photoOnly ? "Choose a cleanup photo" : "Choose a report attachment");
        picker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Photos",
                "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        if (!photoOnly) {
            picker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Videos",
                    "*.mp4", "*.mov", "*.m4v", "*.webm"));
        }
        File selected = picker.showOpenDialog(owner); // [F20-B08] Display the picker
        return selected == null ? null : selected.toPath();
    }
}
```

### FILE 21 of 28: src/main/java/ph/bantaydagat/LoginView.java

```java
// FILE 21 of 28: src/main/java/ph/bantaydagat/LoginView.java
// [F21-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F21-B01] Handle failed registration saves
import java.util.function.Consumer; // [F21-B01] Send the account to Main
import javafx.geometry.Pos; // [F21-B01] Center the sign-in form
import javafx.scene.control.Button; // [F21-B01] Session actions
import javafx.scene.control.Label; // [F21-B01] Help and form labels
import javafx.scene.control.PasswordField; // [F21-B01] Mask typed passwords
import javafx.scene.control.TextField; // [F21-B01] Username input
import javafx.scene.layout.HBox; // [F21-B01] Login and register buttons
import javafx.scene.layout.VBox; // [F21-B01] Sign-in layout

public final class LoginView extends VBox {
    // [F21-B02] Form fields and registration feedback
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final Label feedback = new Label();

    // [F21-B03] Put the form and demo credentials on screen
    public LoginView(AuthService auth, Consumer<User> signedIn, Runnable anonymous) {
        setAlignment(Pos.CENTER);
        setSpacing(12);
        VBox fields = createFields();
        getChildren().addAll(ViewSupport.title("BantayDagat"),
                new Label("Marine Waste Reporting & Incident Management System"), fields,
                createActions(auth, signedIn, anonymous), feedback,
                new Label("Demo accounts: admin / change-admin-password        citizen / change-citizen-password"));
    }

    // [F21-B04] Set up the account fields
    private VBox createFields() {
        username.setPromptText("Username");
        password.setPromptText("Password");
        VBox fields = new VBox(6, new Label("Username"), username, new Label("Password"), password);
        fields.setMaxWidth(320);
        return fields;
    }

    // [F21-B05] Connect login, registration and guest mode
    private HBox createActions(AuthService auth, Consumer<User> signedIn, Runnable anonymous) {
        Button login = new Button("Log in");
        Button register = new Button("Register citizen");
        Button guest = new Button("Report anonymously");
        login.setDefaultButton(true);
        login.setOnAction(event -> logIn(auth, signedIn)); // [F21-B05] Trigger credential check
        register.setOnAction(event -> register(auth)); // [F21-B05] Save a new citizen
        guest.setOnAction(event -> anonymous.run()); // [F21-B05] Open the guest form
        HBox buttons = new HBox(8, login, register, guest);
        buttons.setAlignment(Pos.CENTER);
        return buttons;
    }

    // [F21-B06] Sign in or show an invalid credentials message
    private void logIn(AuthService auth, Consumer<User> signedIn) {
        String entered = password.getText();
        password.clear();
        auth.login(username.getText(), entered).ifPresentOrElse(signedIn,
                () -> ViewSupport.error("Invalid username or password."));
    }

    // [F21-B07] Register and prompt the new citizen to sign in
    private void register(AuthService auth) {
        try {
            CitizenUser user = auth.register(username.getText(), password.getText());
            username.setText(user.getUsername());
            password.clear();
            feedback.setText("Account created. Enter your password and log in.");
        } catch (IllegalArgumentException | IOException e) {
            ViewSupport.error(e.getMessage());
        }
    }
}
```

### FILE 22 of 28: src/main/java/ph/bantaydagat/ReportFormView.java

```java
// FILE 22 of 28: src/main/java/ph/bantaydagat/ReportFormView.java
// [F22-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F22-B01] Handle copy and save errors
import java.nio.file.Path; // [F22-B01] Selected file stays local until submission
import javafx.scene.control.Button; // [F22-B01] Form actions
import javafx.scene.control.ComboBox; // [F22-B01] Category and severity controls
import javafx.scene.control.Label; // [F22-B01] Form labels and feedback
import javafx.scene.control.ScrollPane; // [F22-B01] Keep the form usable in a small window
import javafx.scene.control.TextArea; // [F22-B01] Incident description
import javafx.scene.control.TextField; // [F22-B01] Latitude and longitude
import javafx.scene.layout.HBox; // [F22-B01] Attachment picker row
import javafx.scene.layout.VBox; // [F22-B01] Form layout

public final class ReportFormView extends ScrollPane {
    // [F22-B02] Session and services
    private final IncidentManager manager;
    private final String reporterId;
    private final Runnable onSaved;

    // [F22-B03] Form fields and optional attachment
    private final ComboBox<WasteCategory> category = new ComboBox<>();
    private final ComboBox<Severity> severity = new ComboBox<>();
    private final TextArea description = new TextArea();
    private final TextField latitude = new TextField();
    private final TextField longitude = new TextField();
    private final Label attachmentName = new Label("No attachment");
    private final Label feedback = new Label();
    private Path attachment;

    // [F22-B04] Wire the form to the current session
    public ReportFormView(IncidentManager manager, String reporterId, Runnable onSaved) {
        this.manager = manager;
        this.reporterId = reporterId; // [F22-B04] Null means anonymous
        this.onSaved = onSaved;
        configureFields();
        setFitToWidth(true);
        setContent(createForm());
    }

    // [F22-B05] Load the enums and configure the description field
    private void configureFields() {
        category.getItems().setAll(WasteCategory.values());
        severity.getItems().setAll(Severity.values());
        latitude.setPromptText("e.g. 13.7500");
        longitude.setPromptText("e.g. 121.0400");
        description.setPromptText("Describe the waste and where you saw it.");
        description.setWrapText(true);
        description.setPrefRowCount(4);
    }

    // [F22-B06] Lay out the reporting fields and submit action
    private VBox createForm() {
        Label note = new Label(reporterId == null
                ? "Anonymous report. Keep names and identifying details out of text and attachments,"
                        + " including file metadata."
                : "This report will appear in My Reports.");
        note.setWrapText(true);
        Button submit = new Button("Submit report");
        submit.setOnAction(event -> submit()); // [F22-B06] Connect the button to the submission logic
        VBox fields = ViewSupport.page(ViewSupport.title("Report marine waste"), note,
                new HBox(12, field("Waste category", category), field("Severity", severity)),
                new HBox(12, field("Latitude (-90 to 90)", latitude), field("Longitude (-180 to 180)", longitude)),
                field("Description", description), createAttachmentRow(), submit, feedback);
        fields.setMaxWidth(780);
        return fields;
    }

    // [F22-B07] Pair a control with its field label
    private VBox field(String label, javafx.scene.Node control) {
        return new VBox(5, new Label(label), control);
    }

    // [F22-B08] Choose or remove an optional attachment
    private HBox createAttachmentRow() {
        Button choose = new Button("Choose photo or video");
        Button remove = new Button("Remove attachment");
        choose.setOnAction(event -> {
            Path selected = ViewSupport.chooseFile(getScene().getWindow(), false);
            if (selected != null) {
                attachment = selected;
                attachmentName.setText(selected.getFileName().toString());
            }
        });
        remove.setOnAction(event -> clearAttachment());
        return new HBox(8, choose, remove, attachmentName);
    }

    // [F22-B09] Submit through IncidentManager and refresh history
    private void submit() {
        try {
            Location location = parseLocation();
            Report report = manager.submitReport(location, severity.getValue(), reporterId,
                    category.getValue(), description.getText().strip(), attachment);
            clearForm();
            feedback.setText("Saved as Submitted. Report ID: " + report.getReportID().substring(0, 8));
            onSaved.run(); // [F22-B09] Refresh views after the save succeeds
        } catch (InvalidReportException | IOException e) {
            ViewSupport.error(e.getMessage());
        }
    }

    // [F22-B10] Parse coordinates; the model enforces the ranges
    private Location parseLocation() throws InvalidReportException {
        try {
            return new Location(Double.parseDouble(latitude.getText().strip()),
                    Double.parseDouble(longitude.getText().strip()));
        } catch (NumberFormatException e) {
            throw new InvalidReportException("Enter numeric latitude and longitude using a decimal point.");
        }
    }

    // [F22-B11] Reset inputs after submission
    private void clearForm() {
        category.getSelectionModel().clearSelection();
        severity.getSelectionModel().clearSelection();
        latitude.clear();
        longitude.clear();
        description.clear();
        clearAttachment();
    }

    // [F22-B12] Drop the local selection without changing any original file
    private void clearAttachment() {
        attachment = null;
        attachmentName.setText("No attachment");
    }
}
```

### FILE 23 of 28: src/main/java/ph/bantaydagat/MyReportsView.java

```java
// FILE 23 of 28: src/main/java/ph/bantaydagat/MyReportsView.java
// [F23-B01] Package and dependencies
package ph.bantaydagat;

import java.util.Comparator; // [F23-B01] Most recent reports first
import javafx.geometry.Insets; // [F23-B01] View padding
import javafx.scene.control.TableView; // [F23-B01] Citizen history
import javafx.scene.control.TextArea; // [F23-B01] Selected report summary
import javafx.scene.layout.Priority; // [F23-B01] Expand table with the window
import javafx.scene.layout.VBox; // [F23-B01] History layout

public final class MyReportsView extends VBox {
    // [F23-B02] Session and report table
    private final IncidentManager manager;
    private final String citizenId;
    private final TableView<Report> table = ViewSupport.reportTable();

    // [F23-B03] Build a read-only history for the signed-in citizen
    public MyReportsView(IncidentManager manager, String citizenId) {
        this.manager = manager;
        this.citizenId = citizenId;
        setSpacing(12);
        setPadding(new Insets(20));
        getChildren().addAll(ViewSupport.title("My Reports"), table, createDetails());
        VBox.setVgrow(table, Priority.ALWAYS);
        refresh();
    }

    // [F23-B04] Load only the citizen's reports
    public void refresh() {
        table.getItems().setAll(manager.reportsByReporter(citizenId).stream()
                .sorted(Comparator.comparing(Report::getTimestamp).reversed()).toList());
    }

    // [F23-B05] Display details and status for the selected row
    private TextArea createDetails() {
        TextArea details = new TextArea();
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(5);
        details.setPromptText("Select a report to see its details.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) ->
                details.setText(current == null ? "" : current.exportReport())); // [F23-B05] Bind selected row
        return details;
    }
}
```

### FILE 24 of 28: src/main/java/ph/bantaydagat/AdminView.java

```java
// FILE 24 of 28: src/main/java/ph/bantaydagat/AdminView.java
// [F24-B01] Package and dependencies
package ph.bantaydagat;

import java.awt.Desktop; // [F24-B01] Open attachments in the system's default app
import java.io.IOException; // [F24-B01] File operation failures
import java.nio.file.Files; // [F24-B01] Check if stored evidence still exists
import java.nio.file.Path; // [F24-B01] Selected photos
import java.util.Comparator; // [F24-B01] Severity, status and category sort
import javafx.geometry.Orientation; // [F24-B01] Split the queue and details vertically
import javafx.scene.control.Button; // [F24-B01] Queue actions
import javafx.scene.control.ButtonType; // [F24-B01] Confirm resolution
import javafx.scene.control.ComboBox; // [F24-B01] Queue filters
import javafx.scene.control.Dialog; // [F24-B01] Cleanup details form
import javafx.scene.control.Label; // [F24-B01] Filter and file labels
import javafx.scene.control.SplitPane; // [F24-B01] Adjustable queue and details
import javafx.scene.control.TableView; // [F24-B01] Admin report queue
import javafx.scene.control.TextArea; // [F24-B01] Report details and notes
import javafx.scene.layout.HBox; // [F24-B01] Action rows
import javafx.scene.layout.Priority; // [F24-B01] Fill available space
import javafx.scene.layout.VBox; // [F24-B01] Main queue layout

public final class AdminView extends VBox {
    // [F24-B02] Incident store and dashboard refresh callback
    private final IncidentManager manager;
    private final Runnable onChanged;

    // [F24-B03] Queue table and filters
    private final TableView<Report> table = ViewSupport.reportTable();
    private final ComboBox<Severity> severity = new ComboBox<>();
    private final ComboBox<Status> status = new ComboBox<>();
    private final ComboBox<WasteCategory> category = new ComboBox<>();
    private final ComboBox<String> sort = new ComboBox<>();

    // [F24-B04] Selected report and available actions
    private final TextArea details = new TextArea();
    private final Button advance = new Button("Advance status");
    private final Button attachment = new Button("Open attachment");
    private final Button before = new Button("Open before photo");
    private final Button after = new Button("Open after photo");

    // [F24-B05] Construct the queue, detail panel and actions
    public AdminView(IncidentManager manager, Runnable onChanged) {
        this.manager = manager;
        this.onChanged = onChanged;
        setSpacing(12);
        setPadding(new javafx.geometry.Insets(20));
        SplitPane panels = new SplitPane(table, createDetailsPanel());
        panels.setOrientation(Orientation.VERTICAL);
        panels.setDividerPositions(0.55);
        VBox.setVgrow(panels, Priority.ALWAYS);
        getChildren().addAll(ViewSupport.title("Incident queue"), createFilters(), panels);
        refresh();
    }

    // [F24-B06] Make filter selections refresh the queue
    private HBox createFilters() {
        configureFilter(severity, "All severities", Severity.values());
        configureFilter(status, "All statuses", Status.values());
        configureFilter(category, "All categories", WasteCategory.values());
        sort.getItems().setAll("Severity (highest first)", "Status", "Category", "Newest");
        sort.getSelectionModel().selectFirst();
        sort.setOnAction(event -> refresh());
        Button clear = new Button("Clear filters");
        clear.setOnAction(event -> clearFilters()); // [F24-B06] Restore the full queue
        return new HBox(8, severity, status, category, new Label("Sort by"), sort, clear);
    }

    // [F24-B07] Add enum options and a placeholder for the unfiltered state
    private <T> void configureFilter(ComboBox<T> filter, String prompt, T[] values) {
        filter.setPromptText(prompt);
        filter.getItems().setAll(values);
        filter.setOnAction(event -> refresh());
    }

    // [F24-B08] Clear all three filters
    private void clearFilters() {
        severity.setValue(null);
        status.setValue(null);
        category.setValue(null);
        refresh();
    }

    // [F24-B09] Wire details and actions to the selected report
    private VBox createDetailsPanel() {
        details.setEditable(false);
        details.setWrapText(true);
        details.setPromptText("Select a report to see its details.");
        details.setMinHeight(80);
        VBox.setVgrow(details, Priority.ALWAYS);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, report) -> showDetails(report));
        advance.setOnAction(event -> advanceSelected()); // [F24-B09] Change the selected status
        attachment.setOnAction(event -> open(selected().getMediaPath()));
        before.setOnAction(event -> open(selected().getResolutionLog().beforePhotoPath()));
        after.setOnAction(event -> open(selected().getResolutionLog().afterPhotoPath()));
        showDetails(null);
        return new VBox(8, details, new HBox(8, advance, attachment, before, after));
    }

    // [F24-B10] Show the report, resolution log and available actions
    private void showDetails(Report report) {
        details.setText(report == null ? "" : report.exportReport());
        Status next = report == null ? null : report.getStatus().next();
        advance.setText(next == null ? "Advance status" : "Move to " + next);
        advance.setDisable(next == null);
        attachment.setDisable(report == null || report.getMediaPath() == null);
        ResolutionLog log = report == null ? null : report.getResolutionLog();
        before.setDisable(log == null || log.beforePhotoPath() == null);
        after.setDisable(log == null || log.afterPhotoPath() == null);
    }

    // [F24-B11] Read the current row
    private Report selected() {
        return table.getSelectionModel().getSelectedItem();
    }

    // [F24-B12] Apply filters and retain selection when the report still matches
    public void refresh() {
        Report previous = selected();
        table.getSortOrder().clear();
        table.getItems().setAll(manager.filterReports(severity.getValue(), status.getValue(),
                category.getValue(), order()));
        if (previous != null && table.getItems().contains(previous)) {
            table.getSelectionModel().select(previous);
        }
        showDetails(selected());
    }

    // [F24-B13] Translate the sort control into a report comparator
    private Comparator<Report> order() {
        Comparator<Report> newest = Comparator.comparing(Report::getTimestamp).reversed();
        return switch (sort.getSelectionModel().getSelectedIndex()) {
            case 1 -> Comparator.comparing(Report::getStatus).thenComparing(newest);
            case 2 -> Comparator.comparing((Report report) -> report.getCategory().toString())
                    .thenComparing(newest);
            case 3 -> newest;
            default -> Comparator.comparing(Report::getSeverityLevel).reversed().thenComparing(newest);
        };
    }

    // [F24-B14] Move one step or collect a resolution log first
    private void advanceSelected() {
        Report report = selected();
        if (report == null || report.getStatus().next() == null) {
            return;
        }
        Status next = report.getStatus().next();
        if (next == Status.RESOLVED) {
            showResolutionDialog(report);
        } else {
            update(report, next, null, null, null);
        }
    }

    // [F24-B15] Create the notes and optional before/after photo controls
    private void showResolutionDialog(Report report) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Resolve incident");
        TextArea notes = new TextArea();
        notes.setPromptText("Describe the cleanup work.");
        notes.setPrefRowCount(4);
        Button beforePhoto = new Button("Choose before photo");
        Button afterPhoto = new Button("Choose after photo");
        wirePhoto(beforePhoto);
        wirePhoto(afterPhoto);
        dialog.getDialogPane().setContent(new VBox(8, new Label("Cleanup notes (required)"), notes,
                beforePhoto, afterPhoto));
        confirmResolution(dialog, report, notes, beforePhoto, afterPhoto);
    }

    // [F24-B16] Keep the dialog open when validation or saving fails
    private void confirmResolution(Dialog<ButtonType> dialog, Report report, TextArea notes,
                                   Button beforePhoto, Button afterPhoto) {
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button confirm = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        confirm.setText("Resolve");
        confirm.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> notes.getText().isBlank(), notes.textProperty()));
        confirm.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!update(report, Status.RESOLVED, notes.getText(),
                    (Path) beforePhoto.getUserData(), (Path) afterPhoto.getUserData())) {
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    // [F24-B17] Store the chosen photo path on its picker button
    private void wirePhoto(Button button) {
        button.setOnAction(event -> {
            Path selected = ViewSupport.chooseFile(button.getScene().getWindow(), true);
            if (selected != null) {
                button.setUserData(selected);
                button.setText(selected.getFileName().toString());
            }
        });
    }

    // [F24-B18] Persist the status and refresh the queue and dashboard
    private boolean update(Report report, Status next, String notes, Path beforePhoto, Path afterPhoto) {
        try {
            manager.updateStatus(report, next, notes, beforePhoto, afterPhoto);
            refresh();
            onChanged.run(); // [F24-B18] Update the charts after the save succeeds
            return true;
        } catch (InvalidStatusTransitionException | InvalidReportException | IOException e) {
            ViewSupport.error(e.getMessage());
            return false;
        }
    }

    // [F24-B19] Open the copied evidence file
    private void open(String path) {
        try {
            Path file = manager.attachmentPath(path);
            if (!Files.isRegularFile(file)) {
                throw new IOException("Attachment file is missing: " + path);
            }
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                throw new IOException("Open this attachment manually: " + file.toAbsolutePath());
            }
            Desktop.getDesktop().open(file.toFile());
        } catch (IOException e) {
            ViewSupport.error(e.getMessage());
        }
    }
}
```

### FILE 25 of 28: src/main/java/ph/bantaydagat/DashboardView.java

```java
// FILE 25 of 28: src/main/java/ph/bantaydagat/DashboardView.java
// [F25-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F25-B01] Snapshot of all reports
import javafx.geometry.Insets; // [F25-B01] Dashboard spacing
import javafx.scene.chart.BarChart; // [F25-B01] Counts per category
import javafx.scene.chart.CategoryAxis; // [F25-B01] Waste categories along the bottom
import javafx.scene.chart.NumberAxis; // [F25-B01] Count axis
import javafx.scene.chart.PieChart; // [F25-B01] Active versus resolved
import javafx.scene.chart.XYChart; // [F25-B01] Bar data series
import javafx.scene.layout.HBox; // [F25-B01] Side-by-side charts
import javafx.scene.layout.Priority; // [F25-B01] Resize charts with the window
import javafx.scene.layout.VBox; // [F25-B01] Dashboard layout

public final class DashboardView extends VBox {
    // [F25-B02] Read-only manager and reusable charts
    private final IncidentManager manager;
    private final PieChart statusChart = new PieChart();
    private final BarChart<String, Number> categoryChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

    // [F25-B03] Build the dashboard with two resizable charts
    public DashboardView(IncidentManager manager) {
        this.manager = manager;
        setSpacing(12);
        setPadding(new Insets(20));
        configureCharts();
        HBox charts = new HBox(12, statusChart, categoryChart);
        HBox.setHgrow(statusChart, Priority.ALWAYS);
        HBox.setHgrow(categoryChart, Priority.ALWAYS);
        VBox.setVgrow(charts, Priority.ALWAYS);
        getChildren().addAll(ViewSupport.title("Incident dashboard"), charts);
        refresh();
    }

    // [F25-B04] Label charts and disable animation during updates
    private void configureCharts() {
        statusChart.setTitle("Active vs. resolved");
        statusChart.setAnimated(false);
        statusChart.setMinWidth(250);
        categoryChart.setTitle("Reports by waste category");
        categoryChart.getYAxis().setLabel("Reports");
        categoryChart.getXAxis().setTickLabelRotation(-32);
        categoryChart.setAnimated(false);
        categoryChart.setLegendVisible(false);
        categoryChart.setMinWidth(410);
    }

    // [F25-B05] Refresh both charts from the same snapshot
    public void refresh() {
        List<Report> reports = manager.getReports();
        updateStatusChart(reports);
        updateCategoryChart(reports); // [F25-B05] Apply the same report data to the bar chart
    }

    // [F25-B06] Count all unresolved statuses as active
    private void updateStatusChart(List<Report> reports) {
        long resolved = reports.stream().filter(report -> report.getStatus() == Status.RESOLVED).count();
        long active = reports.size() - resolved;
        statusChart.getData().setAll(new PieChart.Data("Active (" + active + ")", active),
                new PieChart.Data("Resolved (" + resolved + ")", resolved));
    }

    // [F25-B07] Count every waste category, including categories with zero reports
    private void updateCategoryChart(List<Report> reports) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (WasteCategory category : WasteCategory.values()) {
            long count = reports.stream().filter(report -> report.getCategory() == category).count();
            series.getData().add(new XYChart.Data<>(category.toString(), count));
        }
        categoryChart.getData().clear();
        categoryChart.getData().add(series);
    }
}
```

### FILE 26 of 28: src/main/java/ph/bantaydagat/HotlinesView.java

```java
// FILE 26 of 28: src/main/java/ph/bantaydagat/HotlinesView.java
// [F26-B01] Package and dependencies
package ph.bantaydagat;

import java.util.function.Consumer; // [F26-B01] Open official source pages
import javafx.geometry.Insets; // [F26-B01] Directory padding
import javafx.scene.control.Hyperlink; // [F26-B01] Official source link
import javafx.scene.control.Label; // [F26-B01] Service and phone labels
import javafx.scene.layout.GridPane; // [F26-B01] Directory rows and columns
import javafx.scene.layout.VBox; // [F26-B01] Page layout

public final class HotlinesView extends VBox {
    // [F26-B02] Display the directory and a note about emergency dispatch
    public HotlinesView(HotlinesDirectory directory, Consumer<String> openSource) {
        setSpacing(12);
        setPadding(new Insets(20));
        getChildren().addAll(ViewSupport.title("Emergency hotlines"),
                new Label("For immediate danger, call the appropriate hotline directly."),
                createGrid(directory, openSource));
    }

    // [F26-B03] Create a row for each stored contact
    private GridPane createGrid(HotlinesDirectory directory, Consumer<String> openSource) {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);
        grid.addRow(0, new Label("Service"), new Label("Phone"), new Label("Reference"));
        int row = 1;
        for (HotlinesDirectory.Hotline hotline : directory.getHotlines()) {
            Hyperlink source = new Hyperlink("Official source");
            source.setOnAction(event -> openSource.accept(hotline.source()));
            grid.addRow(row++, new Label(hotline.service()), new Label(hotline.number()), source);
        }
        return grid;
    }
}
```

### FILE 27 of 28: src/main/java/ph/bantaydagat/Main.java

```java
// FILE 27 of 28: src/main/java/ph/bantaydagat/Main.java
// [F27-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F27-B01] Handle startup persistence failures
import java.nio.file.Path; // [F27-B01] data.ser and uploads/ live in the working directory
import javafx.application.Application; // [F27-B01] JavaFX application lifecycle
import javafx.application.Platform; // [F27-B01] Exit after a startup failure
import javafx.geometry.Insets; // [F27-B01] Navigation bar spacing
import javafx.geometry.Pos; // [F27-B01] Align the session header
import javafx.scene.Node; // [F27-B01] Add any view to a tab
import javafx.scene.Scene; // [F27-B01] Window content
import javafx.scene.control.Button; // [F27-B01] Sign out / return to login
import javafx.scene.control.Label; // [F27-B01] Current session text
import javafx.scene.control.Tab; // [F27-B01] Each session screen
import javafx.scene.control.TabPane; // [F27-B01] Session navigation
import javafx.scene.layout.BorderPane; // [F27-B01] App shell
import javafx.scene.layout.HBox; // [F27-B01] Session bar
import javafx.scene.layout.Priority; // [F27-B01] Expand the header spacer
import javafx.scene.layout.Region; // [F27-B01] Push the sign-out button to the right
import javafx.stage.Stage; // [F27-B01] Desktop window

public final class Main extends Application {
    // [F27-B02] Shared app state and root layout
    private final BorderPane root = new BorderPane();
    private IncidentManager manager;
    private AuthService auth;

    // [F27-B03] Startup, persistence load and first screen
    @Override
    public void start(Stage stage) {
        try {
            manager = new IncidentManager(Path.of("."));
            auth = new AuthService(manager);
        } catch (IOException e) {
            ViewSupport.error("Could not open BantayDagat's data: " + e.getMessage());
            Platform.exit();
            return;
        }
        showLogin();
        showWindow(stage);
    }

    // [F27-B04] Configure and display the window
    private void showWindow(Stage stage) {
        stage.setTitle("BantayDagat: Marine Waste Reporting & Incident Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(590);
        stage.setScene(new Scene(root, 1060, 690)); // [F27-B04] Attach the app layout to the window
        stage.show(); // [F27-B04] A Stage must be shown to display its Scene
    }

    // [F27-B05] Connect login and anonymous entry to session navigation
    private void showLogin() {
        root.setTop(null);
        root.setCenter(new LoginView(auth, this::showSession, () -> showSession(null))); // [F27-B05] Guest has no account
    }

    // [F27-B06] Use role permissions to select the right screens
    private void showSession(User user) {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        if (user == null) {
            tabs.getTabs().add(tab("New report", new ReportFormView(manager, null, () -> {})));
        } else if (user.getPermissions().contains(User.MANAGE)) {
            addAdminTabs(tabs);
        } else if (user.getPermissions().contains(User.REPORT)) {
            addCitizenTabs(tabs, user);
        }
        root.setTop(sessionBar(user));
        root.setCenter(tabs);
    }

    // [F27-B07] Admin tabs share the same manager and dashboard refresh
    private void addAdminTabs(TabPane tabs) {
        DashboardView dashboard = new DashboardView(manager);
        tabs.getTabs().addAll(tab("Incident queue", new AdminView(manager, dashboard::refresh)),
                tab("Dashboard", dashboard),
                tab("Hotlines", new HotlinesView(new HotlinesDirectory(), getHostServices()::showDocument)));
    }

    // [F27-B08] Registered users can submit and see their own history
    private void addCitizenTabs(TabPane tabs, User user) {
        MyReportsView history = new MyReportsView(manager, user.getId());
        tabs.getTabs().add(tab("New report", new ReportFormView(manager, user.getId(), history::refresh)));
        if (user.getPermissions().contains(User.MY_REPORTS)) {
            tabs.getTabs().add(tab("My Reports", history));
        }
    }

    // [F27-B09] Session title and sign-out action
    private HBox sessionBar(User user) {
        String who = user == null ? "Anonymous reporting" : "Signed in as " + user.getUsername();
        Button leave = new Button(user == null ? "Back to login" : "Sign out");
        leave.setOnAction(event -> showLogin());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, new Label("BantayDagat"), new Label(who), spacer, leave);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: #e3f3f6;");
        return bar;
    }

    // [F27-B10] Wrap a screen in a navigation tab
    private Tab tab(String title, Node view) {
        return new Tab(title, view);
    }

    // [F27-B11] Java entry point used by Maven
    public static void main(String[] args) {
        launch(args); // [F27-B11] Start the JavaFX runtime
    }
}
```

## Standalone utility

### FILE 28 of 28: PrintNumbered.java

```java
// FILE 28 of 28: PrintNumbered.java
// [F28-B01] Standalone utility dependencies
import java.io.IOException; // [F28-B01] Handle unreadable source files
import java.nio.file.Files; // [F28-B01] Read UTF-8 source
import java.nio.file.Path; // [F28-B01] Convert the command-line argument

public final class PrintNumbered {
    // [F28-B02] Check the argument and print the numbered source
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java PrintNumbered.java <source-file>");
            System.exit(1);
        }
        try {
            print(Path.of(args[0]));
        } catch (IOException e) {
            System.err.println("Could not read source: " + e.getMessage());
            System.exit(1);
        }
    }

    // [F28-B03] Count and print each line, including blank lines
    private static void print(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            int number = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.printf("%4d | %s%n", number++, line); // [F28-B03] Increment after printing
            }
        }
    }
}
```

## Run and print

From the folder containing pom.xml, run these commands with JDK 17+ and Maven 3 installed.

```powershell
mvn clean javafx:run
java PrintNumbered.java "src/main/java/ph/bantaydagat/IncidentManager.java"
```

Demo credentials: admin / change-admin-password and citizen / change-citizen-password.

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

