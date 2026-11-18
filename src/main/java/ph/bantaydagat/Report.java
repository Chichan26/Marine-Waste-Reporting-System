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

