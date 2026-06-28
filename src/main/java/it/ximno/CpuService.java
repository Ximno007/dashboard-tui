package it.ximno;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class CpuService {

    private final CentralProcessor processor;

    public CpuService() {
        SystemInfo systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
    }

    /**
     * Returns current system CPU usage as a percentage between 0 and 100.
     */
    public double getCpuUsagePercent() {
        double load = processor.getSystemCpuLoad(1000L);
        if (load < 0) {
            return 0.0;
        }
        return load * 100.0;
    }

    public String getCpuName() {
        return processor.getProcessorIdentifier().getName();
    }

    public String getCpuVendor() {
        return processor.getProcessorIdentifier().getVendor();
    }

    public int getPhysicalCoreCount() {
        return processor.getPhysicalProcessorCount();
    }

    public int getLogicalCoreCount() {
        return processor.getLogicalProcessorCount();
    }

    /**
     * Returns CPU base/vendor frequency in GHz, or -1.0 if unknown.
     */
    public double getBaseFrequencyGhz() {
        long freqHz = processor.getProcessorIdentifier().getVendorFreq();
        if (freqHz <= 0) {
            return -1.0;
        }
        return freqHz / 1_000_000_000.0;
    }
}