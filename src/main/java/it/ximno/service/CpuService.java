package it.ximno.service;

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

    /**
     * Returns the human-readable CPU model name.
     * Typically, this is the full marketing string, e.g. "Intel(R) Core(TM) i7-8750H CPU @ 2.20GHz".
     */
    public String getCpuName() {
        return processor.getProcessorIdentifier().getName();
    }

    /**
     * Returns the number of physical CPU cores.
     * Physical cores are the real hardware cores, excluding additional logical threads.
     */
    public int getPhysicalCoreCount() {
        return processor.getPhysicalProcessorCount();
    }

    /**
     * Returns the number of logical CPU cores (hardware threads).
     * Logical cores include hyper-threaded threads on top of the physical cores.
     */
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