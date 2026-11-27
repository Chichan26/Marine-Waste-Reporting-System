// FILE 29 of 29: src/main/java/ph/bantaydagat/shared/Motion.java
// [F29-B01] Package and dependencies
package ph.bantaydagat.shared;

import java.util.List; // [F29-B01] Staggered groups
import java.util.function.LongFunction; // [F29-B01] Format animated numbers
import javafx.animation.Animation; // [F29-B01] Looping wave animation
import javafx.animation.FadeTransition; // [F29-B01] Opacity changes
import javafx.animation.Interpolator; // [F29-B01] Easing curves
import javafx.animation.KeyFrame; // [F29-B01] Count-up frames
import javafx.animation.KeyValue; // [F29-B01] Count-up target
import javafx.animation.ParallelTransition; // [F29-B01] Fade and slide together
import javafx.animation.ScaleTransition; // [F29-B01] Button press feedback
import javafx.animation.Timeline; // [F29-B01] Count-up and shake
import javafx.animation.TranslateTransition; // [F29-B01] Slides, lifts and waves
import javafx.beans.property.SimpleLongProperty; // [F29-B01] Animated counter value
import javafx.scene.Node; // [F29-B01] Anything can be animated
import javafx.scene.control.Label; // [F29-B01] KPI numbers
import javafx.scene.layout.Pane; // [F29-B01] Wave layer
import javafx.scene.shape.ClosePath; // [F29-B01] Wave shape
import javafx.scene.shape.CubicCurveTo; // [F29-B01] Wave shape
import javafx.scene.shape.LineTo; // [F29-B01] Wave shape
import javafx.scene.shape.MoveTo; // [F29-B01] Wave shape
import javafx.scene.shape.Path; // [F29-B01] Wave shape
import javafx.scene.shape.Rectangle; // [F29-B01] Clip the wave layer
import javafx.util.Duration; // [F29-B01] Timing tokens

/**
 * Motion tokens and reusable animations for the Ocean design system.
 * All motion is subtle (150-300 ms, ease-out) except the ambient login waves.
 * Start the app with -Dbantaydagat.motion=off to disable every animation (reduced motion).
 */
public final class Motion {
    // [F29-B02] Motion tokens (mirrored in design/design-tokens.json -> primitive.motion)
    public static final Duration FAST = Duration.millis(150);
    public static final Duration NORMAL = Duration.millis(220);
    public static final Duration SLOW = Duration.millis(300);
    public static final Duration COUNT = Duration.millis(700);
    public static final Duration STAGGER = Duration.millis(60);
    public static final Interpolator EASE_OUT = Interpolator.SPLINE(0.16, 1, 0.3, 1);
    public static final Interpolator EASE_IN_OUT = Interpolator.SPLINE(0.65, 0, 0.35, 1);
    public static final double ENTER_OFFSET = 10;

    public static final boolean ENABLED = !"off".equalsIgnoreCase(System.getProperty("bantaydagat.motion"));

    private Motion() {}

    // [F29-B03] Fade in while rising a few pixels; used for screens, cards and banners
    public static void enter(Node node) {
        enter(node, Duration.ZERO);
    }

    public static void enter(Node node, Duration delay) {
        if (!ENABLED) {
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(ENTER_OFFSET);
        FadeTransition fade = new FadeTransition(SLOW, node);
        fade.setToValue(1);
        TranslateTransition rise = new TranslateTransition(SLOW, node);
        rise.setToY(0);
        ParallelTransition both = new ParallelTransition(fade, rise);
        both.setInterpolator(EASE_OUT);
        both.setDelay(delay);
        both.play();
    }

    // [F29-B03] Enter a group one after another
    public static void stagger(List<? extends Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            enter(nodes.get(i), STAGGER.multiply(i));
        }
    }

    // [F29-B04] Raise a card slightly while the pointer is over it
    public static void hoverLift(Node node) {
        if (!ENABLED) {
            return;
        }
        TranslateTransition lift = new TranslateTransition(FAST, node);
        lift.setInterpolator(EASE_OUT);
        node.setOnMouseEntered(event -> {
            lift.stop();
            lift.setToY(-3);
            lift.playFromStart();
        });
        node.setOnMouseExited(event -> {
            lift.stop();
            lift.setToY(0);
            lift.playFromStart();
        });
    }

