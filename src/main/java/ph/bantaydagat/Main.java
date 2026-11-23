// FILE 27 of 28: src/main/java/ph/bantaydagat/Main.java
// [F27-B01] Package and dependencies
package ph.bantaydagat;

import java.io.IOException; // [F27-B01] Handle startup persistence failures
import java.nio.file.Path; // [F27-B01] data.ser and uploads/ live in the working directory
import java.util.ArrayList; // [F27-B01] Navigation entries for the session
import java.util.List; // [F27-B01] Navigation entries for the session
import javafx.application.Application; // [F27-B01] JavaFX application lifecycle
import javafx.application.Platform; // [F27-B01] Exit after a startup failure
import javafx.beans.property.BooleanProperty; // [F27-B01] Light / dark theme state
import javafx.beans.property.SimpleBooleanProperty; // [F27-B01] Observable theme flag
import javafx.geometry.Insets; // [F27-B01] Navigation bar spacing
import javafx.geometry.Pos; // [F27-B01] Align the session header
import javafx.scene.Node; // [F27-B01] Any view can be shown in the content area
import javafx.scene.Scene; // [F27-B01] Window content
import javafx.scene.control.Button; // [F27-B01] Sign out / return to login
import javafx.scene.control.Label; // [F27-B01] Brand and session text
import javafx.scene.control.ToggleButton; // [F27-B01] Sidebar navigation entries
import javafx.scene.control.ToggleGroup; // [F27-B01] Only one active screen
import javafx.scene.layout.BorderPane; // [F27-B01] App shell
import javafx.scene.layout.HBox; // [F27-B01] Top bar and brand lockup
import javafx.scene.layout.Priority; // [F27-B01] Expand spacers
import javafx.scene.layout.Region; // [F27-B01] Spacers
import javafx.scene.layout.StackPane; // [F27-B01] Brand icon container
import javafx.scene.layout.VBox; // [F27-B01] Sidebar
import javafx.stage.Stage; // [F27-B01] Desktop window

