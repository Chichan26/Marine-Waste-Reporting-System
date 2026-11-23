// FILE 25 of 28: src/main/java/ph/bantaydagat/DashboardView.java
// [F25-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F25-B01] Snapshot of all reports
import javafx.geometry.Pos; // [F25-B01] Align KPI dot and label
import javafx.scene.Node; // [F25-B01] Attach tooltips to chart nodes
import javafx.scene.chart.BarChart; // [F25-B01] Counts per category
import javafx.scene.chart.CategoryAxis; // [F25-B01] Waste categories along the bottom
import javafx.scene.chart.NumberAxis; // [F25-B01] Count axis
import javafx.scene.chart.PieChart; // [F25-B01] Active versus resolved
import javafx.scene.chart.XYChart; // [F25-B01] Bar data series
import javafx.scene.control.Label; // [F25-B01] KPI values
import javafx.scene.control.ScrollPane; // [F25-B01] Small windows
import javafx.scene.control.Tooltip; // [F25-B01] Chart hover tooltips
import javafx.scene.layout.HBox; // [F25-B01] Side-by-side cards
import javafx.scene.layout.Priority; // [F25-B01] Resize with the window
import javafx.scene.layout.Region; // [F25-B01] Colored KPI dot
import javafx.scene.layout.VBox; // [F25-B01] Dashboard layout
import javafx.util.Duration; // [F25-B01] Snappy tooltip delay

public final class DashboardView extends ScrollPane {
    // [F25-B02] Read-only manager, KPI labels and reusable charts
    private final IncidentManager manager;
    private final Label total = kpiValue();
    private final Label active = kpiValue();
    private final Label critical = kpiValue();
    private final Label resolvedRate = kpiValue();
    private final long[] kpiShown = new long[4]; // [F25-B02] Values currently on screen (count-up start)
    private final long[] kpiTarget = new long[4]; // [F25-B02] Latest values (count-up end)
    private boolean noReports; // [F25-B02] Resolution rate shows a dash when there is no data
    private final PieChart statusChart = new PieChart();
    private final BarChart<String, Number> categoryChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

