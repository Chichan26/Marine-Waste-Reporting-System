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

