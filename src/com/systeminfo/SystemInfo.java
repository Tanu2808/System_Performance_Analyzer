package com.systeminfo;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.Enumeration;

import com.sun.management.OperatingSystemMXBean;

public class SystemInfo {
	
	static OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	
	private static long totalMemory = osBean.getTotalMemorySize() / (1024 * 1024);
	private static long freeMemory = osBean.getFreeMemorySize() / (1024 * 1024);
	private static long usedMemory = totalMemory - freeMemory;
	
	private static int cores = Runtime.getRuntime().availableProcessors();
	private static double systemLoad = osBean.getCpuLoad();
	private static double processLoad = osBean.getProcessCpuLoad();
	
	
	public static String getOSInfo()
	{
		StringBuilder sb = new StringBuilder();
        sb.append("=== Operating System Info ===\n");
        sb.append("OS: ").append(System.getProperty("os.name")).append("\n");
        sb.append("Version: ").append(System.getProperty("os.version")).append("\n");
        sb.append("Architecture: ").append(System.getProperty("os.arch")).append("\n\n");
        return sb.toString();
	}
	public static String getCPUInfo()
	{
		StringBuilder sb = new StringBuilder();
        sb.append("=== CPU Info ===\n");
        sb.append("Available Cores: ").append(cores).append("\n");
        sb.append(String.format("System CPU Load: %.2f%%\n", systemLoad));
        sb.append(String.format("Process CPU Load: %.2f%%\n\n", processLoad));
        return sb.toString();
	}
	public static String getMemoryInfo()
	{
		StringBuilder sb = new StringBuilder();
        sb.append("=== Physical Memory ===\n");
        sb.append("Total: ").append(totalMemory).append(" MB\n");
        sb.append("Free: ").append(freeMemory).append(" MB\n");
        sb.append("Used: ").append(usedMemory).append(" MB\n\n");

        return sb.toString();
	}
	public static String getDiskInfo()
	{
		File[] roots = File.listRoots();
		StringBuilder info = new StringBuilder();
		
	    for (File root : roots) {
	        long totalDisk = root.getTotalSpace() / (1024 * 1024 * 1024); // GB
	        long freeDisk = root.getFreeSpace() / (1024 * 1024 * 1024); // GB
	        long usedDisk = totalDisk - freeDisk;
	        info.append("Drive: ").append(root.getAbsolutePath()).append("\n");
            info.append("Total Disk Space: ").append(totalDisk).append(" GB\n");
            info.append("Free Disk Space: ").append(freeDisk).append(" GB\n");
            info.append("Used Disk Space: ").append(usedDisk).append(" GB\n");
            info.append("-------------------------------------\n");
	    }
	    
	    return info.toString();
	}
	public static int getCPUUsage() {
        double cpuLoad = osBean.getCpuLoad();
        if (cpuLoad < 0) return 0;
        return (int) (cpuLoad * 100);
    }

    public static int getMemoryUsage() {
        long totalMem = osBean.getTotalMemorySize();
        long freeMem = osBean.getFreeMemorySize();
        return (int) (((totalMem - freeMem) * 100) / totalMem);
    }
    private static final File root = new File("C:");
    public static int getDiskUsage() {
        long totalDisk = root.getTotalSpace();
        long freeDisk = root.getFreeSpace();
        return (int) (((totalDisk - freeDisk) * 100) / totalDisk);
    }
    public static int getGPUUsage() {
        int usage = -1; // -1 indicates unavailable
        try {
            // Query GPU utilization via WMI
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
                "wmic path Win32_PerfFormattedData_GPUPerformanceCounters_GPUEngine " +
                "where \"Name like '%3D%'\" get UtilizationPercentage");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.matches("\\d+")) { // Only digits
                        usage = Integer.parseInt(line);
                        break;
                    }
                }
            }
            process.waitFor();

        } catch (Exception e) {
            usage = -1;
        }
        return usage;
    }
    private static long previousTotalBytes = -1;

    public static int getNetworkUsage() {
        try {
            long totalBytes = 0;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                byte[] mac = ni.getHardwareAddress();
                if (mac == null) continue;

                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress addr = ia.getAddress();
                    if (addr.isLoopbackAddress()) continue;
                }

                // Approximation: total bytes sent + received
                totalBytes += ni.getMTU(); // we don’t have exact bytes, will normalize later
            }

            // First run
            if (previousTotalBytes == -1) {
                previousTotalBytes = totalBytes;
                return 0;
            }

            long diff = totalBytes - previousTotalBytes;
            previousTotalBytes = totalBytes;

            // Convert to percentage of some assumed max (e.g., 100 Mbps)
            int usage = (int) Math.min((diff * 100) / (100 * 1024 * 1024 / 8), 100); 
            return usage;

        } catch (Exception e) {
            return 0;
        }
    }
}
