// FILE 18 of 28: src/main/java/ph/bantaydagat/DemoData.java
// [F18-B01] Package and dependencies
package ph.bantaydagat;

import java.time.LocalDateTime; // [F18-B01] Timestamp the demo cleanup log
import java.util.List; // [F18-B01] Fill the manager's new collections

final class DemoData {
    // [F18-B02] Utility constructor
    private DemoData() {}

    // [F18-B03] Create the two demo accounts and seed reports
    static void seed(List<Report> reports, List<User> users) {
        CitizenUser citizen = new CitizenUser("citizen", "change-citizen-password");
        users.add(new AdminUser("admin", "change-admin-password"));
        users.add(citizen);
        addActive(reports, citizen.getId());
        addRemaining(reports, citizen.getId());
    }

    // [F18-B04] Seed the first four reports
    private static void addActive(List<Report> reports, String citizen) {
        reports.add(example(13.75, 121.04, WasteCategory.PLASTIC, Severity.MEDIUM,
                citizen, "Plastic bottles on the Batangas Bay shoreline.", Status.SUBMITTED));
        reports.add(example(13.80, 120.90, WasteCategory.FISHING_GEAR, Severity.HIGH,
                null, "Discarded net tangled near the reef.", Status.UNDER_REVIEW));
        reports.add(example(14.58, 120.97, WasteCategory.OIL_CHEMICAL, Severity.CRITICAL,
                null, "Oil sheen beside the Manila Bay seawall.", Status.DISPATCHED));
        reports.add(example(13.49, 120.95, WasteCategory.GLASS_METAL, Severity.LOW,
                citizen, "Glass fragments along the beach access.", Status.RESOLVED));
    }

    // [F18-B05] Seed four more reports across categories and states
    private static void addRemaining(List<Report> reports, String citizen) {
        reports.add(example(14.62, 120.94, WasteCategory.ORGANIC, Severity.LOW,
                null, "Food waste collecting near the harbor.", Status.UNDER_REVIEW));
        reports.add(example(13.67, 120.89, WasteCategory.PLASTIC, Severity.HIGH,
                citizen, "Plastic sacks along the rocky coast.", Status.SUBMITTED));
        reports.add(example(13.84, 120.61, WasteCategory.OTHER, Severity.MEDIUM,
                null, "Broken foam crates beside the landing area.", Status.RESOLVED));
        reports.add(example(14.44, 120.90, WasteCategory.FISHING_GEAR, Severity.MEDIUM,
                null, "Loose fishing lines floating beside a pier.", Status.DISPATCHED));
    }

    // [F18-B06] Construct a validated report and set its demo status
    private static Report example(double lat, double lon, WasteCategory category, Severity severity,
                                  String citizen, String description, Status status) {
        Report report = new TextReport(new Location(lat, lon), severity, citizen, category, description);
        try {
            report.submit();
        } catch (InvalidReportException e) {
            throw new IllegalStateException("Invalid demo report.", e);
        }
        ResolutionLog log = status == Status.RESOLVED
                ? new ResolutionLog(LocalDateTime.now(), "Demo cleanup crew collected and sorted the debris.",
                        null, null) : null;
        report.setWorkflow(status, log);
        return report;
    }
}