public final class Main extends Application {
    // [F27-B02] Shared app state and root layout
    private final BorderPane root = new BorderPane();
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);
    private Label topBarTitle;
    private IncidentManager manager;
    private AuthService auth;

    // [F27-B02] A sidebar entry, its icon and the screen it opens
    private record NavItem(String label, String icon, Node view) {}

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
        darkMode.addListener((obs, old, dark) -> ViewSupport.applyTheme(root, dark));
        showLogin();
        showWindow(stage);
    }

    // [F27-B04] Configure and display the window with the ocean theme
    private void showWindow(Stage stage) {
        stage.setTitle("BantayDagat: Marine Waste Reporting & Incident Management System");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        Scene scene = new Scene(root, 1220, 780);
        scene.getStylesheets().add(Main.class.getResource("styles.css").toExternalForm()); // [F27-B04] Theme
        stage.setScene(scene); // [F27-B04] Attach the app layout to the window
        stage.show(); // [F27-B04] A Stage must be shown to display its Scene
    }

    // [F27-B05] Connect login and anonymous entry to session navigation
    private void showLogin() {
        root.setTop(null);
        root.setLeft(null);
        root.setCenter(new LoginView(auth, darkMode, this::showSession, () -> showSession(null))); // [F27-B05]
    }

    // [F27-B06] Use role permissions to select the right screens
    private void showSession(User user) {
        List<NavItem> items = new ArrayList<>();
        String role;
        if (user == null) {
            role = "Guest";
            items.add(new NavItem("New report", Icons.REPORT_ADD, new ReportFormView(manager, null, () -> {})));
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
        items.add(new NavItem("Dashboard", Icons.DASHBOARD, dashboard));
        items.add(new NavItem("Incident queue", Icons.QUEUE, new AdminView(manager, dashboard::refresh)));
        items.add(hotlines());
    }

    // [F27-B08] Registered users can submit and see their own history
    private void addCitizenItems(List<NavItem> items, User user) {
        MyReportsView history = new MyReportsView(manager, user.getId());
        if (user.getPermissions().contains(User.REPORT)) {
            items.add(new NavItem("New report", Icons.REPORT_ADD,
                    new ReportFormView(manager, user.getId(), history::refresh)));
        }
        if (user.getPermissions().contains(User.MY_REPORTS)) {
            items.add(new NavItem("My reports", Icons.HISTORY, history));
        }
        items.add(hotlines());
    }

    // [F27-B08] Every role can reach the emergency directory
    private NavItem hotlines() {
        return new NavItem("Hotlines", Icons.PHONE,
                new HotlinesView(new HotlinesDirectory(), getHostServices()::showDocument));
    }

    // [F27-B09] Header with the active screen title, theme switch, session identity and sign-out
    private HBox topBar(User user, String role) {
        topBarTitle = ViewSupport.styled(new Label("BantayDagat"), "top-bar-title");
        Label subtitle = ViewSupport.styled(new Label("Marine Waste Reporting & Incident Management"),
                "top-bar-subtitle");
        VBox titleBox = new VBox(1, topBarTitle, subtitle);

        String who = user == null ? "Anonymous reporting" : user.getUsername() + "  ·  " + role;
        Label pill = ViewSupport.styled(new Label(who), "user-pill");
        Button theme = ViewSupport.themeToggle(darkMode);
        Button leave = ViewSupport.styled(new Button(user == null ? "Back to login" : "Sign out"), "btn-sm");
        leave.setGraphic(Icons.of(Icons.LOGOUT, 14, "icon-primary"));
        leave.setOnAction(event -> showLogin());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, titleBox, spacer, theme, pill, leave);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 24, 12, 24));
        bar.getStyleClass().add("top-bar");
        return bar;
    }

    // [F27-B10] Sidebar that swaps the centre screen
    private VBox sidebar(List<NavItem> items, String role) {
        StackPane mark = ViewSupport.styled(new StackPane(Icons.of(Icons.WAVE_EMBLEM, 22, "icon-brand")),
                "brand-mark");
        Label brand = ViewSupport.styled(new Label("BantayDagat"), "brand");
        Label tagline = ViewSupport.styled(new Label("Guarding our seas"), "brand-tagline");
        HBox brandLockup = new HBox(10, mark, new VBox(1, brand, tagline));
        brandLockup.setAlignment(Pos.CENTER_LEFT);

        VBox nav = new VBox(4, brandLockup);
        VBox.setMargin(brandLockup, new Insets(0, 0, 24, 2));
        nav.getChildren().add(ViewSupport.styled(new Label(role.toUpperCase()), "nav-section"));
        ToggleGroup group = new ToggleGroup();
        for (NavItem item : items) {
            ToggleButton button = ViewSupport.styled(new ToggleButton(item.label()), "nav-button");
            button.setGraphic(Icons.of(item.icon(), 16, "icon-nav"));
            button.setMaxWidth(Double.MAX_VALUE);
            button.setToggleGroup(group);
            button.setOnAction(event -> showScreen(item.label(), item.view()));
            nav.getChildren().add(button);
        }
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true); // [F27-B10] Keep one screen selected
            }
        });
        if (!group.getToggles().isEmpty()) {
            group.getToggles().get(0).setSelected(true);
            showScreen(items.get(0).label(), items.get(0).view());
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox footer = ViewSupport.styled(
                new VBox(2, ViewSupport.styled(new Label("Ocean Design System · v1.0"), "sidebar-footer-text")),
                "sidebar-footer");
        nav.getChildren().addAll(spacer, footer);

        nav.setPadding(new Insets(22, 14, 18, 14));
        nav.setPrefWidth(232);
        nav.getStyleClass().add("sidebar");
        return nav;
    }

    // [F27-B10] Swap the centre screen with a short fade-and-rise (skipped if already shown)
    private void showScreen(String title, Node view) {
        if (topBarTitle != null) {
            topBarTitle.setText(title);
        }
        if (root.getCenter() == view) {
            return;
        }
        root.setCenter(view);
        Motion.enter(view);
    }

    // [F27-B11] Java entry point used by Maven
    public static void main(String[] args) {
        launch(args); // [F27-B11] Start the JavaFX runtime
    }
}
