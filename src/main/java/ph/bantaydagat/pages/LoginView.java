// FILE 21 of 28: src/main/java/ph/bantaydagat/pages/LoginView.java
// [F21-B01] Package and dependencies
package ph.bantaydagat.pages;

import java.io.IOException; // [F21-B01] Handle failed registration saves
import java.util.List; // [F21-B01] Hero lines to stagger
import java.util.function.Consumer; // [F21-B01] Send the account to Main
import javafx.beans.property.BooleanProperty; // [F21-B01] Shared dark mode toggle
import javafx.geometry.Insets; // [F21-B01] Panel padding
import javafx.geometry.Pos; // [F21-B01] Center the sign-in card
import javafx.scene.control.Button; // [F21-B01] Session actions
import javafx.scene.control.Label; // [F21-B01] Help and form labels
import javafx.scene.control.PasswordField; // [F21-B01] Mask typed passwords
import javafx.scene.control.Separator; // [F21-B01] Divide sign-in from guest mode
import javafx.scene.control.TextField; // [F21-B01] Username input
import javafx.scene.layout.HBox; // [F21-B01] Hero and form side by side
import javafx.scene.layout.Priority; // [F21-B01] Share the window width
import javafx.scene.layout.StackPane; // [F21-B01] Center the card
import javafx.scene.layout.VBox; // [F21-B01] Sign-in layout
import ph.bantaydagat.AuthService;
import ph.bantaydagat.CitizenUser;
import ph.bantaydagat.User;
import ph.bantaydagat.shared.Icons;
import ph.bantaydagat.shared.Motion;
import ph.bantaydagat.shared.ViewSupport;

public final class LoginView extends HBox {
    // [F21-B02] Form fields and registration feedback
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final VBox feedback = new VBox();
    private VBox card; // [F21-B02] Kept so a failed sign-in can shake it

    // [F21-B03] Ocean hero on the left, sign-in card on the right
    public LoginView(AuthService auth, BooleanProperty darkMode, Consumer<User> signedIn, Runnable anonymous) {
        StackPane hero = createHero();
        card = createCard(auth, signedIn, anonymous);
        Motion.enter(card, Motion.STAGGER.multiply(2)); // [F21-B03] Card rises in after the hero copy

        Button theme = ViewSupport.themeToggle(darkMode);
        StackPane.setAlignment(theme, Pos.TOP_RIGHT);
        StackPane.setMargin(theme, new Insets(18, 22, 0, 0));

        StackPane formSide = new StackPane(card, theme);
        formSide.setPadding(new Insets(32));
        formSide.getStyleClass().add("page");
        HBox.setHgrow(hero, Priority.ALWAYS);
        HBox.setHgrow(formSide, Priority.ALWAYS);
        hero.setPrefWidth(520);
        formSide.setPrefWidth(520);
        getChildren().addAll(hero, formSide);
    }

    // [F21-B04] Branding and what the system does
    private StackPane createHero() {
        StackPane mark = ViewSupport.styled(new StackPane(Icons.of(Icons.WAVE_EMBLEM, 28, "icon-brand")),
                "brand-mark");
        Label badge = ViewSupport.styled(new Label("MARINE WASTE REPORTING SYSTEM"), "hero-badge");
        HBox topRow = new HBox(12, mark, badge);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = ViewSupport.styled(new Label("BantayDagat"), "hero-title");
        Label text = ViewSupport.styled(new Label(
                "Report marine waste, coordinate coastal cleanups and keep our seas safe."), "hero-text");
        text.setWrapText(true);
        text.setMaxWidth(420);

        VBox points = new VBox(12,
                heroPoint("Report plastic, oil spills, fishing gear and more"),
                heroPoint("Attach photos or videos as verifiable evidence"),
                heroPoint("Track every incident from dispatch to resolution"));

        VBox copy = new VBox(18, topRow, title, text, points);
        copy.setAlignment(Pos.CENTER_LEFT);
        copy.setPadding(new Insets(52));
        Motion.stagger(List.of(topRow, title, text, points)); // [F21-B04] Staggered hero entrance
        StackPane hero = new StackPane(Motion.waves(), copy); // [F21-B04] Drifting waves behind the copy
        hero.getStyleClass().add("login-hero");
        return hero;
    }

