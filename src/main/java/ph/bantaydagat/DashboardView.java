// FILE 25 of 28: src/main/java/ph/bantaydagat/DashboardView.java
// [F25-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F25-B01] Snapshot of all reports
import javafx.scene.chart.BarChart; // [F25-B01] Counts per category
import javafx.scene.chart.CategoryAxis; // [F25-B01] Waste categories along the bottom
import javafx.scene.chart.NumberAxis; // [F25-B01] Count axis
import javafx.scene.chart.PieChart; // [F25-B01] Active versus resolved
import javafx.scene.chart.XYChart; // [F25-B01] Bar data series
import javafx.scene.control.Label; // [F25-B01] KPI values
import javafx.scene.control.ScrollPane; // [F25-B01] Small windows
import javafx.scene.layout.HBox; // [F25-B01] Side-by-side cards
import javafx.scene.layout.Priority; // [F25-B01] Resize with the window
import javafx.scene.layout.VBox; // [F25-B01] Dashboard layout

public final class DashboardView extends ScrollPane {
    // [F25-B02] Read-only manager, KPI labels and reusable charts
    private final IncidentManager manager;
    private final Label total = kpiValue();
    private final Label active = kpiValue();
    private final Label critical = kpiValue();
    private final Label resolvedRate = kpiValue();
    private final PieChart statusChart = new PieChart();
    private final BarChart<String, Number> categoryChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

    // [F25-B03] KPI row above two chart cards
    public DashboardView(IncidentManager manager) {
        this.manager = manager;
        configureCharts();
        HBox kpis = new HBox(16, kpiCard("TOTAL REPORTS", total, "kpi-accent-teal"),
                kpiCard("ACTIVE INCIDENTS", active, "kpi-accent-amber"),
                kpiCard("CRITICAL OPEN", critical, "kpi-accent-red"),
                kpiCard("RESOLUTION RATE", resolvedRate, "kpi-accent-green"));
        VBox pieCard = ViewSupport.card(statusChart);
        VBox barCard = ViewSupport.card(categoryChart);
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
    }

    // [F25-B04] Label charts and disable animation during updates
    private void configureCharts() {
        statusChart.setTitle("Active vs. resolved");
        statusChart.setAnimated(false);
        statusChart.setLabelsVisible(false);
        statusChart.setMinWidth(250);
        statusChart.setPrefHeight(340);
        categoryChart.setTitle("Reports by waste category");
        categoryChart.getYAxis().setLabel("Reports");
        categoryChart.getXAxis().setTickLabelRotation(-32);
        categoryChart.setAnimated(false);
        categoryChart.setLegendVisible(false);
        categoryChart.setMinWidth(380);
        categoryChart.setPrefHeight(340);
    }

    // [F25-B04] One summary tile
    private VBox kpiCard(String label, Label value, String accent) {
        VBox card = ViewSupport.card(ViewSupport.styled(new Label(label), "kpi-label"), value);
        card.setSpacing(6);
        card.getStyleClass().addAll("kpi-card", accent);
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
        total.setText(String.valueOf(reports.size()));
        active.setText(String.valueOf(reports.size() - resolved));
        critical.setText(String.valueOf(criticalOpen));
        resolvedRate.setText(reports.isEmpty() ? "—" : Math.round(100.0 * resolved / reports.size()) + "%");
    }

    // [F25-B06] Count all unresolved statuses as active
    private void updateStatusChart(List<Report> reports) {
        long resolved = reports.stream().filter(report -> report.getStatus() == Status.RESOLVED).count();
        long open = reports.size() - resolved;
        statusChart.getData().setAll(new PieChart.Data("Active (" + open + ")", open),
                new PieChart.Data("Resolved (" + resolved + ")", resolved));
    }

    // [F25-B07] Count every waste category, including categories with zero reports
    private void updateCategoryChart(List<Report> reports) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (WasteCategory category : WasteCategory.values()) {
            long count = reports.stream().filter(report -> report.getCategory() == category).count();
            series.getData().add(new XYChart.Data<>(category.toString(), count));
        }
        categoryChart.getData().clear();
        categoryChart.getData().add(series);
    }
}
