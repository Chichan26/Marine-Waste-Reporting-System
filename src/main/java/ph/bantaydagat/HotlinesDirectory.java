// FILE 19 of 28: src/main/java/ph/bantaydagat/HotlinesDirectory.java
// [F19-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F19-B01] Fixed hotline entries

public final class HotlinesDirectory {
    // [F19-B02] A contact row and the official source used to check it
    public record Hotline(String service, String number, String source) {}

    // [F19-B03] National emergency and environmental contacts
    private static final Hotline EMERGENCY = new Hotline("National Emergency", "911",
            "https://ehotlines.e.gov.ph/");
    private static final Hotline COAST_GUARD = new Hotline("Philippine Coast Guard", "(02) 8527-3877",
            "https://ehotlines.e.gov.ph/");
    private static final Hotline EMB = new Hotline("DENR-EMB CALABARZON (pollution)", "(02) 8653-3496",
            "https://calabarzon.emb.gov.ph/contact-us/");

    // [F19-B04] Example LGU and maritime rescue contacts
    private static final Hotline CALAMBA = new Hotline("Calamba City Public Order and Safety",
            "0917-148-9813", "https://gad.calambacity.gov.ph/hotlines/");
    private static final Hotline BATANGAS = new Hotline("Batangas City CDRRMO", "(043) 702-3902",
            "https://batangascity.gov.ph/web/cdrrmoprofile");
    private static final Hotline MARINE_RESCUE = new Hotline("PCG Maritime Search and Rescue Group",
            "0992-016-0892", "https://www.coastguard.gov.ph/images/2024/ARTA/PCG_CITIZENS_CHARTER_2024.pdf");

    // [F19-B05] Fixed display order for the directory
    public List<Hotline> getHotlines() {
        return List.of(EMERGENCY, COAST_GUARD, EMB, CALAMBA, BATANGAS, MARINE_RESCUE);
    }
}

