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

