// FILE: src/main/java/ph/bantaydagat/ReportDetailsCard.java
// [F29-B01] Package and dependencies
package ph.bantaydagat;

import java.nio.file.Files; // [F29-B01] Check that a preview file exists
import java.nio.file.Path; // [F29-B01] Stored evidence location
import javafx.geometry.Insets; // [F29-B01] Card padding
import javafx.scene.Node; // [F29-B01] Generic rows
import javafx.scene.control.Label; // [F29-B01] Keys, values and headings
import javafx.scene.control.ScrollPane; // [F29-B01] Long reports stay readable
import javafx.scene.image.Image; // [F29-B01] Inline photo preview
import javafx.scene.image.ImageView; // [F29-B01] Display the preview
import javafx.scene.layout.ColumnConstraints; // [F29-B01] Key column width
import javafx.scene.layout.FlowPane; // [F29-B01] Wrap thumbnails
import javafx.scene.layout.GridPane; // [F29-B01] Key/value metadata grid
import javafx.scene.layout.HBox; // [F29-B01] Badge row
import javafx.scene.layout.VBox; // [F29-B01] Card content

final class ReportDetailsCard extends ScrollPane {
    // [F29-B02] Used to resolve stored attachment paths for previews
    private final IncidentManager manager;
    private final VBox content = new VBox(16);

    // [F29-B03] Build an empty card
    ReportDetailsCard(IncidentManager manager) {
        this.manager = manager;
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card");
        setContent(content);
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");
        show(null);
    }

    // [F29-B04] Replace the card content with the selected report
    void show(Report report) {
        content.getChildren().clear();
        if (report == null) {
            Label empty = ViewSupport.styled(new Label("Select a report to see its details."), "empty-state");
            content.getChildren().add(empty);
            return;
        }
        Label heading = ViewSupport.styled(new Label("Report #" + report.getReportID().substring(0, 8)),
                "detail-title");
        HBox badges = new HBox(8, ViewSupport.badge(report.getSeverityLevel()), ViewSupport.badge(report.getStatus()));
        content.getChildren().addAll(new VBox(8, heading, badges), metadata(report), description(report));
        if (report.getMediaPath() != null) {
            content.getChildren().add(section("Attachment", thumbnail("Reported evidence", report.getMediaPath())));
        }
        if (report.getResolutionLog() != null) {
            content.getChildren().add(resolution(report.getResolutionLog()));
        }
    }

    // [F29-B05] Category, time, location and reporter
    private GridPane metadata(Report report) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        ColumnConstraints keys = new ColumnConstraints(90);
        grid.getColumnConstraints().addAll(keys, new ColumnConstraints());
        addRow(grid, 0, "Category", String.valueOf(report.getCategory()));
        addRow(grid, 1, "Submitted", ViewSupport.DATE.format(report.getTimestamp()));
        addRow(grid, 2, "Location", String.valueOf(report.getLocation()));
        addRow(grid, 3, "Reporter", report.getReporterId() == null ? "Anonymous"
                : "Citizen #" + report.getReporterId().substring(0, Math.min(8, report.getReporterId().length())));
        return grid;
    }

    // [F29-B06] One key/value row
    private void addRow(GridPane grid, int row, String key, String value) {
        Label valueLabel = ViewSupport.styled(new Label(value), "detail-value");
        valueLabel.setWrapText(true);
        grid.addRow(row, ViewSupport.styled(new Label(key), "detail-key"), valueLabel);
    }

    // [F29-B07] Free-text description
    private VBox description(Report report) {
        Label text = new Label(report.getDescription() == null || report.getDescription().isBlank()
                ? "No description provided." : report.getDescription());
        text.setWrapText(true);
        return section("Description", text);
    }

    // [F29-B08] Cleanup notes and before/after photos
    private VBox resolution(ResolutionLog log) {
        Label when = ViewSupport.styled(new Label("Resolved " + ViewSupport.DATE.format(log.resolvedAt())), "muted");
        Label notes = new Label(log.notes());
        notes.setWrapText(true);
        FlowPane photos = new FlowPane(12, 12);
        if (log.beforePhotoPath() != null) {
            photos.getChildren().add(thumbnail("Before", log.beforePhotoPath()));
        }
        if (log.afterPhotoPath() != null) {
            photos.getChildren().add(thumbnail("After", log.afterPhotoPath()));
        }
        VBox box = section("Cleanup log", when, notes);
        if (!photos.getChildren().isEmpty()) {
            box.getChildren().add(photos);
        }
        return box;
    }

    // [F29-B09] Heading followed by its content
    private VBox section(String title, Node... children) {
        VBox box = new VBox(6, ViewSupport.sectionTitle(title));
        box.getChildren().addAll(children);
        return box;
    }

    // [F29-B10] Inline image preview, or the file name for videos and missing files
    private VBox thumbnail(String caption, String storedPath) {
        VBox box = new VBox(6, ViewSupport.styled(new Label(caption), "field-label"));
        box.getStyleClass().add("thumbnail");
        Path file = manager.attachmentPath(storedPath);
        if (ViewSupport.isPreviewableImage(storedPath) && Files.isRegularFile(file)) {
            ImageView view = new ImageView(new Image(file.toUri().toString(), 240, 160, true, true, true));
            view.setPreserveRatio(true);
            box.getChildren().add(view);
        } else {
            String name = file.getFileName() == null ? storedPath : file.getFileName().toString();
            box.getChildren().add(ViewSupport.styled(new Label(name), "muted"));
        }
        return box;
    }
}
