// FILE 23 of 28: src/main/java/ph/bantaydagat/MyReportsView.java
// [F23-B01] Package and dependencies
package ph.bantaydagat;

import java.util.Comparator; // [F23-B01] Most recent reports first
import javafx.geometry.Insets; // [F23-B01] View padding
import javafx.scene.control.TableView; // [F23-B01] Citizen history
import javafx.scene.control.TextArea; // [F23-B01] Selected report summary
import javafx.scene.layout.Priority; // [F23-B01] Expand table with the window
import javafx.scene.layout.VBox; // [F23-B01] History layout

public final class MyReportsView extends VBox {
    // [F23-B02] Session and report table
    private final IncidentManager manager;
    private final String citizenId;
    private final TableView<Report> table = ViewSupport.reportTable();

    // [F23-B03] Build a read-only history for the signed-in citizen
    public MyReportsView(IncidentManager manager, String citizenId) {
        this.manager = manager;
        this.citizenId = citizenId;
        setSpacing(12);
        setPadding(new Insets(20));
        getChildren().addAll(ViewSupport.title("My Reports"), table, createDetails());
        VBox.setVgrow(table, Priority.ALWAYS);
        refresh();
    }

    // [F23-B04] Load only the citizen's reports
    public void refresh() {
        table.getItems().setAll(manager.reportsByReporter(citizenId).stream()
                .sorted(Comparator.comparing(Report::getTimestamp).reversed()).toList());
    }

    // [F23-B05] Display details and status for the selected row
    private TextArea createDetails() {
        TextArea details = new TextArea();
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(5);
        details.setPromptText("Select a report to see its details.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) ->
                details.setText(current == null ? "" : current.exportReport())); // [F23-B05] Bind selected row
        return details;
    }
}

