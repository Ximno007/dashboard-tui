package it.ximno;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.FileSystem;

import java.util.ArrayList;
import java.util.List;

public class SystemStatsService {

    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final OperatingSystem os;

    public SystemStatsService() {
        SystemInfo systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.memory = systemInfo.getHardware().getMemory();
        this.os = systemInfo.getOperatingSystem();
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
     * Returns all file stores (disks/volumes) as DiskUsage objects.
     */
    public List<DiskUsage> getAllDisks() {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();

        List<DiskUsage> result = new ArrayList<>();

        for (OSFileStore store : fileStores) {
            String name = store.getName();
            String mount = store.getMount();
            String type = store.getType();
            long total = store.getTotalSpace();
            long usable = store.getUsableSpace();

            result.add(new DiskUsage(name, mount, type, total, usable));
        }

        return result;
    }

    /**
     * Determines whether a disk should be hidden from the dashboard,
     * filtering out temporary, boot, and system-only file systems across platforms.
     */
    private boolean isHiddenDisk(DiskUsage disk) {
        String type = disk.type().toLowerCase();
        String mount = disk.mount().toLowerCase();

        // 1. Temporary / RAM-backed file systems on Unix (tmpfs, devtmpfs, ramfs, etc.)
        if (type.contains("tmpfs") || type.contains("ramfs")) {
            return true;
        }

        // 2. Boot / EFI partitions, typically not relevant to end users
        if (mount.startsWith("/boot") || mount.contains("efi")) {
            return true;
        }

        // 3. System pseudo file systems (proc, sysfs, etc.)
        if (type.contains("proc") || type.contains("sysfs")) {
            return true;
        }

        // 4. On Windows, hide raw volumes without a drive letter (e.g. "\\\\?\\Volume{...}")
        return mount.startsWith("\\\\?\\");

        // Everything else is considered user-visible (NTFS, FAT, APFS, ext4, etc.)
    }

    /**
     * Returns only user-visible disks, filtering out tmpfs, boot/EFI and system mounts.
     */
    public List<DiskUsage> getVisibleDisks() {
        return new ArrayList<>(
                getAllDisks().stream()
                        .filter(disk -> !isHiddenDisk(disk))
                        .toList()
        );
    }

    /**
     * Immutable disk usage snapshot for a single file store.
     */
    public record DiskUsage(
            String name,
            String mount,
            String type,
            long totalBytes,
            long usableBytes
    ) {
        /**
         * Returns disk usage as a percentage of total space.
         */
        public double usedPercent() {
            if (totalBytes == 0) {
                return 0.0;
            }
            long used = totalBytes - usableBytes;
            return (used * 100.0) / totalBytes;
        }
    }
}