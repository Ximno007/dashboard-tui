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
                double cpuUsage = statsService.getCpuUsagePercent();
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

        // CPU Usage
        List<String> cpuLines = List.of(
                String.format("Usage: %.2f%%", cpuUsage)
        );
        Box cpuBox = new Box(
                2,
                4,
                30,
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
                String.format("Used: %.2f / %.2f GB", usedMemGb, totalMemGb)
        );
        Box ramBox = new Box(
                35,
                4,
                30,
                "RAM",
                ramLines,
                false
        );
        ramBox.draw(textGraphics, false);

        // Disks Usage
        var disks = showHiddenDisks
                ? statsService.getAllDisks()
                : statsService.getVisibleDisks();

        disks.sort(java.util.Comparator.comparing(SystemStatsService.DiskUsage::mount));

        List<String> lines = new ArrayList<>();
        for (SystemStatsService.DiskUsage disk : disks) {
            long totalBytes = disk.totalBytes();
            long usableBytes = disk.usableBytes();
            long usedBytes = totalBytes - usableBytes;

            double totalGb = totalBytes / (1024.0 * 1024 * 1024);
            double usedGb = usedBytes / (1024.0 * 1024 * 1024);

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

        int boxX = 68;
        int boxY = 4;

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