package it.ximno.service;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;

public class MemoryService {

    private final GlobalMemory memory;

    public MemoryService() {
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

    /**
     * Returns the total size of the system swap space in gigabytes.
     * Swap space is disk-backed virtual memory used when physical RAM is not sufficient.
     */
    public double getSwapTotalGb() {
        VirtualMemory vm = memory.getVirtualMemory();
        long totalBytes = vm.getSwapTotal();
        return totalBytes / (1024.0 * 1024 * 1024);
    }

    /**
     * Returns the amount of swap space currently in use in gigabytes.
     * This indicates how much virtual memory on disk is actively used by the system.
     */
    public double getSwapUsedGb() {
        VirtualMemory vm = memory.getVirtualMemory();
        long usedBytes = vm.getSwapUsed();
        return usedBytes / (1024.0 * 1024 * 1024);
    }
}