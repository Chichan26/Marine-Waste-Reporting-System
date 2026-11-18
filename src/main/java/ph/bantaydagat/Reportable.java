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