    private HBox heroPoint(String text) {
        StackPane check = ViewSupport.styled(new StackPane(Icons.of(Icons.CHECK, 12, "icon-brand")), "hero-check");
        Label label = ViewSupport.styled(new Label(text), "hero-point");
        label.setWrapText(true);
        HBox row = new HBox(12, check, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // [F21-B04] Sign-in card with fields, actions and quick-fill demo hint
    private VBox createCard(AuthService auth, Consumer<User> signedIn, Runnable anonymous) {
        username.setPromptText("Enter your username");
        password.setPromptText("Enter your password");
        VBox card = ViewSupport.card(
                ViewSupport.header("Welcome back", "Sign in to submit and track your reports."),
                ViewSupport.field("Username", username),
                ViewSupport.field("Password", password),
                createActions(auth, signedIn, anonymous),
                feedback,
                createDemoHint());
        card.getStyleClass().add("card-elevated");
        card.setSpacing(16);
        card.setPadding(new Insets(32));
        card.setMaxWidth(420);
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        return card;
    }

    // [F21-B04] One-click credential chips for demo accounts
    private VBox createDemoHint() {
        Label title = ViewSupport.styled(new Label("QUICK DEMO FILL"), "demo-hint-title");
        Button fillAdmin = demoChip("Admin", "admin", "change-admin-password");
        Button fillCitizen = demoChip("Citizen", "citizen", "change-citizen-password");
        HBox chips = new HBox(8, fillAdmin, fillCitizen);
        Label note = ViewSupport.styled(
                new Label("admin / change-admin-password  ·  citizen / change-citizen-password"), "muted");
        note.setWrapText(true);
        VBox box = ViewSupport.styled(new VBox(8, title, chips, note), "demo-hint");
        return box;
    }

    private Button demoChip(String label, String user, String pass) {
        Button chip = ViewSupport.styled(new Button(label), "demo-chip");
        chip.setOnAction(event -> {
            username.setText(user);
            password.setText(pass);
            feedback.getChildren().clear();
        });
        return chip;
    }

    // [F21-B05] Connect login, registration and guest mode
    private VBox createActions(AuthService auth, Consumer<User> signedIn, Runnable anonymous) {
        Button login = ViewSupport.primaryButton("Log in");
        Button register = new Button("Create citizen account");
        Button guest = ViewSupport.ghostButton("Report anonymously  →");
        login.setDefaultButton(true);
        login.setMaxWidth(Double.MAX_VALUE);
        register.setMaxWidth(Double.MAX_VALUE);
        guest.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> logIn(auth, signedIn)); // [F21-B05] Trigger credential check
        register.setOnAction(event -> register(auth)); // [F21-B05] Save a new citizen
        guest.setOnAction(event -> anonymous.run()); // [F21-B05] Open the guest form
        return new VBox(10, login, register, new Separator(), guest);
    }

    // [F21-B06] Sign in or show an invalid credentials message
    private void logIn(AuthService auth, Consumer<User> signedIn) {
        String entered = password.getText();
        password.clear();
        auth.login(username.getText(), entered).ifPresentOrElse(signedIn, () -> {
            feedback.getChildren().setAll(ViewSupport.banner("danger", "Invalid username or password."));
            Motion.shake(card); // [F21-B06] Signal the rejected attempt
        });
    }

    // [F21-B07] Register and prompt the new citizen to sign in
    private void register(AuthService auth) {
        try {
            CitizenUser user = auth.register(username.getText(), password.getText());
            username.setText(user.getUsername());
            password.clear();
            feedback.getChildren().setAll(ViewSupport.banner("success",
                    "Account created. Enter your password and log in."));
        } catch (IllegalArgumentException | IOException e) {
            feedback.getChildren().setAll(ViewSupport.banner("danger", e.getMessage()));
        }
    }
}
