// FILE: src/main/java/ph/bantaydagat/Icons.java
package ph.bantaydagat;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * Crisp vector icons built from SVG paths so they scale cleanly at any DPI
 * and inherit colors from the Ocean design system CSS classes.
 */
final class Icons {
    private Icons() {}

    // 24x24 viewBox paths (clean minimal stroke/fill silhouettes)
    static final String WAVE_EMBLEM = "M12 2L4 5.5v6c0 5.2 3.4 9.9 8 11.5 4.6-1.6 8-6.3 8-11.5v-6L12 2zm0 2.2l6 2.6v4.7c0 4.1-2.6 7.8-6 9.2-3.4-1.4-6-5.1-6-9.2V6.8l6-2.6zm-4.2 8.1c1 .9 2.2.9 3.2 0 .6-.5 1.4-.5 2 0 1 .9 2.2.9 3.2 0l.9 1.1c-1.5 1.3-3.5 1.3-5 0-.1-.1-.2-.1-.3 0-1.5 1.3-3.5 1.3-5 0l1-1.1zm0-2.9c1 .9 2.2.9 3.2 0 .6-.5 1.4-.5 2 0 1 .9 2.2.9 3.2 0l.9 1.1c-1.5 1.3-3.5 1.3-5 0-.1-.1-.2-.1-.3 0-1.5 1.3-3.5 1.3-5 0l1-1.1z";
    static final String DASHBOARD = "M3 3h8v8H3V3zm10 0h8v5h-8V3zM3 13h8v8H3v-8zm10-3h8v11h-8V10z";
    static final String QUEUE = "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2zm0 2v6h4.5l1.5 2h4l1.5-2H20V7H4z";
    static final String REPORT_ADD = "M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 11h3a1 1 0 0 0 0-2h-3V8a1 1 0 0 0-2 0v3H8a1 1 0 0 0 0 2h3v3a1 1 0 0 0 2 0z";
    static final String HISTORY = "M6 3h12a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm2 5v2h8V8H8zm0 4v2h8v-2H8zm0 4v2h5v-2H8z";
    static final String PHONE = "M6.6 10.8a15.1 15.1 0 0 0 6.6 6.6l2.2-2.2a1 1 0 0 1 1-.24 11.4 11.4 0 0 0 3.6.58 1 1 0 0 1 1 1V20a1 1 0 0 1-1 1A17 17 0 0 1 3 4a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1 11.4 11.4 0 0 0 .58 3.6 1 1 0 0 1-.25 1z";
    static final String SUN = "M12 7a5 5 0 1 0 5 5 5 5 0 0 0-5-5zm0-5a1 1 0 0 1 1 1v2a1 1 0 0 1-2 0V3a1 1 0 0 1 1-1zm0 17a1 1 0 0 1 1 1v2a1 1 0 0 1-2 0v-2a1 1 0 0 1 1-1zM4.22 5.64a1 1 0 0 1 1.42-1.42l1.41 1.42a1 1 0 1 1-1.41 1.41zm12.73 12.73a1 1 0 0 1 1.41-1.41l1.42 1.41a1 1 0 0 1-1.42 1.42zM2 12a1 1 0 0 1 1-1h2a1 1 0 0 1 0 2H3a1 1 0 0 1-1-1zm17 0a1 1 0 0 1 1-1h2a1 1 0 0 1 0 2h-2a1 1 0 0 1-1-1zM5.64 19.78a1 1 0 0 1-1.42-1.42l1.42-1.41a1 1 0 0 1 1.41 1.41zm12.73-12.73a1 1 0 0 1-1.41-1.41l1.41-1.42a1 1 0 0 1 1.42 1.42z";
    static final String MOON = "M20.35 15.35A9 9 0 0 1 8.65 3.65a9 9 0 1 0 11.7 11.7z";
    static final String CHECK = "M9.55 17.65L4.3 12.4l1.4-1.4 3.85 3.85 8.75-8.75 1.4 1.4z";
    static final String COPY = "M8 7a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2h-8a2 2 0 0 1-2-2V7zm-4 3v9a2 2 0 0 0 2 2h7v-2H6v-9H4z";
    static final String SEARCH = "M10.5 3a7.5 7.5 0 1 0 4.66 13.38l4.23 4.23 1.41-1.41-4.23-4.23A7.5 7.5 0 0 0 10.5 3zm0 2a5.5 5.5 0 1 1-5.5 5.5A5.5 5.5 0 0 1 10.5 5z";
    static final String DOCUMENT = "M6 2h8l6 6v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm7 1.5V9h5.5L13 3.5zM8 13v2h8v-2H8zm0 4v2h5v-2H8z";
    static final String PAPERCLIP = "M16.5 6v11.5a4 4 0 0 1-8 0V5a2.5 2.5 0 0 1 5 0v10.5a1 1 0 0 1-2 0V6h-1.5v9.5a2.5 2.5 0 0 0 5 0V5a4 4 0 0 0-8 0v12.5a5.5 5.5 0 0 0 11 0V6H16.5z";
    static final String ALERT = "M12 2L1 21h22L12 2zm0 4.5L19.53 19H4.47L12 6.5zM11 10v4h2v-4h-2zm0 6v2h2v-2h-2z";
    static final String INFO = "M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 15h-2v-6h2zm0-8h-2V7h2z";
    static final String CHECK_CIRCLE = "M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm-1.45 14.3L6.3 12.05l1.4-1.4 2.85 2.85 5.75-5.75 1.4 1.4z";
    static final String LOGOUT = "M10 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5v-2H5V5h5V3zm7 4l-1.4 1.4L18.2 11H9v2h9.2l-2.6 2.6L17 17l5-5-5-5z";

    /**
     * Builds a scalable icon inside a fixed square region using -fx-shape so CSS
     * -fx-background-color controls its fill seamlessly across light/dark themes.
     */
    static StackPane of(String svgPath, double size, String... styleClasses) {
        SVGPath shape = new SVGPath();
        shape.setContent(svgPath);
        Region glyph = new Region();
        glyph.setShape(shape);
        glyph.setMinSize(size, size);
        glyph.setPrefSize(size, size);
        glyph.setMaxSize(size, size);
        glyph.getStyleClass().add("icon");
        glyph.getStyleClass().addAll(styleClasses);
        StackPane box = new StackPane(glyph);
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        return box;
    }
}
