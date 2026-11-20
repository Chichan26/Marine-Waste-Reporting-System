// FILE 24 of 28: src/main/java/ph/bantaydagat/AdminView.java
// [F24-B01] Package and dependencies
package ph.bantaydagat;

import java.awt.Desktop; // [F24-B01] Open attachments in the system's default app
import java.io.IOException; // [F24-B01] File operation failures
import java.nio.file.Files; // [F24-B01] Check if stored evidence still exists
import java.nio.file.Path; // [F24-B01] Selected photos
import java.util.Comparator; // [F24-B01] Severity, status and category sort
import javafx.geometry.Orientation; // [F24-B01] Split the queue and details vertically
import javafx.scene.control.Button; // [F24-B01] Queue actions
import javafx.scene.control.ButtonType; // [F24-B01] Confirm resolution
import javafx.scene.control.ComboBox; // [F24-B01] Queue filters
import javafx.scene.control.Dialog; // [F24-B01] Cleanup details form
import javafx.scene.control.Label; // [F24-B01] Filter and file labels
import javafx.scene.control.SplitPane; // [F24-B01] Adjustable queue and details
import javafx.scene.control.TableView; // [F24-B01] Admin report queue
import javafx.scene.control.TextArea; // [F24-B01] Report details and notes
import javafx.scene.layout.HBox; // [F24-B01] Action rows
import javafx.scene.layout.Priority; // [F24-B01] Fill available space
import javafx.scene.layout.VBox; // [F24-B01] Main queue layout

public final class AdminView extends VBox {
    // [F24-B02] Incident store and dashboard refresh callback
    private final IncidentManager manager;
    private final Runnable onChanged;

    // [F24-B03] Queue table and filters
    private final TableView<Report> table = ViewSupport.reportTable();
    private final ComboBox<Severity> severity = new ComboBox<>();
    private final ComboBox<Status> status = new ComboBox<>();
    private final ComboBox<WasteCategory> category = new ComboBox<>();
    private final ComboBox<String> sort = new ComboBox<>();

    // [F24-B04] Selected report and available actions
    private final ReportDetailsCard details;
    private final Button advance = ViewSupport.primaryButton("Advance status");
    private final Button attachment = new Button("Open attachment");
    private final Button before = new Button("Before photo");
    private final Button after = new Button("After photo");

    // [F24-B05] Construct the queue, detail panel and actions
    public AdminView(IncidentManager manager, Runnable onChanged) {
        this.manager = manager;
        this.onChanged = onChanged;
        this.details = new ReportDetailsCard(manager);
        setSpacing(16);
        setPadding(new javafx.geometry.Insets(24));
        getStyleClass().add("page");
        SplitPane panels = new SplitPane(table, createDetailsPanel());
        panels.setOrientation(Orientation.HORIZONTAL);
        panels.setDividerPositions(0.58);
        VBox.setVgrow(panels, Priority.ALWAYS);
        getChildren().addAll(ViewSupport.header("Incident queue",
                "Triage incoming reports, dispatch responders and log cleanups."), createFilters(), panels);
        refresh();
    }

