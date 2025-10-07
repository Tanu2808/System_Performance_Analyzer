package com.system.monitor;

import java.awt.*;
import javax.swing.*;
import com.systeminfo.SystemInfo;

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
        JPanel networkPanel = createMetricPanel("Network Usage");
        
        mainPanel.add(cpuPanel);
        mainPanel.add(memPanel);
        mainPanel.add(diskPanel);
        mainPanel.add(gpuPanel);
        mainPanel.add(networkPanel);

        frame.add(mainPanel, BorderLayout.CENTER);


        Timer timer = new Timer(1000, e -> {
            updateMetric(cpuPanel, SystemInfo.getCPUUsage());
            updateMetric(memPanel, SystemInfo.getMemoryUsage());
            updateMetric(diskPanel, SystemInfo.getDiskUsage());
            updateMetric(gpuPanel, SystemInfo.getGPUUsage());
            updateMetric(networkPanel, SystemInfo.getNetworkUsage());

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

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // spacing
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(percent);

        // Store references
        panel.putClientProperty("progressBar", progressBar);
        panel.putClientProperty("percentLabel", percent);

        return panel;
    }


    private void updateMetric(JPanel panel, int targetValue) {
        JProgressBar bar = (JProgressBar) panel.getClientProperty("progressBar");
        JLabel percent = (JLabel) panel.getClientProperty("percentLabel");

        if (bar == null || percent == null) return;

        int[] current = { bar.getValue() }; // mutable holder

        Timer animation = new Timer(15, null); // update every 15ms
        animation.addActionListener(e -> {
            if (current[0] < targetValue) current[0]++;
            else if (current[0] > targetValue) current[0]--;
            else ((Timer) e.getSource()).stop();

            bar.setValue(current[0]);
            percent.setText(current[0] + "%");

            if (current[0] > 80) bar.setForeground(Color.RED);
            else if (current[0] > 60) bar.setForeground(Color.ORANGE);
            else bar.setForeground(new Color(0, 180, 0));
        });
        animation.start();
    }


}
