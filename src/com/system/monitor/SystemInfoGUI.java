package com.system.monitor;

import java.awt.*;
import javax.swing.*;
import com.systeminfo.SystemInfoFetcher;

public class SystemInfoGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SystemInfoGUI().createDashboard());
    }

    private void createDashboard() {
        JFrame frame = new JFrame("Sky Watch - System Performance Monitor");
//        frame.getContentPane().setBackground(new Color(18, 18, 18));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel("Sky Watch - Real-Time Performance Monitor", SwingConstants.CENTER);
        title.setFont(new Font("Consolas", Font.BOLD, 20));
//        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        frame.add(title, BorderLayout.NORTH);


        JPanel mainPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cpuPanel = createMetricPanel("CPU Usage");
        JPanel memPanel = createMetricPanel("Memory Usage");
        JPanel diskPanel = createMetricPanel("Disk Usage");
        JPanel gpuPanel = createMetricPanel("GPU Usage");
        JPanel networkPanel = createMetricPanel("WIFI Usage");
        
        mainPanel.add(cpuPanel);
        mainPanel.add(memPanel);
        mainPanel.add(diskPanel);
        mainPanel.add(gpuPanel);
        mainPanel.add(networkPanel);

        frame.add(mainPanel, BorderLayout.CENTER);


        Timer timer = new Timer(1000, e -> {
            updateMetric(cpuPanel, SystemInfoFetcher.getCPUUsage(), SystemInfoFetcher.getCPUInfo());
            updateMetric(memPanel, SystemInfoFetcher.getMemoryUsage(), SystemInfoFetcher.getMemoryInfo());
            updateMetric(diskPanel, SystemInfoFetcher.getDiskUsage(), SystemInfoFetcher.getDiskInfo());
            updateMetric(gpuPanel, SystemInfoFetcher.getGPUUsage(), SystemInfoFetcher.getGPUInfo());
            updateMetric(networkPanel, SystemInfoFetcher.getWifiUsage(), SystemInfoFetcher.getWifiInfo());

        });

        timer.setInitialDelay(500);
        timer.start();

        frame.setVisible(true);
    }


    private JPanel createMetricPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBackground(new Color(18, 18, 18));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
//        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 180, 0));
        progressBar.setBackground(new Color(40, 40, 40));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25)); // fixed height, fills width

        JLabel percent = new JLabel("0%");
        percent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        percent.setForeground(Color.WHITE);
        percent.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("Fetching info...");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(Color.DARK_GRAY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // spacing
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(percent);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(infoLabel);

        // Store references
        panel.putClientProperty("progressBar", progressBar);
        panel.putClientProperty("percentLabel", percent);
        panel.putClientProperty("infoLabel", infoLabel);

        return panel;
    }


    private void updateMetric(JPanel panel, int targetValue, String infoText) {
        JProgressBar bar = (JProgressBar) panel.getClientProperty("progressBar");
        JLabel percent = (JLabel) panel.getClientProperty("percentLabel");
        JLabel infoLabel = (JLabel) panel.getClientProperty("infoLabel");

        if (bar == null || percent == null || infoLabel == null) return;

//        int[] current = { bar.getValue() }; // mutable holder
//
//        Timer animation = new Timer(15, null); // update every 15ms
//        animation.addActionListener(e -> {
//            if (current[0] < targetValue) current[0]++;
//            else if (current[0] > targetValue) current[0]--;
//            else ((Timer) e.getSource()).stop();
//
//            bar.setValue(current[0]);
//            percent.setText(current[0] + "%");
//
//            if (current[0] > 80) bar.setForeground(Color.RED);
//            else if (current[0] > 60) bar.setForeground(Color.ORANGE);
//            else bar.setForeground(new Color(0, 180, 0));
//            
//            infoLabel.setText(infoText);
//        });
//        animation.start();
        
        bar.setValue(targetValue);
        percent.setText(targetValue + "%");

        if (targetValue > 80) bar.setForeground(Color.RED);
        else if (targetValue > 60) bar.setForeground(Color.ORANGE);
        else bar.setForeground(new Color(0, 180, 0));

        infoLabel.setText(infoText);
    }


}
