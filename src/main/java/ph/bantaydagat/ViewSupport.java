// FILE 20 of 28: src/main/java/ph/bantaydagat/ViewSupport.java
// [F20-B01] Package and dependencies
package ph.bantaydagat;

import java.io.File; // [F20-B01] FileChooser returns a File
import java.nio.file.Path; // [F20-B01] Open a copied attachment
import java.time.format.DateTimeFormatter; // [F20-B01] Readable submission dates
import java.util.Locale; // [F20-B01] Stable CSS class names
import java.util.function.Function; // [F20-B01] Extract each column value
import javafx.beans.property.ReadOnlyObjectWrapper; // [F20-B01] Adapt a model value to JavaFX
import javafx.geometry.Insets; // [F20-B01] View padding
import javafx.scene.Node; // [F20-B01] Nodes passed into layouts
import javafx.scene.control.Alert; // [F20-B01] Show validation and file errors
import javafx.scene.control.Button; // [F20-B01] Styled buttons
import javafx.scene.control.Label; // [F20-B01] Titles and badges
import javafx.scene.control.TableCell; // [F20-B01] Badge cells
import javafx.scene.control.TableColumn; // [F20-B01] Table column definitions
import javafx.scene.control.TableView; // [F20-B01] Shared table setup
import javafx.scene.layout.HBox; // [F20-B01] Banner layout
import javafx.scene.layout.VBox; // [F20-B01] Shared vertical layout
import javafx.stage.FileChooser; // [F20-B01] Pick report and cleanup files
import javafx.stage.Window; // [F20-B01] Owner of the file picker

final class ViewSupport {
    // [F20-B02] Utility constructor and shared date format
    private ViewSupport() {}

    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a", Locale.ENGLISH);

    // [F20-B03] Standard padding for each view
    static VBox page(Node... children) {
        VBox box = new VBox(16, children);
        box.setPadding(new Insets(24));
        box.getStyleClass().add("page");
        return box;
    }

    // [F20-B04] Standard view heading
    static Label title(String text) {
        return styled(new Label(text), "page-title");
    }

    // [F20-B04] Title with a short explanation underneath
    static VBox header(String title, String subtitle) {
        Label sub = styled(new Label(subtitle), "page-subtitle");
        sub.setWrapText(true);
        return new VBox(4, title(title), sub);
    }

    // [F20-B04] Bold heading inside a card
    static Label sectionTitle(String text) {
        return styled(new Label(text), "section-title");
    }

    // [F20-B04] White rounded surface with a soft shadow
    static VBox card(Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("card");
        return box;
    }

    // [F20-B04] Pair a control with a small uppercase label
    static VBox field(String label, Node control) {
        return new VBox(6, styled(new Label(label.toUpperCase(Locale.ROOT)), "field-label"), control);
    }

    // [F20-B04] Filled call-to-action button
    static Button primaryButton(String text) {
        Button button = styled(new Button(text), "btn-primary");
        Motion.pressable(button); // [F20-B04] Press-down feedback
        return button;
    }

    // [F20-B04] Text-only button for low-emphasis actions
    static Button ghostButton(String text) {
        return styled(new Button(text), "btn-ghost");
    }

    // [F20-B04] Coloured message strip (info, success or danger)
    static HBox banner(String kind, String text) {
        Label message = new Label(text);
        message.setWrapText(true);
        HBox box = new HBox(message);
        box.getStyleClass().addAll("banner", "banner-" + kind);
        Motion.enter(box); // [F20-B04] Messages fade in instead of popping
        return box;
    }

    // [F20-B04] Pill label coloured by severity or status
    static Label badge(Object value) {
        Label label = new Label(String.valueOf(value));
        label.getStyleClass().add("badge");
        if (value instanceof Enum<?> e) {
            String prefix = value instanceof Severity ? "severity-" : "status-";
            label.getStyleClass().add(prefix + e.name().toLowerCase(Locale.ROOT).replace('_', '-'));
        }
        return label;
    }

    // [F20-B04] Add a CSS class and return the node for chaining
    static <T extends Node> T styled(T node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
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
        table.setPlaceholder(styled(new Label("No matching reports yet."), "empty-state"));
        TableColumn<Report, String> id = column("REPORT ID", report -> "#" + report.getReportID().substring(0, 8));
        id.getStyleClass().add("mono");
        table.getColumns().add(id);
        table.getColumns().add(column("SUBMITTED", report -> DATE.format(report.getTimestamp())));
        table.getColumns().add(column("CATEGORY", Report::getCategory));
        table.getColumns().add(badgeColumn("SEVERITY", Report::getSeverityLevel));
        table.getColumns().add(badgeColumn("STATUS", Report::getStatus));
        table.getColumns().add(column("LOCATION", Report::getLocation));
        return table;
    }

    // [F20-B07] Connect a table column to a getter
    private static <T> TableColumn<Report, T> column(String name, Function<Report, T> getter) {
        TableColumn<Report, T> column = new TableColumn<>(name);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getter.apply(cell.getValue())));
        return column;
    }

    // [F20-B07] Render the column value as a coloured badge
    private static <T> TableColumn<Report, T> badgeColumn(String name, Function<Report, T> getter) {
        TableColumn<Report, T> column = column(name, getter);
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : badge(item));
            }
        });
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

    // [F20-B09] Image extensions that JavaFX can preview inline
    static boolean isPreviewableImage(String path) {
        String lower = path == null ? "" : path.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif");
    }
}
