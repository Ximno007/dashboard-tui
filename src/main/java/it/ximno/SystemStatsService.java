package it.ximno;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

public class SystemStatsService {

    private final GlobalMemory memory;

    public SystemStatsService() {
        SystemInfo systemInfo = new SystemInfo();
        this.memory = systemInfo.getHardware().getMemory();
    }

    /**
     * Returns current system memory usage as a percentage between 0 and 100.
     */
    public double getMemoryUsagePercent() {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        if (total == 0) {
            return 0.0;
        }

        return (used * 100.0) / total;
    }

    /**
     * Returns total physical memory (RAM) in gigabytes.
     */
    public double getTotalMemoryGb() {
        long totalBytes = memory.getTotal();
        return totalBytes / (1024.0 * 1024 * 1024);
    }

    /**
     * Returns used physical memory (RAM) in gigabytes.
     */
    public double getUsedMemoryGb() {
        long totalBytes = memory.getTotal();
        long availableBytes = memory.getAvailable();
        long usedBytes = totalBytes - availableBytes;
        return usedBytes / (1024.0 * 1024 * 1024);
    }
}