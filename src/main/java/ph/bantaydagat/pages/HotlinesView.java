// FILE 26 of 28: src/main/java/ph/bantaydagat/pages/HotlinesView.java
// [F26-B01] Package and dependencies
package ph.bantaydagat.pages;

import java.util.function.Consumer; // [F26-B01] Open official source pages
import javafx.animation.PauseTransition; // [F26-B01] Reset "Copied!" confirmation
import javafx.geometry.Pos; // [F26-B01] Align card footer actions
import javafx.scene.control.Button; // [F26-B01] One-click copy number action
import javafx.scene.control.Hyperlink; // [F26-B01] Official source link
import javafx.scene.control.Label; // [F26-B01] Service and phone labels
import javafx.scene.control.ScrollPane; // [F26-B01] Small windows
import javafx.scene.input.Clipboard; // [F26-B01] Copy phone number
import javafx.scene.input.ClipboardContent; // [F26-B01] Clipboard payload
import javafx.scene.layout.FlowPane; // [F26-B01] Wrapping card grid
import javafx.scene.layout.HBox; // [F26-B01] Card action row
import javafx.scene.layout.Priority; // [F26-B01] Push actions to edges
import javafx.scene.layout.Region; // [F26-B01] Flexible spacer
import javafx.scene.layout.VBox; // [F26-B01] Page layout
import javafx.util.Duration; // [F26-B01] Feedback timeout
import ph.bantaydagat.HotlinesDirectory;
import ph.bantaydagat.shared.Icons;
import ph.bantaydagat.shared.Motion;
import ph.bantaydagat.shared.ViewSupport;

public final class HotlinesView extends ScrollPane {
    // [F26-B02] Display the directory and a note about emergency dispatch
    public HotlinesView(HotlinesDirectory directory, Consumer<String> openSource) {
        VBox page = ViewSupport.page(ViewSupport.header("Emergency hotlines",
                        "Official contacts for marine pollution, rescue and local response."),
                ViewSupport.banner("danger", "In immediate danger? Call 911 or the nearest hotline below directly."),
                createGrid(directory, openSource));
        setContent(page);
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");
    }

    // [F26-B03] Create a card for each stored contact
    private FlowPane createGrid(HotlinesDirectory directory, Consumer<String> openSource) {
        FlowPane grid = new FlowPane(16, 16);
        for (HotlinesDirectory.Hotline hotline : directory.getHotlines()) {
            boolean emergency = "911".equals(hotline.number().trim());
            Label name = ViewSupport.styled(new Label(hotline.service()), "hotline-name");
            name.setWrapText(true);
            Label number = ViewSupport.styled(new Label(hotline.number()), "hotline-number");

            Hyperlink source = new Hyperlink("Official source ↗");
            source.setOnAction(event -> openSource.accept(hotline.source()));

            Button copy = copyButton(hotline.number());
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox footer = new HBox(8, source, spacer, copy);
            footer.setAlignment(Pos.CENTER_LEFT);

            VBox card = ViewSupport.card(name, number, footer);
            card.setSpacing(10);
            card.setPrefWidth(300);
            card.getStyleClass().addAll("hotline-card", "card-interactive");
            if (emergency) {
                card.getStyleClass().add("hotline-emergency");
            }
            Motion.hoverLift(card); // [F26-B03] Gentle lift on hover
            grid.getChildren().add(card);
        }
        return grid;
    }

    private Button copyButton(String phoneNumber) {
        Button copy = ViewSupport.ghostButton("Copy");
        copy.getStyleClass().add("btn-sm");
        copy.setGraphic(Icons.of(Icons.COPY, 13, "icon-accent"));
        copy.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(phoneNumber);
            Clipboard.getSystemClipboard().setContent(content);
            copy.setText("Copied");
            copy.setGraphic(Icons.of(Icons.CHECK, 13, "icon-success"));
            PauseTransition reset = new PauseTransition(Duration.seconds(1.6));
            reset.setOnFinished(e -> {
                copy.setText("Copy");
                copy.setGraphic(Icons.of(Icons.COPY, 13, "icon-accent"));
            });
            reset.play();
        });
        return copy;
    }
}
