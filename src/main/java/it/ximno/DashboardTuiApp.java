package it.ximno;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.SimpleTerminalResizeListener;
import com.googlecode.lanterna.terminal.Terminal;
import it.ximno.service.CpuService;
import it.ximno.service.MemoryService;
import it.ximno.service.NetworkService;
import it.ximno.service.OsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DashboardTuiApp {

    private final MemoryService memoryService = new MemoryService();
    private final OsService osService = new OsService();
    private final CpuService cpuService = new CpuService();
    private final NetworkService networkService = new NetworkService();
    private SimpleTerminalResizeListener resizeListener;
    private boolean showHiddenDisks = false;

    private static final Logger log = LoggerFactory.getLogger(DashboardTuiApp.class);

    /**
     * Entry point of the TUI application.
     * Creates and configures the terminal UI, then starts the main run loop.
     */
    public static void main(String[] args) {
        try {
            log.info("Starting Dashboard TUI...");
            new DashboardTuiApp().run();
        } catch (IOException | InterruptedException e) {
            log.error("Error while running Dashboard TUI", e);
        }
    }

    /**
     * Main loop of the TUI dashboard.
     * Continuously reads system metrics, redraws the screen, and handles basic control flow
     * until the application is terminated.
     */
    private void run() throws IOException, InterruptedException {
        TerminalScreen screen = (TerminalScreen) new DefaultTerminalFactory().createScreen();
        Terminal terminal = screen.getTerminal();
        resizeListener = new SimpleTerminalResizeListener(terminal.getTerminalSize());
        terminal.addResizeListener(resizeListener);

        try {
            screen.startScreen();
            screen.setCursorPosition(null);

            while (true) {
                double cpuUsage = cpuService.getCpuUsagePercent();
                double ramUsage = memoryService.getMemoryUsagePercent();

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

    /**
     * Draws the current dashboard view on the given Lanterna Screen.
     * Uses the provided CPU and RAM usage values to render bars, text, and other UI elements,
     * then refreshes the screen so the updated content becomes visible.
     */
    private void draw(Screen screen, double cpuUsage, double ramUsage) throws IOException {
        screen.clear();

        TextGraphics textGraphics = screen.newTextGraphics();
        TerminalSize size = resizeListener.getLastKnownSize();

        //Base GUI
        textGraphics.putString(2, 1, "Dashboard TUI");
        textGraphics.putString(2, 2, "Press Q to quit | S to toggle hidden disks");

        //OS Info
        String osFamily = osService.getOsFamily();
        String osVersion = osService.getOsVersion();
        int osBitness = osService.getOsBitness();
        String uptime = osService.getFormattedUptime();
        int processCount = osService.getProcessCount();
        int threadCount = osService.getThreadCount();

        textGraphics.putString(2, 4,
                String.format("OS: %s %s (%d-bit)", osFamily, osVersion, osBitness));
        textGraphics.putString(2, 5,
                "Uptime: " + uptime);
        textGraphics.putString(2, 6,
                String.format("Processes: %d | Threads: %d", processCount, threadCount));

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

        int y_AXIS = 8;

        // RAM Usage
        double totalMemGb = memoryService.getTotalMemoryGb();
        double usedMemGb = memoryService.getUsedMemoryGb();
        double swapTotalGb = memoryService.getSwapTotalGb();
        double swapUsedGb = memoryService.getSwapUsedGb();

        List<String> ramLines = List.of(
                String.format("Used: %.2f%%", ramUsage),
                String.format("Usage: %.2f / %.2f GB", usedMemGb, totalMemGb),
                String.format("Swap: %.2f / %.2f GB", swapUsedGb, swapTotalGb)
        );

        int ramMinWidth = 30;
        int ramMaxLineLen = ramLines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        int ramWidth = Math.max(ramMinWidth, ramMaxLineLen + 4);

        int ramX = cpuX + cpuWidth + 2;

        //Disks Usage
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

        String title = showHiddenDisks
                ? String.format("Disks (all, %d)", disks.size())
                : String.format("Disks (%d)", disks.size());

        //Network Info
        String hostname = networkService.getHostname();
        String ipv4Gateway = networkService.getIpv4DefaultGateway();
        String[] dnsServers = networkService.getDnsServers();

        List<String> networkLines = new ArrayList<>();
        networkLines.add("Hostname: " + hostname);
        networkLines.add("IPv4 gateway: " + (ipv4Gateway == null || ipv4Gateway.isEmpty() ? "-" : ipv4Gateway));
        networkLines.add("DNS servers:");
        if (dnsServers != null && dnsServers.length > 0) {
            for (String dns : dnsServers) {
                networkLines.add("  - " + dns);
            }
        } else {
            networkLines.add("  (none configured)");
        }

        int netMinWidth = 35;
        int netMaxLineLen = networkLines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        int netWidth = Math.max(netMinWidth, netMaxLineLen + 4);

        int netX = boxX + boxWidth + 2;

        //Check if terminal is too small
        int rightmostBoxEnd = netX + netWidth;
        int requiredCols = rightmostBoxEnd + 2;
        int maxBoxLines = Math.max(
                cpuLines.size(),
                Math.max(
                        ramLines.size(),
                        Math.max(lines.size(), networkLines.size())
                )
        );

        int boxExtra = 3;
        int footerLines = 2;

        int requiredRows = y_AXIS + maxBoxLines + boxExtra + footerLines;

        int currentCols = size.getColumns();
        int currentRows = size.getRows();
        if (currentCols < requiredCols || currentRows < requiredRows) {
            String msg1 = String.format(
                    "Terminal too small: %dx%d (need at least %dx%d)",
                    currentCols, currentRows, requiredCols, requiredRows
            );
            String msg2 = "Please enlarge the terminal window.";

            int centerX = Math.max(2, (currentCols - msg1.length()) / 2);
            int centerY = Math.max(2, currentRows / 2);

            textGraphics.putString(centerX, centerY, msg1);
            textGraphics.putString(centerX, centerY + 2, msg2);

            screen.refresh();
            return;
        }

        //CPU Box
        Box cpuBox = new Box(
                cpuX,
                y_AXIS,
                cpuWidth,
                "CPU",
                cpuLines,
                false
        );
        cpuBox.draw(textGraphics, false);

        //RAM Box
        Box ramBox = new Box(
                ramX,
                y_AXIS,
                ramWidth,
                "RAM",
                ramLines,
                false
        );
        ramBox.draw(textGraphics, false);

        //Disks Box
        Box disksBox = new Box(
                boxX,
                y_AXIS,
                boxWidth,
                title,
                lines,
                true
        );
        disksBox.draw(textGraphics, true);

        //Network Box
        Box networkBox = new Box(
                netX,
                y_AXIS,
                netWidth,
                "Network",
                networkLines,
                false
        );
        networkBox.draw(textGraphics, false);

        textGraphics.putString(2, size.getRows() - 2, "Terminal size: " + size.getColumns() + "x" + size.getRows());

        screen.refresh();
    }
}