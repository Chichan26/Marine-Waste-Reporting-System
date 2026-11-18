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

