package it.ximno;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class SystemStatsService {

    private final SystemInfo systemInfo;
    private final CentralProcessor processor;

    public SystemStatsService() {
        this.systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
    }

    public double getCpuUsagePercent() {
        double load = processor.getSystemCpuLoad(1000L);
        if (load < 0) {
            return 0.0;
        }
        return load * 100.0;
    }
}