// FILE 27 of 28: src/main/java/ph/bantaydagat/Main.java
// [F27-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F27-B01] Handle startup persistence failures
import java.nio.file.Path; // [F27-B01] data.ser and uploads/ live in the working directory
import javafx.application.Application; // [F27-B01] JavaFX application lifecycle
import javafx.application.Platform; // [F27-B01] Exit after a startup failure
import javafx.geometry.Insets; // [F27-B01] Navigation bar spacing
import javafx.geometry.Pos; // [F27-B01] Align the session header
import javafx.scene.Node; // [F27-B01] Add any view to a tab
import javafx.scene.Scene; // [F27-B01] Window content
import javafx.scene.control.Button; // [F27-B01] Sign out / return to login
import javafx.scene.control.Label; // [F27-B01] Current session text
import javafx.scene.control.Tab; // [F27-B01] Each session screen
import javafx.scene.control.TabPane; // [F27-B01] Session navigation
import javafx.scene.layout.BorderPane; // [F27-B01] App shell
import javafx.scene.layout.HBox; // [F27-B01] Session bar
import javafx.scene.layout.Priority; // [F27-B01] Expand the header spacer
import javafx.scene.layout.Region; // [F27-B01] Push the sign-out button to the right
import javafx.stage.Stage; // [F27-B01] Desktop window

public final class Main extends Application {
    // [F27-B02] Shared app state and root layout
    private final BorderPane root = new BorderPane();
    private IncidentManager manager;
    private AuthService auth;

    // [F27-B03] Startup, persistence load and first screen
    @Override
    public void start(Stage stage) {
        try {
            manager = new IncidentManager(Path.of("."));
            auth = new AuthService(manager);
        } catch (IOException e) {
            ViewSupport.error("Could not open BantayDagat's data: " + e.getMessage());
            Platform.exit();
            return;
        }
        showLogin();
        showWindow(stage);
    }

    // [F27-B04] Configure and display the window
    private void showWindow(Stage stage) {
        stage.setTitle("BantayDagat: Marine Waste Reporting & Incident Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(590);
        stage.setScene(new Scene(root, 1060, 690)); // [F27-B04] Attach the app layout to the window
        stage.show(); // [F27-B04] A Stage must be shown to display its Scene
    }

    // [F27-B05] Connect login and anonymous entry to session navigation
    private void showLogin() {
        root.setTop(null);
        root.setCenter(new LoginView(auth, this::showSession, () -> showSession(null))); // [F27-B05] Guest has no account
    }

    // [F27-B06] Use role permissions to select the right screens
    private void showSession(User user) {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        if (user == null) {
            tabs.getTabs().add(tab("New report", new ReportFormView(manager, null, () -> {})));
        } else if (user.getPermissions().contains(User.MANAGE)) {
            addAdminTabs(tabs);
        } else if (user.getPermissions().contains(User.REPORT)) {
            addCitizenTabs(tabs, user);
        }
        root.setTop(sessionBar(user));
        root.setCenter(tabs);
    }

    // [F27-B07] Admin tabs share the same manager and dashboard refresh
    private void addAdminTabs(TabPane tabs) {
        DashboardView dashboard = new DashboardView(manager);
        tabs.getTabs().addAll(tab("Incident queue", new AdminView(manager, dashboard::refresh)),
                tab("Dashboard", dashboard),
                tab("Hotlines", new HotlinesView(new HotlinesDirectory(), getHostServices()::showDocument)));
    }

    // [F27-B08] Registered users can submit and see their own history
    private void addCitizenTabs(TabPane tabs, User user) {
        MyReportsView history = new MyReportsView(manager, user.getId());
        tabs.getTabs().add(tab("New report", new ReportFormView(manager, user.getId(), history::refresh)));
        if (user.getPermissions().contains(User.MY_REPORTS)) {
            tabs.getTabs().add(tab("My Reports", history));
        }
    }

    // [F27-B09] Session title and sign-out action
    private HBox sessionBar(User user) {
        String who = user == null ? "Anonymous reporting" : "Signed in as " + user.getUsername();
        Button leave = new Button(user == null ? "Back to login" : "Sign out");
        leave.setOnAction(event -> showLogin());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, new Label("BantayDagat"), new Label(who), spacer, leave);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: #e3f3f6;");
        return bar;
    }

    // [F27-B10] Wrap a screen in a navigation tab
    private Tab tab(String title, Node view) {
        return new Tab(title, view);
    }

    // [F27-B11] Java entry point used by Maven
    public static void main(String[] args) {
        launch(args); // [F27-B11] Start the JavaFX runtime
    }
}

