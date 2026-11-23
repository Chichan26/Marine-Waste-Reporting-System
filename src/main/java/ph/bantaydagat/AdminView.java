// FILE 24 of 28: src/main/java/ph/bantaydagat/AdminView.java
// [F24-B01] Package and dependencies
package ph.bantaydagat;

import java.awt.Desktop; // [F24-B01] Open attachments in the system's default app
import java.io.IOException; // [F24-B01] File operation failures
import java.nio.file.Files; // [F24-B01] Check if stored evidence still exists
import java.nio.file.Path; // [F24-B01] Selected photos
import java.util.Comparator; // [F24-B01] Severity, status and category sort
import java.util.List; // [F24-B01] Filtered report list
import java.util.Locale; // [F24-B01] Case-insensitive search
import javafx.geometry.Insets; // [F24-B01] Spacing and padding
import javafx.geometry.Orientation; // [F24-B01] Split the queue and details vertically
import javafx.geometry.Pos; // [F24-B01] Align filter bar and header
import javafx.scene.control.Button; // [F24-B01] Queue actions
import javafx.scene.control.ButtonType; // [F24-B01] Confirm resolution
import javafx.scene.control.ComboBox; // [F24-B01] Queue filters
import javafx.scene.control.Dialog; // [F24-B01] Cleanup details form
import javafx.scene.control.Label; // [F24-B01] Filter and count labels
import javafx.scene.control.SplitPane; // [F24-B01] Adjustable queue and details
import javafx.scene.control.TableView; // [F24-B01] Admin report queue
import javafx.scene.control.TextArea; // [F24-B01] Report details and notes
import javafx.scene.control.TextField; // [F24-B01] Keyword search input
import javafx.scene.layout.HBox; // [F24-B01] Action and filter rows
import javafx.scene.layout.Priority; // [F24-B01] Fill available space
import javafx.scene.layout.Region; // [F24-B01] Flexible spacers
import javafx.scene.layout.VBox; // [F24-B01] Main queue layout

public final class AdminView extends VBox {
    // [F24-B02] Incident store and dashboard refresh callback
    private final IncidentManager manager;
    private final Runnable onChanged;

    // [F24-B03] Queue table, search and filters
    private final TableView<Report> table = ViewSupport.reportTable();
    private final TextField search = new TextField();
    private final ComboBox<Severity> severity = new ComboBox<>();
    private final ComboBox<Status> status = new ComboBox<>();
    private final ComboBox<WasteCategory> category = new ComboBox<>();
    private final ComboBox<String> sort = new ComboBox<>();
    private final Label resultCount = ViewSupport.styled(new Label("0 reports"), "count-pill");

    // [F24-B04] Selected report and available actions
    private final ReportDetailsCard details;
    private final Button advance = ViewSupport.primaryButton("Advance status");
    private final Button attachment = ViewSupport.styled(new Button("Attachment"), "btn-sm");
    private final Button before = ViewSupport.styled(new Button("Before photo"), "btn-sm");
    private final Button after = ViewSupport.styled(new Button("After photo"), "btn-sm");

    // [F24-B05] Construct the queue, detail panel and actions
    public AdminView(IncidentManager manager, Runnable onChanged) {
        this.manager = manager;
        this.onChanged = onChanged;
        this.details = new ReportDetailsCard(manager);
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        getStyleClass().add("page");
        SplitPane panels = new SplitPane(table, createDetailsPanel());
        panels.setOrientation(Orientation.HORIZONTAL);
        panels.setDividerPositions(0.58);
        VBox.setVgrow(panels, Priority.ALWAYS);
        getChildren().addAll(createHeader(), createFilters(), panels);
        refresh();
    }

