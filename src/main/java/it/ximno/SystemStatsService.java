package it.ximno;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

public class SystemStatsService {

    private final SystemInfo systemInfo;
    private final CentralProcessor processor;
    private final GlobalMemory memory;

    public SystemStatsService() {
        this.systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.memory = systemInfo.getHardware().getMemory();
    }

    public double getCpuUsagePercent() {
        double load = processor.getSystemCpuLoad(1000L);
        if (load < 0) {
            return 0.0;
        }
        return load * 100.0;
    }

    public double getMemoryUsagePercent() {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        if (total == 0) {
            return 0.0;
        }

        return (used * 100.0) / total;
    }
}