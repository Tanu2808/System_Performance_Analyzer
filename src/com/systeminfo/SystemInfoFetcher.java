package com.systeminfo;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.*;
import java.io.*;
import java.util.List;

public class SystemInfoFetcher {
	
	private static final SystemInfo sysINFO = new SystemInfo();
	private static final HardwareAbstractionLayer hardwareInformation = sysINFO.getHardware();
	private static final OperatingSystem os = sysINFO.getOperatingSystem();
	private static CentralProcessor cpu = hardwareInformation.getProcessor();
	private static long[] prevTicks = cpu.getSystemCpuLoadTicks();
	private static long lastRxBytes = 0;
	private static long lastTxBytes = 0;
	private static long lastCheckTime = System.currentTimeMillis();
	
	
	public static String getOSInfo()
	{
		StringBuilder sb = new StringBuilder();
        sb.append("=== Operating System Info ===\n");
        sb.append("OS: ").append(os.toString()).append("\n");
        sb.append("Version: ").append(os.getVersionInfo()).append("\n");
        sb.append("Family: ").append(os.getFamily()).append("\n\n");
        return sb.toString();
	}
	public static String getCPUInfo() {
	    try {
	        oshi.hardware.CentralProcessor processor = new oshi.SystemInfo().getHardware().getProcessor();
	        String name = processor.getProcessorIdentifier().getName();
	        int physicalCores = processor.getPhysicalProcessorCount();
	        int logicalCores = processor.getLogicalProcessorCount();

	        return "<html><b>=== CPU Info ===</b><br>" +
	                "Name: " + name + "<br>" +
	                "Physical Cores: " + physicalCores + "<br>" +
	                "Logical Cores: " + logicalCores + "</html>";
	    } catch (Exception e) {
	        return "<html><b>=== CPU Info ===</b><br>Error fetching CPU info</html>";
	    }
	}

	public static String getMemoryInfo() {
	    try {
	        oshi.hardware.GlobalMemory memory = new oshi.SystemInfo().getHardware().getMemory();
	        double total = memory.getTotal() / (1024.0 * 1024 * 1024);
	        double available = memory.getAvailable() / (1024.0 * 1024 * 1024);
	        double used = total - available;

	        return String.format("<html><b>=== Physical Memory ===</b><br>Total: %.1f GiB<br>Available: %.1f GiB<br>Used: %.1f GiB</html>",
	                total, available, used);
	    } catch (Exception e) {
	        return "<html><b>=== Physical Memory ===</b><br>Error fetching memory info</html>";
	    }
	}

	public static String getDiskInfo() {
	    try {
	        List<HWDiskStore> disks = new oshi.SystemInfo().getHardware().getDiskStores();
	        StringBuilder info = new StringBuilder("<html><b>=== Disk Drives ===</b><br>");
	        for (oshi.hardware.HWDiskStore disk : disks) {
	            double size = disk.getSize() / (1024.0 * 1024 * 1024);
	            info.append("Name: ").append(disk.getName()).append("<br>")
	                .append("Model: ").append(disk.getModel()).append("<br>")
	                .append(String.format("Size: %.1f GiB<br>", size))
	                .append("Reads: ").append(disk.getReads()).append("<br>")
	                .append("Writes: ").append(disk.getWrites()).append("<br>-----------------------------<br>");
	        }
	        return info.append("</html>").toString();
	    } catch (Exception e) {
	        return "<html><b>=== Disk Drives ===00</b><br>Error fetching disk info</html>";
	    }
	}

	public static String getGPUInfo() {
        StringBuilder info = new StringBuilder("<html><b>=== GPU Information ===</b><br>");
        try {
            SystemInfo si = new SystemInfo();
            List<GraphicsCard> gpus = si.getHardware().getGraphicsCards();

            if (gpus.isEmpty()) {
                info.append("No GPU detected<br>");
            } else {
                GraphicsCard gpu = gpus.get(0);
                long totalVRAM = gpu.getVRam();
//                long usedVRAM = gpu.getVRam();
//                if (usedVRAM < 0) usedVRAM = 0;

                info.append("GPU Name: ").append(gpu.getName()).append("<br>")
                    .append("VRAM: ").append(String.format("%.2f GB", totalVRAM / (1024.0 * 1024 * 1024))).append("<br>")
//                    .append("Used VRAM: ").append(String.format("%.2f GB", usedVRAM / (1024.0 * 1024 * 1024))).append("<br>")
//                    .append("Approx Usage: ").append(getGPUUsage()).append("%<br>")
                    .append("</html>");
            }

        } catch (Exception e) {
            info.append("Error fetching GPU info: ").append(e.getMessage()).append("<br></html>");
        }
        return info.toString();
    }



	public static String getWifiInfo() {
	    StringBuilder wifiInfo = new StringBuilder();
	    try {
	        // Basic info (SSID, Signal, etc.)
	        ProcessBuilder pb = new ProcessBuilder("netsh", "wlan", "show", "interfaces");
	        pb.redirectErrorStream(true);
	        Process process = pb.start();

	        String ssid = "N/A", signal = "N/A";
	        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                line = line.trim();
	                if (line.startsWith("SSID") && !line.startsWith("SSID broadcast"))
	                    ssid = line.split(":", 2)[1].trim();
	                else if (line.startsWith("Signal"))
	                    signal = line.split(":", 2)[1].trim();
	            }
	        }
	        process.waitFor();