    // [F24-B06] Make filter selections refresh the queue
    private HBox createFilters() {
        configureFilter(severity, "All severities", Severity.values());
        configureFilter(status, "All statuses", Status.values());
        configureFilter(category, "All categories", WasteCategory.values());
        sort.getItems().setAll("Severity (highest first)", "Status", "Category", "Newest");
        sort.getSelectionModel().selectFirst();
        sort.setOnAction(event -> refresh());
        Button clear = ViewSupport.ghostButton("Clear filters");
        clear.setOnAction(event -> clearFilters()); // [F24-B06] Restore the full queue
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, ViewSupport.styled(new Label("Filter"), "field-label"), severity, status,
                category, clear, spacer, ViewSupport.styled(new Label("Sort by"), "field-label"), sort);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        bar.setPadding(new javafx.geometry.Insets(12, 16, 12, 16));
        bar.getStyleClass().add("card");
        return bar;
    }

    // [F24-B07] Add enum options and a placeholder for the unfiltered state
    private <T> void configureFilter(ComboBox<T> filter, String prompt, T[] values) {
        filter.setPromptText(prompt);
        filter.getItems().setAll(values);
        filter.setOnAction(event -> refresh());
    }

    // [F24-B08] Clear all three filters
    private void clearFilters() {
        severity.setValue(null);
        status.setValue(null);
        category.setValue(null);
        refresh();
    }

    // [F24-B09] Wire details and actions to the selected report
    private VBox createDetailsPanel() {
        VBox.setVgrow(details, Priority.ALWAYS);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, report) -> showDetails(report));
        advance.setOnAction(event -> advanceSelected()); // [F24-B09] Change the selected status
        attachment.setOnAction(event -> open(selected().getMediaPath()));
        before.setOnAction(event -> open(selected().getResolutionLog().beforePhotoPath()));
        after.setOnAction(event -> open(selected().getResolutionLog().afterPhotoPath()));
        showDetails(null);
        advance.setMaxWidth(Double.MAX_VALUE);
        HBox files = new HBox(8, attachment, before, after);
        VBox actions = new VBox(8, advance, files);
        actions.setPadding(new javafx.geometry.Insets(12, 0, 0, 0));
        VBox panel = new VBox(details, actions);
        panel.setMinWidth(300);
        return panel;
    }

    // [F24-B10] Show the report, resolution log and available actions
    private void showDetails(Report report) {
        details.show(report);
        Status next = report == null ? null : report.getStatus().next();
        advance.setText(next == null ? "Advance status" : "Move to " + next);
        advance.setDisable(next == null);
        attachment.setDisable(report == null || report.getMediaPath() == null);
        ResolutionLog log = report == null ? null : report.getResolutionLog();
        before.setDisable(log == null || log.beforePhotoPath() == null);
        after.setDisable(log == null || log.afterPhotoPath() == null);
    }

    // [F24-B11] Read the current row
    private Report selected() {
        return table.getSelectionModel().getSelectedItem();
    }

    // [F24-B12] Apply filters and retain selection when the report still matches
    public void refresh() {
        Report previous = selected();
        table.getSortOrder().clear();
        table.getItems().setAll(manager.filterReports(severity.getValue(), status.getValue(),
                category.getValue(), order()));
        if (previous != null && table.getItems().contains(previous)) {
            table.getSelectionModel().select(previous);
        }
        showDetails(selected());
    }

    // [F24-B13] Translate the sort control into a report comparator
    private Comparator<Report> order() {
        Comparator<Report> newest = Comparator.comparing(Report::getTimestamp).reversed();
        return switch (sort.getSelectionModel().getSelectedIndex()) {
            case 1 -> Comparator.comparing(Report::getStatus).thenComparing(newest);
            case 2 -> Comparator.comparing((Report report) -> report.getCategory().toString())
                    .thenComparing(newest);
            case 3 -> newest;
            default -> Comparator.comparing(Report::getSeverityLevel).reversed().thenComparing(newest);
        };
    }

    // [F24-B14] Move one step or collect a resolution log first
    private void advanceSelected() {
        Report report = selected();
        if (report == null || report.getStatus().next() == null) {
            return;
        }
        Status next = report.getStatus().next();
        if (next == Status.RESOLVED) {
            showResolutionDialog(report);
        } else {
            update(report, next, null, null, null);
        }
    }

    // [F24-B15] Create the notes and optional before/after photo controls
    private void showResolutionDialog(Report report) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Resolve incident");
        dialog.setHeaderText("Log the cleanup for report #" + report.getReportID().substring(0, 8));
        dialog.getDialogPane().getStylesheets().addAll(getScene().getStylesheets()); // [F24-B15] Same theme
        TextArea notes = new TextArea();
        notes.setPromptText("Describe the cleanup work.");
        notes.setPrefRowCount(4);
        Button beforePhoto = new Button("Choose before photo");
        Button afterPhoto = new Button("Choose after photo");
        wirePhoto(beforePhoto);
        wirePhoto(afterPhoto);
        dialog.getDialogPane().setContent(new VBox(8, new Label("Cleanup notes (required)"), notes,
                beforePhoto, afterPhoto));
        confirmResolution(dialog, report, notes, beforePhoto, afterPhoto);
    }

    // [F24-B16] Keep the dialog open when validation or saving fails
    private void confirmResolution(Dialog<ButtonType> dialog, Report report, TextArea notes,
                                   Button beforePhoto, Button afterPhoto) {
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button confirm = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        confirm.setText("Resolve");
        confirm.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> notes.getText().isBlank(), notes.textProperty()));
        confirm.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!update(report, Status.RESOLVED, notes.getText(),
                    (Path) beforePhoto.getUserData(), (Path) afterPhoto.getUserData())) {
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    // [F24-B17] Store the chosen photo path on its picker button
    private void wirePhoto(Button button) {
        button.setOnAction(event -> {
            Path selected = ViewSupport.chooseFile(button.getScene().getWindow(), true);
            if (selected != null) {
                button.setUserData(selected);
                button.setText(selected.getFileName().toString());
            }
        });
    }

    // [F24-B18] Persist the status and refresh the queue and dashboard
    private boolean update(Report report, Status next, String notes, Path beforePhoto, Path afterPhoto) {
        try {
            manager.updateStatus(report, next, notes, beforePhoto, afterPhoto);
            refresh();
            onChanged.run(); // [F24-B18] Update the charts after the save succeeds
            return true;
        } catch (InvalidStatusTransitionException | InvalidReportException | IOException e) {
            ViewSupport.error(e.getMessage());
            return false;
        }
    }

    // [F24-B19] Open the copied evidence file
    private void open(String path) {
        try {
            Path file = manager.attachmentPath(path);
            if (!Files.isRegularFile(file)) {
                throw new IOException("Attachment file is missing: " + path);
            }
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                throw new IOException("Open this attachment manually: " + file.toAbsolutePath());
            }
            Desktop.getDesktop().open(file.toFile());
        } catch (IOException e) {
            ViewSupport.error(e.getMessage());
        }
    }
}