    // [F29-B04] Shrink a button a little while it is pressed
    public static void pressable(Node node) {
        if (!ENABLED) {
            return;
        }
        ScaleTransition scale = new ScaleTransition(FAST, node);
        scale.setInterpolator(EASE_OUT);
        node.pressedProperty().addListener((obs, was, pressed) -> {
            scale.stop();
            scale.setToX(pressed ? 0.97 : 1);
            scale.setToY(pressed ? 0.97 : 1);
            scale.playFromStart();
        });
    }

    // [F29-B05] Short horizontal shake for rejected input (e.g. wrong password)
    public static void shake(Node node) {
        if (!ENABLED) {
            return;
        }
        Timeline shake = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(60), new KeyValue(node.translateXProperty(), -8)),
                new KeyFrame(Duration.millis(120), new KeyValue(node.translateXProperty(), 7)),
                new KeyFrame(Duration.millis(180), new KeyValue(node.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(240), new KeyValue(node.translateXProperty(), 3)),
                new KeyFrame(SLOW, new KeyValue(node.translateXProperty(), 0, EASE_OUT)));
        shake.play();
    }

    // [F29-B06] Animate a number label from one value to another
    public static void countUp(Label label, long from, long to, LongFunction<String> format) {
        Timeline previous = (Timeline) label.getProperties().get("bd.countUp");
        if (previous != null) {
            previous.stop();
        }
        if (!ENABLED || from == to) {
            label.setText(format.apply(to));
            return;
        }
        SimpleLongProperty value = new SimpleLongProperty(from);
        value.addListener((obs, old, now) -> label.setText(format.apply(now.longValue())));
        label.setText(format.apply(from));
        Timeline timeline = new Timeline(new KeyFrame(COUNT, new KeyValue(value, to, EASE_OUT)));
        label.getProperties().put("bd.countUp", timeline);
        timeline.play();
    }

    // [F29-B07] Layered ocean waves that drift along the bottom of a region
    public static Pane waves() {
        Pane layer = new Pane();
        layer.setMinSize(0, 0);
        layer.setPrefSize(0, 0);
        layer.setMouseTransparent(true);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(layer.widthProperty());
        clip.heightProperty().bind(layer.heightProperty());
        layer.setClip(clip);

        // [F29-B07] Back to front: slower, taller and fainter at the back
        TranslateTransition[] drift = {
                wave(layer, "wave-back", 520, 150, 26, Duration.seconds(18), false),
                wave(layer, "wave-mid", 420, 110, 20, Duration.seconds(13), true),
                wave(layer, "wave-front", 340, 72, 14, Duration.seconds(9), false)
        };
        // [F29-B07] Only run while the layer is on screen
        layer.sceneProperty().addListener((obs, old, scene) -> {
            for (TranslateTransition t : drift) {
                if (scene != null && ENABLED) {
                    t.play();
                } else {
                    t.pause();
                }
            }
        });
        return layer;
    }

    // [F29-B07] One seamless sine-like band; shifting by one wavelength loops without a seam
    private static TranslateTransition wave(Pane layer, String styleClass, double wavelength,
                                            double height, double amplitude, Duration period, boolean reverse) {
        int count = 12; // [F29-B07] Enough wavelengths to cover very wide windows plus one spare
        double depth = 400; // [F29-B07] Fill below the crest so tall windows never show a gap
        Path path = new Path();
        path.getElements().add(new MoveTo(0, amplitude));
        for (int i = 0; i < count; i++) {
            double x = i * wavelength;
            double q = wavelength / 4;
            path.getElements().add(new CubicCurveTo(x + q, 0, x + q, 0, x + 2 * q, amplitude));
            path.getElements().add(new CubicCurveTo(x + 3 * q, 2 * amplitude, x + 3 * q, 2 * amplitude,
                    x + wavelength, amplitude));
        }
        path.getElements().addAll(new LineTo(count * wavelength, depth), new LineTo(0, depth), new ClosePath());
        path.getStyleClass().addAll("wave", styleClass);
        path.setManaged(false);
        path.layoutYProperty().bind(layer.heightProperty().subtract(height));
        layer.getChildren().add(path);

        TranslateTransition drift = new TranslateTransition(period, path);
        drift.setFromX(reverse ? -wavelength : 0);
        drift.setToX(reverse ? 0 : -wavelength);
        drift.setInterpolator(Interpolator.LINEAR);
        drift.setCycleCount(Animation.INDEFINITE);
        return drift;
    }
}