    // [F24-B05] Page heading paired with live match count pill
    private HBox createHeader() {
        VBox heading = ViewSupport.header("Incident queue",
                "Triage incoming reports, dispatch responders and log cleanups.");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(12, heading, spacer, resultCount);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // [F24-B06] Search box + dropdown filters + sort bar
    private VBox createFilters() {
        search.setPromptText("Search ID, category, location, or description...");
        search.textProperty().addListener((obs, old, text) -> refresh());
        HBox.setHgrow(search, Priority.ALWAYS);
        HBox searchWrap = ViewSupport.styled(new HBox(6, Icons.of(Icons.SEARCH, 14), search), "search-box");
        searchWrap.setPrefWidth(260);
        HBox.setHgrow(searchWrap, Priority.ALWAYS);

        configureFilter(severity, "All severities", Severity.values());
        configureFilter(status, "All statuses", Status.values());
        configureFilter(category, "All categories", WasteCategory.values());
        sort.getItems().setAll("Severity (highest first)", "Status", "Category", "Newest");
        sort.getSelectionModel().selectFirst();
        sort.setOnAction(event -> refresh());

        Button clear = ViewSupport.ghostButton("Reset");
        clear.getStyleClass().add("btn-sm");
        clear.setOnAction(event -> clearFilters()); // [F24-B06] Restore the full queue

        HBox row1 = new HBox(10, searchWrap, ViewSupport.styled(new Label("SORT"), "field-label"), sort);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(10, ViewSupport.styled(new Label("FILTER"), "field-label"),
                severity, status, category, clear);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox bar = new VBox(10, row1, row2);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.getStyleClass().add("card");
        return bar;
    }

    // [F24-B07] Add enum options and a placeholder for the unfiltered state
    private <T> void configureFilter(ComboBox<T> filter, String prompt, T[] values) {
        filter.setPromptText(prompt);
        filter.getItems().setAll(values);
        filter.setOnAction(event -> refresh());
    }

    // [F24-B08] Clear search and all three filters
    private void clearFilters() {
        search.clear();
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
        attachment.setGraphic(Icons.of(Icons.PAPERCLIP, 13, "icon-primary"));
        showDetails(null);
        advance.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(attachment, Priority.ALWAYS);
        HBox.setHgrow(before, Priority.ALWAYS);
        HBox.setHgrow(after, Priority.ALWAYS);
        attachment.setMaxWidth(Double.MAX_VALUE);
        before.setMaxWidth(Double.MAX_VALUE);
        after.setMaxWidth(Double.MAX_VALUE);
        HBox files = new HBox(8, attachment, before, after);
        VBox actions = new VBox(8, advance, files);
        actions.setPadding(new Insets(12, 0, 0, 0));
        VBox panel = new VBox(details, actions);
        panel.setMinWidth(320);
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

    // [F24-B12] Apply filters, keyword search and retain selection when the report still matches
    public void refresh() {
        Report previous = selected();
        table.getSortOrder().clear();
        String query = search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
        List<Report> filtered = manager.filterReports(severity.getValue(), status.getValue(),
                        category.getValue(), order()).stream()
                .filter(report -> matchesQuery(report, query))
                .toList();
        int total = manager.getReports().size();
        resultCount.setText(filtered.size() == total
                ? total + (total == 1 ? " report" : " reports")
                : filtered.size() + " of " + total + " reports");
        table.getItems().setAll(filtered);
        if (previous != null && table.getItems().contains(previous)) {
            table.getSelectionModel().select(previous);
        }
        showDetails(selected());
    }

    private static boolean matchesQuery(Report report, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return report.getReportID().toLowerCase(Locale.ROOT).contains(query)
                || String.valueOf(report.getCategory()).toLowerCase(Locale.ROOT).contains(query)
                || String.valueOf(report.getLocation()).toLowerCase(Locale.ROOT).contains(query)
                || (report.getDescription() != null
                        && report.getDescription().toLowerCase(Locale.ROOT).contains(query));
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
        ViewSupport.applyDialogTheme(dialog, getScene()); // [F24-B15] Same light/dark theme
        TextArea notes = new TextArea();
        notes.setPromptText("Describe the cleanup work, team involved, and waste collected.");
        notes.setWrapText(true);
        notes.setPrefRowCount(4);
        Button beforePhoto = new Button("Choose before photo");
        Button afterPhoto = new Button("Choose after photo");
        wirePhoto(beforePhoto);
        wirePhoto(afterPhoto);
        HBox photos = new HBox(10, beforePhoto, afterPhoto);
        VBox body = new VBox(12,
                ViewSupport.field("Cleanup notes (required)", notes),
                ViewSupport.field("Evidence photos (optional)", photos));
        body.setPadding(new Insets(12, 4, 4, 4));
        body.setPrefWidth(420);
        dialog.getDialogPane().setContent(body);
        confirmResolution(dialog, report, notes, beforePhoto, afterPhoto);
    }

    // [F24-B16] Keep the dialog open when validation or saving fails
    private void confirmResolution(Dialog<ButtonType> dialog, Report report, TextArea notes,
                                   Button beforePhoto, Button afterPhoto) {
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button confirm = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        confirm.setText("Resolve incident");
        confirm.getStyleClass().add("btn-primary");
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
