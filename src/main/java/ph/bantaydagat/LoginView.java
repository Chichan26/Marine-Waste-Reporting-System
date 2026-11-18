// FILE 21 of 28: src/main/java/ph/bantaydagat/LoginView.java
// [F21-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F21-B01] Handle failed registration saves
import java.util.function.Consumer; // [F21-B01] Send the account to Main
import javafx.geometry.Pos; // [F21-B01] Center the sign-in form
import javafx.scene.control.Button; // [F21-B01] Session actions
import javafx.scene.control.Label; // [F21-B01] Help and form labels
import javafx.scene.control.PasswordField; // [F21-B01] Mask typed passwords
import javafx.scene.control.TextField; // [F21-B01] Username input
import javafx.scene.layout.HBox; // [F21-B01] Login and register buttons
import javafx.scene.layout.VBox; // [F21-B01] Sign-in layout

public final class LoginView extends VBox {
    // [F21-B02] Form fields and registration feedback
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final Label feedback = new Label();

    // [F21-B03] Put the form and demo credentials on screen
    public LoginView(AuthService auth, Consumer<User> signedIn, Runnable anonymous) {
        setAlignment(Pos.CENTER);
        setSpacing(12);
        VBox fields = createFields();
        getChildren().addAll(ViewSupport.title("BantayDagat"),
                new Label("Marine Waste Reporting & Incident Management System"), fields,
                createActions(auth, signedIn, anonymous), feedback,
                new Label("Demo accounts: admin / change-admin-password        citizen / change-citizen-password"));
    }

    // [F21-B04] Set up the account fields
    private VBox createFields() {
        username.setPromptText("Username");
        password.setPromptText("Password");
        VBox fields = new VBox(6, new Label("Username"), username, new Label("Password"), password);
        fields.setMaxWidth(320);
        return fields;
    }

    // [F21-B05] Connect login, registration and guest mode
    private HBox createActions(AuthService auth, Consumer<User> signedIn, Runnable anonymous) {
        Button login = new Button("Log in");
        Button register = new Button("Register citizen");
        Button guest = new Button("Report anonymously");
        login.setDefaultButton(true);
        login.setOnAction(event -> logIn(auth, signedIn)); // [F21-B05] Trigger credential check
        register.setOnAction(event -> register(auth)); // [F21-B05] Save a new citizen
        guest.setOnAction(event -> anonymous.run()); // [F21-B05] Open the guest form
        HBox buttons = new HBox(8, login, register, guest);
        buttons.setAlignment(Pos.CENTER);
        return buttons;
    }

    // [F21-B06] Sign in or show an invalid credentials message
    private void logIn(AuthService auth, Consumer<User> signedIn) {
        String entered = password.getText();
        password.clear();
        auth.login(username.getText(), entered).ifPresentOrElse(signedIn,
                () -> ViewSupport.error("Invalid username or password."));
    }

    // [F21-B07] Register and prompt the new citizen to sign in
    private void register(AuthService auth) {
        try {
            CitizenUser user = auth.register(username.getText(), password.getText());
            username.setText(user.getUsername());
            password.clear();
            feedback.setText("Account created. Enter your password and log in.");
        } catch (IllegalArgumentException | IOException e) {
            ViewSupport.error(e.getMessage());
        }
    }
}

