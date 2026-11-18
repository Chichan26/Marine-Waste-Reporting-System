// FILE 20 of 28: src/main/java/ph/bantaydagat/ViewSupport.java
// [F20-B01] Package and dependencies
package ph.bantaydagat;

import java.io.File; // [F20-B01] FileChooser returns a File
import java.nio.file.Path; // [F20-B01] Open a copied attachment
import java.util.function.Function; // [F20-B01] Extract each column value
import javafx.beans.property.ReadOnlyObjectWrapper; // [F20-B01] Adapt a model value to JavaFX
import javafx.geometry.Insets; // [F20-B01] View padding
import javafx.scene.Node; // [F20-B01] Nodes passed into layouts
import javafx.scene.control.Alert; // [F20-B01] Show validation and file errors
import javafx.scene.control.Label; // [F20-B01] Titles
import javafx.scene.control.TableColumn; // [F20-B01] Table column definitions
import javafx.scene.control.TableView; // [F20-B01] Shared table setup
import javafx.scene.layout.VBox; // [F20-B01] Shared vertical layout
import javafx.stage.FileChooser; // [F20-B01] Pick report and cleanup files
import javafx.stage.Window; // [F20-B01] Owner of the file picker

final class ViewSupport {
    // [F20-B02] Utility constructor
    private ViewSupport() {}

    // [F20-B03] Standard padding for each view
    static VBox page(Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(20));
        return box;
    }

    // [F20-B04] Standard view heading
    static Label title(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #13566a;");
        return label;
    }

    // [F20-B05] Show a readable error popup
    static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait(); // [F20-B05] Present the error to the user
    }

    // [F20-B06] Attach columns and an empty state to a report table
    static TableView<Report> reportTable() {
        TableView<Report> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No matching reports."));
        table.getColumns().add(column("Report ID", report -> report.getReportID().substring(0, 8)));
        table.getColumns().add(column("Category", Report::getCategory));
        table.getColumns().add(column("Severity", Report::getSeverityLevel));
        table.getColumns().add(column("Status", Report::getStatus));
        table.getColumns().add(column("Location", Report::getLocation));
        return table;
    }

    // [F20-B07] Connect a table column to a getter
    private static <T> TableColumn<Report, T> column(String name, Function<Report, T> getter) {
        TableColumn<Report, T> column = new TableColumn<>(name);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getter.apply(cell.getValue())));
        return column;
    }

    // [F20-B08] Open the native file picker with supported extensions
    static Path chooseFile(Window owner, boolean photoOnly) {
        FileChooser picker = new FileChooser();
        picker.setTitle(photoOnly ? "Choose a cleanup photo" : "Choose a report attachment");
        picker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Photos",
                "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        if (!photoOnly) {
            picker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Videos",
                    "*.mp4", "*.mov", "*.m4v", "*.webm"));
        }
        File selected = picker.showOpenDialog(owner); // [F20-B08] Display the picker
        return selected == null ? null : selected.toPath();
    }
}

