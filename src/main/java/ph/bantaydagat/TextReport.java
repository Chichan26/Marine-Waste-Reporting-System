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

