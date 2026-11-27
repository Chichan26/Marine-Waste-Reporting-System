// FILE: src/main/java/ph/bantaydagat/shared/ReportDetailsCard.java
// [F29-B01] Package and dependencies
package ph.bantaydagat.shared;

import java.nio.file.Files; // [F29-B01] Check that a preview file exists
import java.nio.file.Path; // [F29-B01] Stored evidence location
import javafx.geometry.Insets; // [F29-B01] Card padding
import javafx.geometry.Pos; // [F29-B01] Align stepper nodes
import javafx.scene.Node; // [F29-B01] Generic rows
import javafx.scene.control.Label; // [F29-B01] Keys, values and headings
import javafx.scene.control.ScrollPane; // [F29-B01] Long reports stay readable
import javafx.scene.control.Separator; // [F29-B01] Subtle section dividers
import javafx.scene.image.Image; // [F29-B01] Inline photo preview
import javafx.scene.image.ImageView; // [F29-B01] Display the preview
import javafx.scene.layout.ColumnConstraints; // [F29-B01] Key column width
import javafx.scene.layout.FlowPane; // [F29-B01] Wrap thumbnails
import javafx.scene.layout.GridPane; // [F29-B01] Key/value metadata grid
import javafx.scene.layout.HBox; // [F29-B01] Badge row and stepper
import javafx.scene.layout.Priority; // [F29-B01] Expand stepper connectors
import javafx.scene.layout.Region; // [F29-B01] Stepper dots and lines
import javafx.scene.layout.VBox; // [F29-B01] Card content
import ph.bantaydagat.IncidentManager;
import ph.bantaydagat.Report;
import ph.bantaydagat.ResolutionLog;
import ph.bantaydagat.Status;

public final class ReportDetailsCard extends ScrollPane {
    // [F29-B02] Used to resolve stored attachment paths for previews
    private final IncidentManager manager;
    private final VBox content = new VBox(16);

    // [F29-B03] Build an empty card
    public ReportDetailsCard(IncidentManager manager) {
        this.manager = manager;
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card");
        setContent(content);
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");
        show(null);
    }

    // [F29-B04] Replace the card content with the selected report
    public void show(Report report) {
        content.getChildren().clear();
        if (report == null) {
            content.getChildren().add(ViewSupport.emptyState(
                    Icons.DOCUMENT,
                    "No report selected",
                    "Select a report from the table to inspect its workflow status, metadata, and evidence."));
            return;
        }
        Label heading = ViewSupport.styled(new Label("Report #" + report.getReportID().substring(0, 8)),
                "detail-title");
        HBox badges = new HBox(8, ViewSupport.badge(report.getSeverityLevel()), ViewSupport.badge(report.getStatus()));
        content.getChildren().addAll(
                new VBox(8, heading, badges),
                workflowStepper(report.getStatus()),
                metadata(report),
                new Separator(),
                description(report));
        if (report.getMediaPath() != null) {
            content.getChildren().addAll(new Separator(),
                    section("Attachment", thumbnail("Reported evidence", report.getMediaPath())));
        }
        if (report.getResolutionLog() != null) {
            content.getChildren().addAll(new Separator(), resolution(report.getResolutionLog()));
        }
    }

    // [F29-B04] Four-stage visual progress indicator for the incident lifecycle
    private HBox workflowStepper(Status current) {
        Status[] stages = Status.values();
        int currentIndex = current.ordinal();
        HBox bar = new HBox(4);
        bar.setAlignment(Pos.CENTER);
        bar.getStyleClass().add("stepper");
        for (int i = 0; i < stages.length; i++) {
            Region dot = ViewSupport.styled(new Region(), "step-dot");
            if (i < currentIndex || (i == currentIndex && current == Status.RESOLVED)) {
                dot.getStyleClass().add("step-dot-done");
            } else if (i == currentIndex) {
                dot.getStyleClass().add("step-dot-current");
            } else {
                dot.getStyleClass().add("step-dot-pending");
            }
            Label label = ViewSupport.styled(new Label(stages[i].toString()), "step-label");
            if (i == currentIndex) {
                label.getStyleClass().add("step-label-active");
            }
            VBox step = new VBox(5, dot, label);
            step.setAlignment(Pos.CENTER);
            bar.getChildren().add(step);
            if (i < stages.length - 1) {
                Region line = ViewSupport.styled(new Region(), "step-line");
                if (i < currentIndex) {
                    line.getStyleClass().add("step-line-done");
                }
                HBox.setHgrow(line, Priority.ALWAYS);
                HBox.setMargin(line, new Insets(0, 0, 14, 0));
                bar.getChildren().add(line);
            }
        }
        return bar;
    }

    // [F29-B05] Category, time, location and reporter
    private GridPane metadata(Report report) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        ColumnConstraints keys = new ColumnConstraints();
        keys.setMinWidth(92);
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

    // [F29-B07] Free-text description in a subtle surface container
    private VBox description(Report report) {
        Label text = new Label(report.getDescription() == null || report.getDescription().isBlank()
                ? "No description provided." : report.getDescription());
        text.setWrapText(true);
        text.setMaxWidth(560);
        VBox box = ViewSupport.styled(new VBox(text), "description-box");
        return section("Description", box);
    }

    // [F29-B08] Cleanup notes and before/after photos
    private VBox resolution(ResolutionLog log) {
        Label when = ViewSupport.styled(new Label("Resolved " + ViewSupport.DATE.format(log.resolvedAt())), "muted");
        Label notes = new Label(log.notes());
        notes.setWrapText(true);
        notes.setMaxWidth(560);
        VBox notesBox = ViewSupport.styled(new VBox(notes), "description-box");
        FlowPane photos = new FlowPane(12, 12);
        if (log.beforePhotoPath() != null) {
            photos.getChildren().add(thumbnail("Before", log.beforePhotoPath()));
        }
        if (log.afterPhotoPath() != null) {
            photos.getChildren().add(thumbnail("After", log.afterPhotoPath()));
        }
        VBox box = section("Cleanup log", when, notesBox);
        if (!photos.getChildren().isEmpty()) {
            box.getChildren().add(photos);
        }
        return box;
    }

    // [F29-B09] Heading followed by its content
    private VBox section(String title, Node... children) {
        VBox box = new VBox(8, ViewSupport.sectionTitle(title));
        box.getChildren().addAll(children);
        return box;
    }

    // [F29-B10] Inline image preview, or the file name for videos and missing files
    private VBox thumbnail(String caption, String storedPath) {
        VBox box = new VBox(6, ViewSupport.styled(new Label(caption.toUpperCase()), "field-label"));
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
