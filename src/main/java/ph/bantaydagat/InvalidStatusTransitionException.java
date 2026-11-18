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

