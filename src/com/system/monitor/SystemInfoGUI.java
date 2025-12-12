
package com.system.monitor;

import java.awt.*;
import java.util.concurrent.*;

import javax.swing.*;
import com.systeminfo.SystemInfoFetcher;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.time.*;

public class SystemInfoGUI {

	private TimeSeries cpuSeries = new TimeSeries("CPU");
    private TimeSeries memSeries = new TimeSeries("Memory");
    private TimeSeries diskSeries = new TimeSeries("Disk");
    private TimeSeries gpuSeries = new TimeSeries("GPU");
    private TimeSeries wifiSeries = new TimeSeries("Wi-Fi");
    private ScheduledExecutorService scheduler;
    private int gcCounter = 0;

    
    public SystemInfoGUI() {
        cpuSeries.setMaximumItemAge(10000);
        memSeries.setMaximumItemAge(10000);
        diskSeries.setMaximumItemAge(10000);
        gpuSeries.setMaximumItemAge(10000);
        wifiSeries.setMaximumItemAge(10000);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SystemInfoGUI().createDashboard());
    }

    private void createDashboard() {
        JFrame frame = new JFrame("Sky Watch - System Performance Monitor");
//        frame.getContentPane().setBackground(new Color(18, 18, 18));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 800);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel("Sky Watch - Real-Time Performance Monitor", SwingConstants.CENTER);
        title.setFont(new Font("Consolas", Font.BOLD, 20));
//        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        frame.add(title, BorderLayout.NORTH);
        
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel cpuPanel = createMetricPanel("CPU Usage", cpuSeries);
        JPanel memPanel = createMetricPanel("Memory Usage", memSeries);
        JPanel diskPanel = createMetricPanel("Disk Usage", diskSeries);
        JPanel gpuPanel = createMetricPanel("GPU Usage", gpuSeries);
        JPanel networkPanel = createMetricPanel("WIFI Usage", wifiSeries);
        
        tabs.add("CPU", cpuPanel);
        tabs.add("Memory", memPanel);
        tabs.add("Disk", diskPanel);
        tabs.add("GPU", gpuPanel);
        tabs.add("WI-FI", networkPanel);
        
        frame.add(tabs, BorderLayout.CENTER);


        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 1️⃣ Fetch system stats (heavy work happens off the Swing thread)
                int cpuVal = SystemInfoFetcher.getCPUUsage();
                int memVal = SystemInfoFetcher.getMemoryUsage();
                int diskVal = SystemInfoFetcher.getDiskUsage();
                int gpuVal = SystemInfoFetcher.getGPUUsage();
                int wifiVal = SystemInfoFetcher.getWifiUsage();

                String cpuInfo = SystemInfoFetcher.getCPUInfo();
                String memInfo = SystemInfoFetcher.getMemoryInfo();
                String diskInfo = SystemInfoFetcher.getDiskInfo();
                String gpuInfo = SystemInfoFetcher.getGPUInfo();
                String wifiInfo = SystemInfoFetcher.getWifiInfo();

                // 2️⃣ Update GUI safely
                SwingUtilities.invokeLater(() -> {
                    updateMetric(cpuPanel, cpuVal, cpuInfo, cpuSeries);
                    updateMetric(memPanel, memVal, memInfo, memSeries);
                    updateMetric(diskPanel, diskVal, diskInfo, diskSeries);
                    updateMetric(gpuPanel, gpuVal, gpuInfo, gpuSeries);
                    updateMetric(networkPanel, wifiVal, wifiInfo, wifiSeries);
                });

                // 3️⃣ Suggest GC only occasionally (e.g., every 1 min)
                gcCounter++;
                if (gcCounter >= 30) { // 30 × 2 s = 60 s
                    System.gc();
                    gcCounter = 0;
                }

            } catch (Exception ex) {
                ex.printStackTrace(); // Keeps scheduler alive even if one fetch fails
            }

        }, 0, 2, TimeUnit.SECONDS);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdownNow();
                }
            }
        });


        frame.setVisible(true);
    }


    private JPanel createMetricPanel(String title, TimeSeries series) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
//        panel.setBackground(new Color(18, 18, 18));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
//        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(null, "Time", "% Usage", dataset, false, false, false);
        XYPlot plot = chart.getXYPlot();
        XYAreaRenderer renderer = new XYAreaRenderer();
        Color areaColor = new Color(0, 120, 255, 100); // Blue with transparency
        renderer.setSeriesPaint(0, areaColor);
        plot.setRenderer(renderer);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 100));
//        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        chartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 180, 0));
        progressBar.setBackground(new Color(40, 40, 40));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25)); // fixed height, fills width

        JLabel percent = new JLabel("0%");
        percent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        percent.setForeground(Color.BLACK);
        percent.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("Fetching info...");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(Color.DARK_GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(chartPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(percent);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(infoLabel);

        // Store references
        panel.putClientProperty("progressBar", progressBar);
        panel.putClientProperty("percentLabel", percent);
        panel.putClientProperty("infoLabel", infoLabel);

        return panel;
    }


    private void updateMetric(JPanel panel, int targetValue, String infoText, TimeSeries series) {
        JProgressBar bar = (JProgressBar) panel.getClientProperty("progressBar");
        JLabel percent = (JLabel) panel.getClientProperty("percentLabel");
        JLabel infoLabel = (JLabel) panel.getClientProperty("infoLabel");

        if (bar == null || percent == null || infoLabel == null) return;
        
        bar.setValue(targetValue);
        percent.setText(targetValue + "%");

        if (targetValue > 80) bar.setForeground(Color.RED);
        else if (targetValue > 60) bar.setForeground(Color.ORANGE);
        else bar.setForeground(new Color(0, 180, 0));

        infoLabel.setText(infoText);
        
        series.addOrUpdate(new Millisecond(), targetValue);
    }


}
