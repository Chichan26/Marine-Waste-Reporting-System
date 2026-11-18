// FILE 22 of 28: src/main/java/ph/bantaydagat/ReportFormView.java
// [F22-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F22-B01] Handle copy and save errors
import java.nio.file.Path; // [F22-B01] Selected file stays local until submission
import javafx.scene.control.Button; // [F22-B01] Form actions
import javafx.scene.control.ComboBox; // [F22-B01] Category and severity controls
import javafx.scene.control.Label; // [F22-B01] Form labels and feedback
import javafx.scene.control.ScrollPane; // [F22-B01] Keep the form usable in a small window
import javafx.scene.control.TextArea; // [F22-B01] Incident description
import javafx.scene.control.TextField; // [F22-B01] Latitude and longitude
import javafx.scene.layout.HBox; // [F22-B01] Attachment picker row
import javafx.scene.layout.VBox; // [F22-B01] Form layout

public final class ReportFormView extends ScrollPane {
    // [F22-B02] Session and services
    private final IncidentManager manager;
    private final String reporterId;
    private final Runnable onSaved;

    // [F22-B03] Form fields and optional attachment
    private final ComboBox<WasteCategory> category = new ComboBox<>();
    private final ComboBox<Severity> severity = new ComboBox<>();
    private final TextArea description = new TextArea();
    private final TextField latitude = new TextField();
    private final TextField longitude = new TextField();
    private final Label attachmentName = new Label("No attachment");
    private final Label feedback = new Label();
    private Path attachment;

    // [F22-B04] Wire the form to the current session
    public ReportFormView(IncidentManager manager, String reporterId, Runnable onSaved) {
        this.manager = manager;
        this.reporterId = reporterId; // [F22-B04] Null means anonymous
        this.onSaved = onSaved;
        configureFields();
        setFitToWidth(true);
        setContent(createForm());
    }

    // [F22-B05] Load the enums and configure the description field
    private void configureFields() {
        category.getItems().setAll(WasteCategory.values());
        severity.getItems().setAll(Severity.values());
        latitude.setPromptText("e.g. 13.7500");
        longitude.setPromptText("e.g. 121.0400");
        description.setPromptText("Describe the waste and where you saw it.");
        description.setWrapText(true);
        description.setPrefRowCount(4);
    }

    // [F22-B06] Lay out the reporting fields and submit action
    private VBox createForm() {
        Label note = new Label(reporterId == null
                ? "Anonymous report. Keep names and identifying details out of text and attachments,"
                        + " including file metadata."
                : "This report will appear in My Reports.");
        note.setWrapText(true);
        Button submit = new Button("Submit report");
        submit.setOnAction(event -> submit()); // [F22-B06] Connect the button to the submission logic
        VBox fields = ViewSupport.page(ViewSupport.title("Report marine waste"), note,
                new HBox(12, field("Waste category", category), field("Severity", severity)),
                new HBox(12, field("Latitude (-90 to 90)", latitude), field("Longitude (-180 to 180)", longitude)),
                field("Description", description), createAttachmentRow(), submit, feedback);
        fields.setMaxWidth(780);
        return fields;
    }

    // [F22-B07] Pair a control with its field label
    private VBox field(String label, javafx.scene.Node control) {
        return new VBox(5, new Label(label), control);
    }

    // [F22-B08] Choose or remove an optional attachment
    private HBox createAttachmentRow() {
        Button choose = new Button("Choose photo or video");
        Button remove = new Button("Remove attachment");
        choose.setOnAction(event -> {
            Path selected = ViewSupport.chooseFile(getScene().getWindow(), false);
            if (selected != null) {
                attachment = selected;
                attachmentName.setText(selected.getFileName().toString());
            }
        });
        remove.setOnAction(event -> clearAttachment());
        return new HBox(8, choose, remove, attachmentName);
    }

    // [F22-B09] Submit through IncidentManager and refresh history
    private void submit() {
        try {
            Location location = parseLocation();
            Report report = manager.submitReport(location, severity.getValue(), reporterId,
                    category.getValue(), description.getText().strip(), attachment);
            clearForm();
            feedback.setText("Saved as Submitted. Report ID: " + report.getReportID().substring(0, 8));
            onSaved.run(); // [F22-B09] Refresh views after the save succeeds
        } catch (InvalidReportException | IOException e) {
            ViewSupport.error(e.getMessage());
        }
    }

    // [F22-B10] Parse coordinates; the model enforces the ranges
    private Location parseLocation() throws InvalidReportException {
        try {
            return new Location(Double.parseDouble(latitude.getText().strip()),
                    Double.parseDouble(longitude.getText().strip()));
        } catch (NumberFormatException e) {
            throw new InvalidReportException("Enter numeric latitude and longitude using a decimal point.");
        }
    }

    // [F22-B11] Reset inputs after submission
    private void clearForm() {
        category.getSelectionModel().clearSelection();
        severity.getSelectionModel().clearSelection();
        latitude.clear();
        longitude.clear();
        description.clear();
        clearAttachment();
    }

    // [F22-B12] Drop the local selection without changing any original file
    private void clearAttachment() {
        attachment = null;
        attachmentName.setText("No attachment");
    }
}

