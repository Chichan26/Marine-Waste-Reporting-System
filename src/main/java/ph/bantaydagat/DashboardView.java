// FILE 25 of 28: src/main/java/ph/bantaydagat/DashboardView.java
// [F25-B01] Package and dependencies
package ph.bantaydagat;

import java.util.List; // [F25-B01] Snapshot of all reports
import javafx.geometry.Insets; // [F25-B01] Dashboard spacing
import javafx.scene.chart.BarChart; // [F25-B01] Counts per category
import javafx.scene.chart.CategoryAxis; // [F25-B01] Waste categories along the bottom
import javafx.scene.chart.NumberAxis; // [F25-B01] Count axis
import javafx.scene.chart.PieChart; // [F25-B01] Active versus resolved
import javafx.scene.chart.XYChart; // [F25-B01] Bar data series
import javafx.scene.layout.HBox; // [F25-B01] Side-by-side charts
import javafx.scene.layout.Priority; // [F25-B01] Resize charts with the window
import javafx.scene.layout.VBox; // [F25-B01] Dashboard layout

public final class DashboardView extends VBox {
    // [F25-B02] Read-only manager and reusable charts
    private final IncidentManager manager;
    private final PieChart statusChart = new PieChart();
    private final BarChart<String, Number> categoryChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

    // [F25-B03] Build the dashboard with two resizable charts
    public DashboardView(IncidentManager manager) {
        this.manager = manager;
        setSpacing(12);
        setPadding(new Insets(20));
        configureCharts();
        HBox charts = new HBox(12, statusChart, categoryChart);
        HBox.setHgrow(statusChart, Priority.ALWAYS);
        HBox.setHgrow(categoryChart, Priority.ALWAYS);
        VBox.setVgrow(charts, Priority.ALWAYS);
        getChildren().addAll(ViewSupport.title("Incident dashboard"), charts);
        refresh();
    }

    // [F25-B04] Label charts and disable animation during updates
    private void configureCharts() {
        statusChart.setTitle("Active vs. resolved");
        statusChart.setAnimated(false);
        statusChart.setMinWidth(250);
        categoryChart.setTitle("Reports by waste category");
        categoryChart.getYAxis().setLabel("Reports");
        categoryChart.getXAxis().setTickLabelRotation(-32);
        categoryChart.setAnimated(false);
        categoryChart.setLegendVisible(false);
        categoryChart.setMinWidth(410);
    }

    // [F25-B05] Refresh both charts from the same snapshot
    public void refresh() {
        List<Report> reports = manager.getReports();
        updateStatusChart(reports);
        updateCategoryChart(reports); // [F25-B05] Apply the same report data to the bar chart
    }

    // [F25-B06] Count all unresolved statuses as active
    private void updateStatusChart(List<Report> reports) {
        long resolved = reports.stream().filter(report -> report.getStatus() == Status.RESOLVED).count();
        long active = reports.size() - resolved;
        statusChart.getData().setAll(new PieChart.Data("Active (" + active + ")", active),
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

