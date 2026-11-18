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

