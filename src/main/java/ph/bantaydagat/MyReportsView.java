// FILE 23 of 28: src/main/java/ph/bantaydagat/MyReportsView.java
// [F23-B01] Package and dependencies
package ph.bantaydagat;

import java.util.Comparator; // [F23-B01] Most recent reports first
import java.util.List; // [F23-B01] Citizen's report snapshot
import javafx.scene.control.Label; // [F23-B01] Summary stat values
import javafx.scene.control.SplitPane; // [F23-B01] Table beside details
import javafx.scene.control.TableView; // [F23-B01] Citizen history
import javafx.scene.layout.HBox; // [F23-B01] Summary stat strip
import javafx.scene.layout.Priority; // [F23-B01] Expand with the window
import javafx.scene.layout.VBox; // [F23-B01] History layout

public final class MyReportsView extends VBox {
    // [F23-B02] Session, summary counts and report table
    private final IncidentManager manager;
    private final String citizenId;
    private final TableView<Report> table = ViewSupport.reportTable();
    private final ReportDetailsCard details;
    private final Label totalCount = ViewSupport.styled(new Label("0"), "stat-chip-value");
    private final Label activeCount = ViewSupport.styled(new Label("0"), "stat-chip-value");
    private final Label resolvedCount = ViewSupport.styled(new Label("0"), "stat-chip-value");

    // [F23-B03] Build a read-only history for the signed-in citizen
    public MyReportsView(IncidentManager manager, String citizenId) {
        this.manager = manager;
        this.citizenId = citizenId;
        this.details = new ReportDetailsCard(manager);
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) ->
                details.show(current)); // [F23-B05] Bind selected row
        HBox stats = new HBox(12,
                statChip("SUBMITTED", totalCount),
                statChip("IN PROGRESS", activeCount),
                statChip("RESOLVED", resolvedCount));
        SplitPane split = new SplitPane(table, details);
        split.setDividerPositions(0.58);
        VBox page = ViewSupport.page(ViewSupport.header("My reports",
                "Track the status and cleanup progress of everything you have reported."), stats, split);
        VBox.setVgrow(split, Priority.ALWAYS);
        VBox.setVgrow(page, Priority.ALWAYS);
        getChildren().add(page);
        refresh();
    }

    private HBox statChip(String label, Label value) {
        VBox content = new VBox(2, ViewSupport.styled(new Label(label), "stat-chip-label"), value);
        HBox chip = ViewSupport.styled(new HBox(content), "stat-chip");
        chip.setMinWidth(130);
        return chip;
    }

    // [F23-B04] Load only the citizen's reports and update summary counts
    public void refresh() {
        List<Report> mine = manager.reportsByReporter(citizenId).stream()
                .sorted(Comparator.comparing(Report::getTimestamp).reversed()).toList();
        long resolved = mine.stream().filter(report -> report.getStatus() == Status.RESOLVED).count();
        totalCount.setText(String.valueOf(mine.size()));
        activeCount.setText(String.valueOf(mine.size() - resolved));
        resolvedCount.setText(String.valueOf(resolved));
        table.getItems().setAll(mine);
    }
}
