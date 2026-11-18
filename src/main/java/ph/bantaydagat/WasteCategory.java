// FILE 02 of 28: src/main/java/ph/bantaydagat/WasteCategory.java
// [F02-B01] Package and dependencies
package ph.bantaydagat;

public enum WasteCategory {
    // [F02-B02] Values used by the form, filters and chart
    PLASTIC("Plastic"), FISHING_GEAR("Fishing gear"), OIL_CHEMICAL("Oil / chemical"),
    GLASS_METAL("Glass / metal"), ORGANIC("Organic"), OTHER("Other");

    // [F02-B03] Display label
    private final String label;

    // [F02-B04] Constructor
    WasteCategory(String label) {
        this.label = label;
    }

    // [F02-B05] Text shown in JavaFX controls
    @Override
    public String toString() {
        return label;
    }
}

