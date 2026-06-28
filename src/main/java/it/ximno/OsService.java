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

    public List<DiskUsage> getVisibleDisks() {
        return new ArrayList<>(
                getAllDisks().stream()
                        .filter(disk -> !isHiddenDisk(disk))
                        .toList()
        );
    }

    public String getOsFamily() {
        return os.getFamily();
    }

    public String getOsVersion() {
        return os.getVersionInfo().getVersion();
    }

    public int getOsBitness() {
        return os.getBitness();
    }

    public String getFormattedUptime() {
        long uptimeSeconds = os.getSystemUptime();
        long uptimeHours = uptimeSeconds / 3600;
        long uptimeDays = uptimeHours / 24;

        long hoursRemainder = uptimeHours % 24;

        return String.format("%d days, %d hours", uptimeDays, hoursRemainder);
    }

    public record DiskUsage(
            String name,
            String mount,
            String type,
            long totalBytes,
            long usableBytes
    ) {
        public double usedPercent() {
            if (totalBytes == 0) {
                return 0.0;
            }
            long used = totalBytes - usableBytes;
            return (used * 100.0) / totalBytes;
        }

        public double totalGb() {
            return totalBytes / (1024.0 * 1024 * 1024);
        }

        public double usedGb() {
            long usedBytes = totalBytes - usableBytes;
            return usedBytes / (1024.0 * 1024 * 1024);
        }
    }
}