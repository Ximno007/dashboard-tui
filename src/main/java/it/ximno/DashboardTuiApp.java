package it.ximno;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DashboardTuiApp {

    private final SystemStatsService statsService = new SystemStatsService();
    private final OsService osService = new OsService();
    private final CpuService cpuService = new CpuService();
    private boolean showHiddenDisks = false;

    public static void main(String[] args) {
        try {
            new DashboardTuiApp().run();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException, InterruptedException {
        Screen screen = new DefaultTerminalFactory().createScreen();

        try {
            screen.startScreen();
            screen.setCursorPosition(null);

            while (true) {
                double cpuUsage = cpuService.getCpuUsagePercent();
                double ramUsage = statsService.getMemoryUsagePercent();

                draw(screen, cpuUsage, ramUsage);

                KeyStroke key = screen.pollInput();
                if (key != null) {
                    if (key.getKeyType() == KeyType.Character) {
                        char ch = key.getCharacter();
                        if (ch == 'q' || ch == 'Q') {
                            return;
                        }
                        if (ch == 's' || ch == 'S') {
                            showHiddenDisks = !showHiddenDisks;
                        }
                    } else if (key.getKeyType() == KeyType.EOF) {
                        return;
                    }
                }
            }
        } finally {
            screen.stopScreen();
        }
    }

    private void draw(Screen screen, double cpuUsage, double ramUsage) throws IOException {
        screen.clear();

        TextGraphics textGraphics = screen.newTextGraphics();
        TerminalSize size = screen.getTerminalSize();

        //Base GUI
        textGraphics.putString(2, 1, "Dashboard TUI");
        textGraphics.putString(2, 2, "Press Q to quit | S to toggle hidden disks");

        //OS Info
        String osFamily = osService.getOsFamily();
        String osVersion = osService.getOsVersion();
        int osBitness = osService.getOsBitness();
        String uptime = osService.getFormattedUptime();

        textGraphics.putString(2, 4,
                String.format("OS: %s %s (%d-bit)", osFamily, osVersion, osBitness));
        textGraphics.putString(2, 5,
                "Uptime: " + uptime);

        // CPU Usage
        String cpuName = cpuService.getCpuName();
        int physicalCores = cpuService.getPhysicalCoreCount();
        int logicalCores = cpuService.getLogicalCoreCount();
        double baseFreqGhz = cpuService.getBaseFrequencyGhz();

        List<String> cpuLines = List.of(
                String.format("Usage: %.2f%%", cpuUsage),
                cpuName,
                String.format("Cores: %d physical / %d logical", physicalCores, logicalCores),
                baseFreqGhz > 0
                        ? String.format("Base clock: %.2f GHz", baseFreqGhz)
                        : "Base clock: unknown"
        );

        int cpuMinWidth = 30;
        int cpuMaxLineLen = cpuLines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        int cpuWidth = Math.max(cpuMinWidth, cpuMaxLineLen + 4);

        int cpuX = 2;
        int cpuY = 7;

        Box cpuBox = new Box(
                cpuX,
                cpuY,
                cpuWidth,
                "CPU",
                cpuLines,
                false
        );
        cpuBox.draw(textGraphics, false);

        // RAM Usage
        double totalMemGb = statsService.getTotalMemoryGb();
        double usedMemGb = statsService.getUsedMemoryGb();

        List<String> ramLines = List.of(
                String.format("Used: %.2f%%", ramUsage),
                String.format("Usage: %.2f / %.2f GB", usedMemGb, totalMemGb)
        );

        int ramMinWidth = 30;
        int ramMaxLineLen = ramLines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        int ramWidth = Math.max(ramMinWidth, ramMaxLineLen + 4);

        int ramX = cpuX + cpuWidth + 2;
        int ramY = cpuY;

        Box ramBox = new Box(
                ramX,
                ramY,
                ramWidth,
                "RAM",
                ramLines,
                false
        );
        ramBox.draw(textGraphics, false);

        var disks = showHiddenDisks
                ? osService.getAllDisks()
                : osService.getVisibleDisks();

        disks.sort(java.util.Comparator.comparing(OsService.DiskUsage::mount));

        List<String> lines = new ArrayList<>();
        for (OsService.DiskUsage disk : disks) {
            double totalGb = disk.totalGb();
            double usedGb = disk.usedGb();

            String line = String.format(
                    "%s (%s): %.1f%% | %.2f / %.2f GB",
                    disk.name(),
                    disk.mount(),
                    disk.usedPercent(),
                    usedGb,
                    totalGb
            );
            lines.add(line);
        }

        int minWidth = 35;
        int maxLineLen = lines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        int boxWidth = Math.max(minWidth, maxLineLen + 4);

        int boxX = ramX + ramWidth + 2;
        int boxY = ramY;

        String title = showHiddenDisks
                ? String.format("Disks (all, %d)", disks.size())
                : String.format("Disks (%d)", disks.size());

        Box disksBox = new Box(
                boxX,
                boxY,
                boxWidth,
                title,
                lines,
                true
        );
        disksBox.draw(textGraphics, true);

        textGraphics.putString(2, size.getRows() - 2, "Terminal size: " + size.getColumns() + "x" + size.getRows());

        screen.refresh();
    }
}