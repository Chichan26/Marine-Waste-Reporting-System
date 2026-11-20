// FILE 27 of 28: src/main/java/ph/bantaydagat/Main.java
// [F27-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F27-B01] Handle startup persistence failures
import java.nio.file.Path; // [F27-B01] data.ser and uploads/ live in the working directory
import java.util.ArrayList; // [F27-B01] Navigation entries for the session
import java.util.List; // [F27-B01] Navigation entries for the session
import javafx.application.Application; // [F27-B01] JavaFX application lifecycle
import javafx.application.Platform; // [F27-B01] Exit after a startup failure
import javafx.geometry.Insets; // [F27-B01] Navigation bar spacing
import javafx.geometry.Pos; // [F27-B01] Align the session header
import javafx.scene.Node; // [F27-B01] Any view can be shown in the content area
import javafx.scene.Scene; // [F27-B01] Window content
import javafx.scene.control.Button; // [F27-B01] Sign out / return to login
import javafx.scene.control.Label; // [F27-B01] Brand and session text
import javafx.scene.control.ToggleButton; // [F27-B01] Sidebar navigation entries
import javafx.scene.control.ToggleGroup; // [F27-B01] Only one active screen
import javafx.scene.layout.BorderPane; // [F27-B01] App shell
import javafx.scene.layout.HBox; // [F27-B01] Top bar
import javafx.scene.layout.Priority; // [F27-B01] Expand spacers
import javafx.scene.layout.Region; // [F27-B01] Spacers
import javafx.scene.layout.VBox; // [F27-B01] Sidebar
import javafx.stage.Stage; // [F27-B01] Desktop window

public final class Main extends Application {
    // [F27-B02] Shared app state and root layout
    private final BorderPane root = new BorderPane();
    private IncidentManager manager;
    private AuthService auth;

    // [F27-B02] A sidebar entry and the screen it opens
    private record NavItem(String label, Node view) {}

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

    // [F27-B04] Configure and display the window with the ocean theme
    private void showWindow(Stage stage) {
        stage.setTitle("BantayDagat: Marine Waste Reporting & Incident Management System");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(Main.class.getResource("styles.css").toExternalForm()); // [F27-B04] Theme
        stage.setScene(scene); // [F27-B04] Attach the app layout to the window
        stage.show(); // [F27-B04] A Stage must be shown to display its Scene
    }

    // [F27-B05] Connect login and anonymous entry to session navigation
    private void showLogin() {
        root.setTop(null);
        root.setLeft(null);
        root.setCenter(new LoginView(auth, this::showSession, () -> showSession(null))); // [F27-B05] Guest has no account
    }

    // [F27-B06] Use role permissions to select the right screens
    private void showSession(User user) {
        List<NavItem> items = new ArrayList<>();
        String role;
        if (user == null) {
            role = "Guest";
            items.add(new NavItem("New report", new ReportFormView(manager, null, () -> {})));
            items.add(hotlines());
        } else if (user.getPermissions().contains(User.MANAGE)) {
            role = "Administrator";
            addAdminItems(items);
        } else {
            role = "Citizen";
            addCitizenItems(items, user);
        }
        root.setTop(topBar(user, role));
        root.setLeft(sidebar(items, role));
    }

    // [F27-B07] Admin screens share the same manager and dashboard refresh
    private void addAdminItems(List<NavItem> items) {
        DashboardView dashboard = new DashboardView(manager);
        items.add(new NavItem("Dashboard", dashboard));
        items.add(new NavItem("Incident queue", new AdminView(manager, dashboard::refresh)));
        items.add(hotlines());
    }

    // [F27-B08] Registered users can submit and see their own history
    private void addCitizenItems(List<NavItem> items, User user) {
        MyReportsView history = new MyReportsView(manager, user.getId());
        if (user.getPermissions().contains(User.REPORT)) {
            items.add(new NavItem("New report", new ReportFormView(manager, user.getId(), history::refresh)));
        }
        if (user.getPermissions().contains(User.MY_REPORTS)) {
            items.add(new NavItem("My reports", history));
        }
        items.add(hotlines());
    }

    // [F27-B08] Every role can reach the emergency directory
    private NavItem hotlines() {
        return new NavItem("Hotlines", new HotlinesView(new HotlinesDirectory(), getHostServices()::showDocument));
    }

    // [F27-B09] Header with the session identity and sign-out action
    private HBox topBar(User user, String role) {
        Label title = ViewSupport.styled(new Label("Marine Waste Reporting & Incident Management"), "top-bar-title");
        String who = user == null ? "Anonymous reporting" : user.getUsername() + "  ·  " + role;
        Label pill = ViewSupport.styled(new Label(who), "user-pill");
        Button leave = new Button(user == null ? "Back to login" : "Sign out");
        leave.setOnAction(event -> showLogin());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(12, title, spacer, pill, leave);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 24, 12, 24));
        bar.getStyleClass().add("top-bar");
        return bar;
    }

    // [F27-B10] Sidebar that swaps the centre screen
    private VBox sidebar(List<NavItem> items, String role) {
        Label brand = ViewSupport.styled(new Label("BantayDagat"), "brand");
        Label tagline = ViewSupport.styled(new Label("Guarding our seas together"), "brand-tagline");
        VBox nav = new VBox(4, new VBox(2, brand, tagline));
        VBox.setMargin(nav.getChildren().get(0), new Insets(0, 0, 24, 4));
        nav.getChildren().add(ViewSupport.styled(new Label(role.toUpperCase()), "nav-section"));
        ToggleGroup group = new ToggleGroup();
        for (NavItem item : items) {
            ToggleButton button = ViewSupport.styled(new ToggleButton(item.label()), "nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setToggleGroup(group);
            button.setOnAction(event -> root.setCenter(item.view()));
            nav.getChildren().add(button);
        }
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true); // [F27-B10] Keep one screen selected
            }
        });
        if (!group.getToggles().isEmpty()) {
            group.getToggles().get(0).setSelected(true);
            root.setCenter(items.get(0).view());
        }
        nav.setPadding(new Insets(24, 14, 24, 14));
        nav.setPrefWidth(220);
        nav.getStyleClass().add("sidebar");
        return nav;
    }

    // [F27-B11] Java entry point used by Maven
    public static void main(String[] args) {
        launch(args); // [F27-B11] Start the JavaFX runtime
    }
}
