// FILE 22 of 28: src/main/java/ph/bantaydagat/pages/ReportFormView.java
// [F22-B01] Package and dependencies
package ph.bantaydagat.pages;

import java.io.IOException; // [F22-B01] Handle copy and save errors
import java.nio.file.Files; // [F22-B01] Verify previewable local file
import java.nio.file.Path; // [F22-B01] Selected file stays local until submission
import javafx.geometry.Pos; // [F22-B01] Align the submit row and center form
import javafx.scene.Node; // [F22-B01] Grow fields evenly
import javafx.scene.control.Button; // [F22-B01] Form actions
import javafx.scene.control.ComboBox; // [F22-B01] Category and severity controls
import javafx.scene.control.Label; // [F22-B01] Form labels and feedback
import javafx.scene.control.ScrollPane; // [F22-B01] Keep the form usable in a small window
import javafx.scene.control.TextArea; // [F22-B01] Incident description
import javafx.scene.control.TextField; // [F22-B01] Latitude and longitude
import javafx.scene.image.Image; // [F22-B01] Live photo preview
import javafx.scene.image.ImageView; // [F22-B01] Display photo preview
import javafx.scene.layout.HBox; // [F22-B01] Paired fields and attachment row
import javafx.scene.layout.Priority; // [F22-B01] Stretch fields
import javafx.scene.layout.StackPane; // [F22-B01] Center the form in wide windows
import javafx.scene.layout.VBox; // [F22-B01] Form layout
import ph.bantaydagat.IncidentManager;
import ph.bantaydagat.InvalidReportException;
import ph.bantaydagat.Location;
import ph.bantaydagat.Report;
import ph.bantaydagat.Severity;
import ph.bantaydagat.WasteCategory;
import ph.bantaydagat.shared.Icons;
import ph.bantaydagat.shared.Motion;
import ph.bantaydagat.shared.ViewSupport;

public final class ReportFormView extends ScrollPane {
    // [F22-B02] Session and services
    private final IncidentManager manager;
    private final String reporterId;
    private final Runnable onSaved;

    // [F22-B03] Form fields and optional attachment
    private final ComboBox<WasteCategory> category = new ComboBox<>();
    private final ComboBox<Severity> severity = new ComboBox<>();
    private final TextArea description = new TextArea();
    private final Label charCount = ViewSupport.styled(new Label("0 characters"), "kpi-caption");
    private final TextField latitude = new TextField();
    private final TextField longitude = new TextField();
    private final Label attachmentName = ViewSupport.styled(new Label("No file selected"), "muted");
    private final Button removeAttachment = ViewSupport.ghostButton("Remove");
    private final VBox previewBox = new VBox();
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
        StackPane wrapper = new StackPane(createForm());
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("page");
        setContent(wrapper);
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
        description.textProperty().addListener((obs, old, text) -> {
            int len = text == null ? 0 : text.trim().length();
            charCount.setText(len + (len == 1 ? " character" : " characters"));
        });
    }

    // [F22-B06] Lay out the reporting fields in numbered step cards
    private VBox createForm() {
        String privacy = reporterId == null
                ? "You are reporting anonymously. Keep names and identifying details out of the text and"
                        + " attachments, including file metadata."
                : "This report will be saved to My Reports so you can follow its progress.";
        HBox countRow = new HBox(charCount);
        countRow.setAlignment(Pos.CENTER_RIGHT);
        VBox descField = ViewSupport.field("Description", new VBox(4, description, countRow));
        VBox what = ViewSupport.card(ViewSupport.sectionStep(1, "What did you find?"),
                row(ViewSupport.field("Waste category", category), ViewSupport.field("Severity", severity)),
                descField);
        VBox where = ViewSupport.card(ViewSupport.sectionStep(2, "Where is it?"),
                row(ViewSupport.field("Latitude (-90 to 90)", latitude),
                        ViewSupport.field("Longitude (-180 to 180)", longitude)));
        VBox evidence = ViewSupport.card(ViewSupport.sectionStep(3, "Evidence (optional)"),
                createAttachmentRow(), previewBox);
        Button submit = ViewSupport.primaryButton("Submit report");
        submit.setOnAction(event -> submit()); // [F22-B06] Connect the button to the submission logic
        HBox actions = new HBox(submit);
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox page = ViewSupport.page(ViewSupport.header("Report marine waste",
                        "Tell us what you saw and where. Responders use this to plan the cleanup."),
                ViewSupport.banner("info", privacy), feedback, what, where, evidence, actions);
        page.setMaxWidth(840);
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

    // [F22-B08] Choose or remove an optional attachment with live image preview
    private HBox createAttachmentRow() {
        Button choose = new Button("Choose photo or video");
        choose.setGraphic(Icons.of(Icons.PAPERCLIP, 14, "icon-primary"));
        choose.setOnAction(event -> {
            Path selected = ViewSupport.chooseFile(getScene().getWindow(), false);
            if (selected != null) {
                attachment = selected;
                attachmentName.setText(selected.getFileName().toString());
                removeAttachment.setVisible(true);
                updatePreview(selected);
            }
        });
        removeAttachment.setOnAction(event -> clearAttachment());
        removeAttachment.setVisible(false);
        HBox box = new HBox(12, choose, attachmentName, removeAttachment);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void updatePreview(Path file) {
        previewBox.getChildren().clear();
        if (file != null && ViewSupport.isPreviewableImage(file.toString()) && Files.isRegularFile(file)) {
            ImageView img = new ImageView(new Image(file.toUri().toString(), 260, 170, true, true, true));
            img.setPreserveRatio(true);
            VBox frame = ViewSupport.styled(new VBox(6,
                    ViewSupport.styled(new Label("PREVIEW"), "field-label"), img), "thumbnail");
            frame.setMaxWidth(VBox.USE_PREF_SIZE);
            previewBox.getChildren().add(frame);
            Motion.enter(frame);
        }
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
        previewBox.getChildren().clear();
    }
}
