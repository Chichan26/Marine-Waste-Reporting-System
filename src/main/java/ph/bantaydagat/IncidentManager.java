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
    public Path attachmentPath(String storedPath) {
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

