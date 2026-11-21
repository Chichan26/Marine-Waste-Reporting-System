// FILE 22 of 28: src/main/java/ph/bantaydagat/ReportFormView.java
// [F22-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F22-B01] Handle copy and save errors
import java.nio.file.Path; // [F22-B01] Selected file stays local until submission
import javafx.geometry.Pos; // [F22-B01] Align the submit row
import javafx.scene.Node; // [F22-B01] Grow fields evenly
import javafx.scene.control.Button; // [F22-B01] Form actions
import javafx.scene.control.ComboBox; // [F22-B01] Category and severity controls
import javafx.scene.control.Label; // [F22-B01] Form labels and feedback
import javafx.scene.control.ScrollPane; // [F22-B01] Keep the form usable in a small window
import javafx.scene.control.TextArea; // [F22-B01] Incident description
import javafx.scene.control.TextField; // [F22-B01] Latitude and longitude
import javafx.scene.layout.HBox; // [F22-B01] Paired fields and attachment row
import javafx.scene.layout.Priority; // [F22-B01] Stretch fields
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
    private final Label attachmentName = ViewSupport.styled(new Label("No file selected"), "muted");
    private final Button removeAttachment = ViewSupport.ghostButton("Remove");
    private final VBox feedback = new VBox();
    private Path attachment;

    // [F22-B04] Wire the form to the current session
    public ReportFormView(IncidentManager manager, String reporterId, Runnable onSaved) {
        this.manager = manager;
        this.reporterId = reporterId; // [F22-B04] Null means anonymous
        this.onSaved = onSaved;
        configureFields();
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");
        setContent(createForm());
    }

    // [F22-B05] Load the enums and configure the description field
    private void configureFields() {
        category.getItems().setAll(WasteCategory.values());
        severity.getItems().setAll(Severity.values());
        category.setPromptText("Select category");
        severity.setPromptText("Select severity");
        category.setMaxWidth(Double.MAX_VALUE);
        severity.setMaxWidth(Double.MAX_VALUE);
        latitude.setPromptText("e.g. 13.7500");
        longitude.setPromptText("e.g. 121.0400");
        description.setPromptText("What did you see? Include landmarks, size of the spill or amount of waste.");
        description.setWrapText(true);
        description.setPrefRowCount(5);
    }

    // [F22-B06] Lay out the reporting fields in cards
    private VBox createForm() {
        String privacy = reporterId == null
                ? "You are reporting anonymously. Keep names and identifying details out of the text and"
                        + " attachments, including file metadata."
                : "This report will be saved to My Reports so you can follow its progress.";
        VBox what = ViewSupport.card(ViewSupport.sectionTitle("1  ·  What did you find?"),
                row(ViewSupport.field("Waste category", category), ViewSupport.field("Severity", severity)),
                ViewSupport.field("Description", description));
        VBox where = ViewSupport.card(ViewSupport.sectionTitle("2  ·  Where is it?"),
                row(ViewSupport.field("Latitude (-90 to 90)", latitude),
                        ViewSupport.field("Longitude (-180 to 180)", longitude)));
        VBox evidence = ViewSupport.card(ViewSupport.sectionTitle("3  ·  Evidence (optional)"),
                createAttachmentRow());
        Button submit = ViewSupport.primaryButton("Submit report");
        submit.setOnAction(event -> submit()); // [F22-B06] Connect the button to the submission logic
        HBox actions = new HBox(submit);
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox page = ViewSupport.page(ViewSupport.header("Report marine waste",
                        "Tell us what you saw and where. Responders use this to plan the cleanup."),
                ViewSupport.banner("info", privacy), feedback, what, where, evidence, actions);
        page.setMaxWidth(820);
        return page;
    }

    // [F22-B07] Two fields side by side that share the width
    private HBox row(Node left, Node right) {
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox box = new HBox(16, left, right);
        box.getChildren().forEach(child -> ((VBox) child).setMaxWidth(Double.MAX_VALUE));
        return box;
    }

    // [F22-B08] Choose or remove an optional attachment
    private HBox createAttachmentRow() {
        Button choose = new Button("Choose photo or video");
        choose.setOnAction(event -> {
            Path selected = ViewSupport.chooseFile(getScene().getWindow(), false);
            if (selected != null) {
                attachment = selected;
                attachmentName.setText(selected.getFileName().toString());
                removeAttachment.setVisible(true);
            }
        });
        removeAttachment.setOnAction(event -> clearAttachment());
        removeAttachment.setVisible(false);
        HBox box = new HBox(12, choose, attachmentName, removeAttachment);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // [F22-B09] Submit through IncidentManager and refresh history
    private void submit() {
        try {
            Location location = parseLocation();
            Report report = manager.submitReport(location, severity.getValue(), reporterId,
                    category.getValue(), description.getText().strip(), attachment);
            clearForm();
            feedback.getChildren().setAll(ViewSupport.banner("success", "Thank you! Your report was submitted."
                    + "  Reference: #" + report.getReportID().substring(0, 8)));
            setVvalue(0); // [F22-B09] Scroll up so the confirmation is visible
            onSaved.run(); // [F22-B09] Refresh views after the save succeeds
        } catch (InvalidReportException | IOException e) {
            feedback.getChildren().setAll(ViewSupport.banner("danger", e.getMessage()));
            setVvalue(0);
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
        attachmentName.setText("No file selected");
        removeAttachment.setVisible(false);
    }
}
