package com.systeminfo;

import java.io.File;
import java.lang.management.ManagementFactory;
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
	public static String getAllSystemInfo() {
		// TODO Auto-generated method stub
		return getOSInfo() + getCPUInfo() + getMemoryInfo() + getDiskInfo();
	}
}
