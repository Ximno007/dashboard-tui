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

    private final SystemInfo systemInfo;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final OperatingSystem os;

    public SystemStatsService() {
        this.systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.memory = systemInfo.getHardware().getMemory();
        this.os = systemInfo.getOperatingSystem();
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

        // 1. File system temporanei / RAM disk su Unix (tmpfs, devtmpfs, ramfs, ecc.)
        if (type.contains("tmpfs") || type.contains("ramfs")) {
            return true;
        }

        // 2. Partizioni di sistema/boot, tipicamente non interessanti per l'utente
        if (mount.startsWith("/boot") || mount.contains("efi")) {
            return true;
        }

        // 3. Pseudo-filesystem di sistema (proc, sys, ecc.) se mai dovessero comparire
        if (type.contains("proc") || type.contains("sysfs")) {
            return true;
        }

        // 4. Su Windows, filtra mount "strani" tipo volumi raw senza lettera di drive
        // Esempio: "\\\\?\\Volume{...}" => nascosto
        if (mount.startsWith("\\\\?\\")) {
            return true;
        }

        // Per ora tutto il resto è considerato visibile (NTFS, FAT, APFS, ext4, ecc.)
        return false;
    }

    public List<DiskUsage> getVisibleDisks() {
        return new ArrayList<>(
                getAllDisks().stream()
                        .filter(disk -> !isHiddenDisk(disk))
                        .toList()
        );
    }

    //RECORDS
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
    }
}