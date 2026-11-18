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

