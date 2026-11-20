// FILE 23 of 28: src/main/java/ph/bantaydagat/MyReportsView.java
// [F23-B01] Package and dependencies
package ph.bantaydagat;

import java.util.Comparator; // [F23-B01] Most recent reports first
import javafx.scene.control.SplitPane; // [F23-B01] Table beside details
import javafx.scene.control.TableView; // [F23-B01] Citizen history
import javafx.scene.layout.Priority; // [F23-B01] Expand with the window
import javafx.scene.layout.VBox; // [F23-B01] History layout

public final class MyReportsView extends VBox {
    // [F23-B02] Session and report table
    private final IncidentManager manager;
    private final String citizenId;
    private final TableView<Report> table = ViewSupport.reportTable();
    private final ReportDetailsCard details;

    // [F23-B03] Build a read-only history for the signed-in citizen
    public MyReportsView(IncidentManager manager, String citizenId) {
        this.manager = manager;
        this.citizenId = citizenId;
        this.details = new ReportDetailsCard(manager);
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) ->
                details.show(current)); // [F23-B05] Bind selected row
        SplitPane split = new SplitPane(table, details);
        split.setDividerPositions(0.6);
        VBox page = ViewSupport.page(ViewSupport.header("My reports",
                "Track the status of everything you have reported."), split);
        VBox.setVgrow(split, Priority.ALWAYS);
        VBox.setVgrow(page, Priority.ALWAYS);
        getChildren().add(page);
        refresh();
    }

    // [F23-B04] Load only the citizen's reports
    public void refresh() {
        table.getItems().setAll(manager.reportsByReporter(citizenId).stream()
                .sorted(Comparator.comparing(Report::getTimestamp).reversed()).toList());
    }
}