    // [F25-B03] KPI row above two chart cards
    public DashboardView(IncidentManager manager) {
        this.manager = manager;
        configureCharts();
        HBox kpis = new HBox(16,
                kpiCard("TOTAL REPORTS", "All incidents logged", total, "ocean"),
                kpiCard("ACTIVE INCIDENTS", "Awaiting resolution", active, "amber"),
                kpiCard("CRITICAL OPEN", "Requires urgent dispatch", critical, "red"),
                kpiCard("RESOLUTION RATE", "Share of reports closed", resolvedRate, "green"));
        VBox pieCard = chartCard("Active vs. resolved", "Current workload split across all incidents.", statusChart);
        VBox barCard = chartCard("Reports by waste category", "Distribution of reported marine waste types.",
                categoryChart);
        HBox charts = new HBox(16, pieCard, barCard);
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        HBox.setHgrow(barCard, Priority.ALWAYS);
        pieCard.setPrefWidth(380);
        barCard.setPrefWidth(560);
        setContent(ViewSupport.page(ViewSupport.header("Incident dashboard",
                "A live overview of marine waste reports across all areas."), kpis, charts));
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");
        refresh();
        // [F25-B03] Replay the KPI count-up every time the dashboard is opened
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                animateKpis(true);
            }
        });
    }

    // [F25-B04] Configure charts and disable animation during updates
    private void configureCharts() {
        statusChart.setAnimated(false);
        statusChart.setLabelsVisible(false);
        statusChart.setMinWidth(250);
        statusChart.setPrefHeight(310);
        categoryChart.getYAxis().setLabel("Reports");
        categoryChart.getXAxis().setTickLabelRotation(-32);
        categoryChart.setAnimated(false);
        categoryChart.setLegendVisible(false);
        categoryChart.setMinWidth(380);
        categoryChart.setPrefHeight(310);
    }

    // [F25-B04] Card wrapper with consistent typography for each chart
    private VBox chartCard(String title, String subtitle, Node chart) {
        Label sub = ViewSupport.styled(new Label(subtitle), "kpi-caption");
        VBox header = new VBox(2, ViewSupport.sectionTitle(title), sub);
        VBox card = ViewSupport.card(header, chart);
        card.setSpacing(10);
        return card;
    }

    // [F25-B04] One summary tile with status dot and contextual caption
    private VBox kpiCard(String label, String caption, Label value, String accent) {
        Region dot = ViewSupport.styled(new Region(), "kpi-dot");
        dot.getStyleClass().add("kpi-dot-" + accent);
        HBox topRow = new HBox(7, dot, ViewSupport.styled(new Label(label), "kpi-label"));
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label note = ViewSupport.styled(new Label(caption), "kpi-caption");
        VBox card = ViewSupport.card(topRow, value, note);
        card.setSpacing(5);
        card.getStyleClass().addAll("kpi-card", "card-interactive", "kpi-accent-" + accent);
        Motion.hoverLift(card); // [F25-B04] Gentle lift on hover
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // [F25-B04] Large number label
    private static Label kpiValue() {
        return ViewSupport.styled(new Label("0"), "kpi-value");
    }

    // [F25-B05] Refresh KPIs and charts from the same snapshot
    public void refresh() {
        List<Report> reports = manager.getReports();
        updateKpis(reports);
        updateStatusChart(reports);
        updateCategoryChart(reports); // [F25-B05] Apply the same report data to the bar chart
    }

    // [F25-B05] Headline numbers
    private void updateKpis(List<Report> reports) {
        long resolved = reports.stream().filter(report -> report.getStatus() == Status.RESOLVED).count();
        long criticalOpen = reports.stream().filter(report -> report.getStatus() != Status.RESOLVED
                && report.getSeverityLevel() == Severity.CRITICAL).count();
        kpiTarget[0] = reports.size();
        kpiTarget[1] = reports.size() - resolved;
        kpiTarget[2] = criticalOpen;
        kpiTarget[3] = reports.isEmpty() ? 0 : Math.round(100.0 * resolved / reports.size());
        noReports = reports.isEmpty();
        animateKpis(false);
    }

    // [F25-B05] Count each KPI up to its target, from zero or from the value already shown
    private void animateKpis(boolean fromZero) {
        Label[] labels = {total, active, critical, resolvedRate};
        for (int i = 0; i < labels.length; i++) {
            long from = fromZero ? 0 : kpiShown[i];
            kpiShown[i] = kpiTarget[i];
            if (i == 3 && noReports) {
                Motion.countUp(labels[i], 0, 0, value -> "—");
            } else {
                Motion.countUp(labels[i], from, kpiTarget[i], i == 3 ? value -> value + "%" : String::valueOf);
            }
        }
    }

    // [F25-B06] Count all unresolved statuses as active
    private void updateStatusChart(List<Report> reports) {
        long resolved = reports.stream().filter(report -> report.getStatus() == Status.RESOLVED).count();
        long open = reports.size() - resolved;
        int totalCount = reports.size();
        PieChart.Data activeSlice = new PieChart.Data("Active (" + open + ")", open);
        PieChart.Data resolvedSlice = new PieChart.Data("Resolved (" + resolved + ")", resolved);
        statusChart.getData().setAll(activeSlice, resolvedSlice);
        installTooltip(activeSlice.getNode(), formatSlice("Active", open, totalCount));
        installTooltip(resolvedSlice.getNode(), formatSlice("Resolved", resolved, totalCount));
    }

    private static String formatSlice(String label, long count, int total) {
        long pct = total == 0 ? 0 : Math.round(100.0 * count / total);
        return label + ": " + count + " (" + pct + "%)";
    }

    // [F25-B07] Count every waste category, including categories with zero reports
    private void updateCategoryChart(List<Report> reports) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (WasteCategory category : WasteCategory.values()) {
            long count = reports.stream().filter(report -> report.getCategory() == category).count();
            XYChart.Data<String, Number> point = new XYChart.Data<>(category.toString(), count);
            point.nodeProperty().addListener((obs, old, node) ->
                    installTooltip(node, category + ": " + count + (count == 1 ? " report" : " reports")));
            series.getData().add(point);
        }
        categoryChart.getData().clear();
        categoryChart.getData().add(series);
        for (XYChart.Data<String, Number> point : series.getData()) {
            long count = point.getYValue().longValue();
            installTooltip(point.getNode(), point.getXValue() + ": " + count + (count == 1 ? " report" : " reports"));
        }
    }

    private static void installTooltip(Node node, String text) {
        if (node == null) {
            return;
        }
        Tooltip tip = new Tooltip(text);
        tip.setShowDelay(Duration.millis(120));
        Tooltip.install(node, tip);
    }
}
