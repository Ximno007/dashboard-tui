package it.ximno;

import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.FileSystem;

import java.util.ArrayList;
import java.util.List;

public class OsService {

    private final OperatingSystem os;

    public OsService() {
        SystemInfo systemInfo = new SystemInfo();
        this.os = systemInfo.getOperatingSystem();
    }

    /**
     * Returns raw usage information for all mounted file systems on the system.
     * Each DiskUsage entry includes name, mount point, type, total capacity, and usable space in bytes.
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
     * Returns true if the given disk represents a technical or system-only mount
     * that should be hidden from normal users, such as temporary filesystems,
     * kernel pseudo-filesystems, boot/EFI partitions, or special Windows device paths.
     */
    private boolean isHiddenDisk(DiskUsage disk) {
        String type = disk.type().toLowerCase();
        String mount = disk.mount().toLowerCase();

        if (type.contains("tmpfs") || type.contains("ramfs")) {
            return true;
        }
        if (mount.startsWith("/boot") || mount.contains("efi")) {
            return true;
        }
        if (type.contains("proc") || type.contains("sysfs")) {
            return true;
        }
        return mount.startsWith("\\\\?\\");
    }


    /**
     * Returns only the disks that should be shown in the UI, filtering out temporary,
     * virtual, kernel, and special system mounts (e.g. tmpfs, proc, EFI, Windows device paths).
     */
    public List<DiskUsage> getVisibleDisks() {
        return new ArrayList<>(
                getAllDisks().stream()
                        .filter(disk -> !isHiddenDisk(disk))
                        .toList()
        );
    }

    /**
     * Returns the operating system family name, such as "Windows", "Linux" or "macOS".
     */
    public String getOsFamily() {
        return os.getFamily();
    }

    /**
     * Returns the operating system version string provided by the system,
     * typically including major and minor version numbers.
     */
    public String getOsVersion() {
        return os.getVersionInfo().getVersion();
    }


    /**
     * Returns the operating system bitness (e.g. 32 or 64),
     * indicating whether the OS is 32-bit or 64-bit.
     */
    public int getOsBitness() {
        return os.getBitness();
    }

    /**
     * Returns the system uptime formatted as a human-readable string
     * in "X days, Y hours, Z minutes" based on the OS-reported uptime in seconds.
     */
    public String getFormattedUptime() {
        long uptimeSeconds = os.getSystemUptime();
        long uptimeMinutes = uptimeSeconds / 60;
        long uptimeHours = uptimeMinutes / 60;
        long uptimeDays = uptimeHours / 24;

        long hoursRemainder = uptimeHours % 24;
        long minutesRemainder = uptimeMinutes % 60;

        return String.format("%d days, %d hours, %d minutes",
                uptimeDays, hoursRemainder, minutesRemainder);
    }

    /**
     * Returns the current number of processes running on the operating system.
     */
    public int getProcessCount() {
        return os.getProcessCount();
    }

    /**
     * Returns the current number of threads running on the operating system.
     */
    public int getThreadCount() {
        return os.getThreadCount();
    }

    /**
     * Simple value object describing disk usage for a single mounted file system.
     * Contains name, mount point, type, total capacity in bytes, and usable space in bytes.
     */
    public record DiskUsage(
            String name,
            String mount,
            String type,
            long totalBytes,
            long usableBytes
    ) {
        /**
         * Returns the percentage of disk space currently used, between 0 and 100.
         * If totalBytes is 0 (e.g. a disconnected or special volume), returns 0.0.
         */
        public double usedPercent() {
            if (totalBytes == 0) {
                return 0.0;
            }
            long used = totalBytes - usableBytes;
            return (used * 100.0) / totalBytes;
        }

        /**
         * Returns the total disk capacity in gigabytes, converted from totalBytes.
         */
        public double totalGb() {
            return totalBytes / (1024.0 * 1024 * 1024);
        }

        /**
         * Returns the amount of disk space currently used in gigabytes,
         * computed as (totalBytes - usableBytes) converted from bytes.
         */
        public double usedGb() {
            long usedBytes = totalBytes - usableBytes;
            return usedBytes / (1024.0 * 1024 * 1024);
        }
    }
}