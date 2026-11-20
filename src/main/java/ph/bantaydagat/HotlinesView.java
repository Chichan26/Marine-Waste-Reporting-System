// FILE 26 of 28: src/main/java/ph/bantaydagat/HotlinesView.java
// [F26-B01] Package and dependencies
package ph.bantaydagat;

import java.util.function.Consumer; // [F26-B01] Open official source pages
import javafx.scene.control.Hyperlink; // [F26-B01] Official source link
import javafx.scene.control.Label; // [F26-B01] Service and phone labels
import javafx.scene.control.ScrollPane; // [F26-B01] Small windows
import javafx.scene.layout.FlowPane; // [F26-B01] Wrapping card grid
import javafx.scene.layout.VBox; // [F26-B01] Page layout

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
            Label name = ViewSupport.styled(new Label(hotline.service()), "hotline-name");
            name.setWrapText(true);
            Label number = ViewSupport.styled(new Label(hotline.number()), "hotline-number");
            Hyperlink source = new Hyperlink("Official source ↗");
            source.setOnAction(event -> openSource.accept(hotline.source()));
            VBox card = ViewSupport.card(name, number, source);
            card.setSpacing(8);
            card.setPrefWidth(280);
            grid.getChildren().add(card);
        }
        return grid;
    }
}
