// FILE 26 of 28: src/main/java/ph/bantaydagat/HotlinesView.java
// [F26-B01] Package and dependencies
package ph.bantaydagat;

import java.util.function.Consumer; // [F26-B01] Open official source pages
import javafx.geometry.Insets; // [F26-B01] Directory padding
import javafx.scene.control.Hyperlink; // [F26-B01] Official source link
import javafx.scene.control.Label; // [F26-B01] Service and phone labels
import javafx.scene.layout.GridPane; // [F26-B01] Directory rows and columns
import javafx.scene.layout.VBox; // [F26-B01] Page layout

public final class HotlinesView extends VBox {
    // [F26-B02] Display the directory and a note about emergency dispatch
    public HotlinesView(HotlinesDirectory directory, Consumer<String> openSource) {
        setSpacing(12);
        setPadding(new Insets(20));
        getChildren().addAll(ViewSupport.title("Emergency hotlines"),
                new Label("For immediate danger, call the appropriate hotline directly."),
                createGrid(directory, openSource));
    }

    // [F26-B03] Create a row for each stored contact
    private GridPane createGrid(HotlinesDirectory directory, Consumer<String> openSource) {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);
        grid.addRow(0, new Label("Service"), new Label("Phone"), new Label("Reference"));
        int row = 1;
        for (HotlinesDirectory.Hotline hotline : directory.getHotlines()) {
            Hyperlink source = new Hyperlink("Official source");
            source.setOnAction(event -> openSource.accept(hotline.source()));
            grid.addRow(row++, new Label(hotline.service()), new Label(hotline.number()), source);
        }
        return grid;
    }
}

