// FILE 07 of 28: src/main/java/ph/bantaydagat/Location.java
// [F07-B01] Package and dependencies
package ph.bantaydagat;

import java.io.Serializable; // [F07-B01] Required inside a serialized Report
import java.util.Locale; // [F07-B01] Format coordinates consistently

public record Location(double latitude, double longitude) implements Serializable {
    // [F07-B02] Version for the stored coordinate pair
    private static final long serialVersionUID = 1L;

    // [F07-B03] Bounds check also rejects NaN and infinity
    public boolean isValid() {
        return Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
                && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180;
    }

    // [F07-B04] Readable coordinate pair for tables and export
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%.6f, %.6f", latitude, longitude);
    }
}