	        // Real-time throughput using netstat
	        ProcessBuilder pb2 = new ProcessBuilder("netstat", "-e");
	        pb2.redirectErrorStream(true);
	        Process proc2 = pb2.start();

	        long rxBytes = 0, txBytes = 0;
	        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc2.getInputStream()))) {
	            String l;
	            while ((l = r.readLine()) != null) {
	                if (l.trim().startsWith("Bytes")) {
	                    String[] parts = l.trim().split("\\s+");
	                    if (parts.length >= 3) {
	                        rxBytes = Long.parseLong(parts[1]);
	                        txBytes = Long.parseLong(parts[2]);
	                    }
	                    break;
	                }
	            }
	        }
	        proc2.waitFor();

	        long now = System.currentTimeMillis();
	        long timeDiff = now - lastCheckTime;

	        double rxMbps = 0, txMbps = 0;
	        if (timeDiff > 0 && lastRxBytes > 0) {
	            rxMbps = ((rxBytes - lastRxBytes) * 8.0 / (1024 * 1024)) * (1000.0 / timeDiff);
	            txMbps = ((txBytes - lastTxBytes) * 8.0 / (1024 * 1024)) * (1000.0 / timeDiff);
	        }

	        lastRxBytes = rxBytes;
	        lastTxBytes = txBytes;
	        lastCheckTime = now;

	        wifiInfo.append("<html><b>=== Wi-Fi Information ===</b><br>")
	                .append("SSID: ").append(ssid).append("<br>")
	                .append("Signal: ").append(signal).append("<br>")
	                .append(String.format("Download Speed: %.2f Mbps<br>", rxMbps))
	                .append(String.format("Upload Speed: %.2f Mbps<br>", txMbps))
	                .append("</html>");

	    } catch (Exception e) {
	        wifiInfo.append("<html>Error fetching Wi-Fi info<br>").append(e.getMessage()).append("</html>");
	    }

	    return wifiInfo.toString();
	}
	
	public static int getCPUUsage() {
		long[] ticks = cpu.getSystemCpuLoadTicks();   // current snapshot
	    double load = cpu.getSystemCpuLoadBetweenTicks(prevTicks); // compare with previous
	    prevTicks = ticks; // update for next call
	    return (int) (load * 100);
    }

    public static int getMemoryUsage() {
    	 GlobalMemory memory = hardwareInformation.getMemory();
         return (int) (((memory.getTotal() - memory.getAvailable()) * 100) / memory.getTotal());
    }
    public static int getDiskUsage() {
    	long total = 0, used = 0;
        for (HWDiskStore disk : hardwareInformation.getDiskStores()) {
            total += disk.getSize();
            used += disk.getSize() - disk.getReadBytes(); // Approximation
        }
        return total == 0 ? 0 : (int) ((used * 100) / total);
    }
    public static int getGPUUsage() {
//    	try {
//            SystemInfo si = new SystemInfo();
//            List<GraphicsCard> gpus = si.getHardware().getGraphicsCards();
//
//            if (gpus.isEmpty()) return 0;
//
//            GraphicsCard gpu = gpus.get(0); // pick the first GPU
//
//            long totalVRAM = gpu.getVRam(); // total VRAM in bytes
//            long usedVRAM = gpu.getVRam(); // approximate used VRAM (OSHI 7.0+)
//
//            // Sometimes usedVRAM is unavailable, fall back to 0
//            if (usedVRAM <= 0) usedVRAM = 0;
//
//            // Approximate usage %
//            int usagePercent = (int) ((usedVRAM / (double) totalVRAM) * 100);
//            return Math.min(100, Math.max(0, usagePercent));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        } // fallback if something fails
    	return 0;
    }


    public static int getWifiUsage() {
        try {
            // Use ProcessBuilder instead of deprecated Runtime.exec()
            ProcessBuilder pb = new ProcessBuilder("netstat", "-e");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            long bytesReceived = 0;
            long bytesSent = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("Bytes")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 3) {
                        bytesReceived = Long.parseLong(parts[1]);
                        bytesSent = Long.parseLong(parts[2]);
                    }
                    break;
                }
            }
            reader.close();
            process.waitFor();

            long now = System.currentTimeMillis();
            long timeDiff = now - lastCheckTime;

            if (timeDiff == 0 || lastRxBytes == 0) {
                lastRxBytes = bytesReceived;
                lastTxBytes = bytesSent;
                lastCheckTime = now;
                return 0;
            }

            long rxDiff = bytesReceived - lastRxBytes;
            long txDiff = bytesSent - lastTxBytes;

            lastRxBytes = bytesReceived;
            lastTxBytes = bytesSent;
            lastCheckTime = now;

            // Calculate bytes/sec
            double bytesPerSec = (rxDiff + txDiff) * 1000.0 / timeDiff;
            double mbps = bytesPerSec * 8 / (1024 * 1024); // Convert to Mbps

            // Adjust this based on your Wi-Fi speed
            double maxLinkSpeedMbps = 100.0;
            int usagePercent = (int) Math.min(100, (mbps / maxLinkSpeedMbps) * 100);

            return Math.max(0, usagePercent);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
