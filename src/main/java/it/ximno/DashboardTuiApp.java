package it.ximno;

import com.googlecode.lanterna.TerminalPosition;
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

        //CPU Usage
        textGraphics.drawRectangle(
                new TerminalPosition(2, 4),
                new TerminalSize(30, 5),
                '*'
        );
        textGraphics.putString(4, 5, "CPU");

        String cpuText = String.format("Usage: %.2f%%", cpuUsage);
        textGraphics.putString(4, 6, cpuText);

        //RAM Usage
        textGraphics.drawRectangle(
                new TerminalPosition(35, 4),
                new TerminalSize(30, 5),
                '*'
        );
        textGraphics.putString(37, 5, "RAM");

        String ramText = String.format("Used: %.2f%%", (ramUsage));
        textGraphics.putString(37, 6, ramText);

        // Disks Usage
        var disks = showHiddenDisks
                ? statsService.getAllDisks()
                : statsService.getVisibleDisks();

        // Ordina i dischi per mount
        disks.sort(java.util.Comparator.comparing(SystemStatsService.DiskUsage::mount));

        // Costruiamo le stringhe che mostreremo
        List<String> lines = new ArrayList<>();
        for (SystemStatsService.DiskUsage disk : disks) {
            String line = String.format(
                    "%s (%s): %.1f%%",
                    disk.name(),
                    disk.mount(),
                    disk.usedPercent()
            );
            lines.add(line);
        }

        // Larghezza minima + margine
        int minWidth = 35; // un po' più ampia della vecchia 30
        int maxLineLen = lines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        int boxWidth = Math.max(minWidth, maxLineLen + 4); // +4 per bordi/margine

        // Altezza: 2 righe (titolo + almeno una) + righe per ogni disco + eventuali separatori
        int diskCount = disks.size();
        int separators = diskCount >= 2 ? diskCount - 1 : 0;
        int boxHeight = 3 + lines.size() + separators; // bordo sopra + titolo + dischi + separatori + bordo sotto

        int boxX = 68;
        int boxY = 4;

        // Disegniamo la box
        textGraphics.drawRectangle(
                new TerminalPosition(boxX, boxY),
                new TerminalSize(boxWidth, boxHeight),
                '*'
        );

        // Titolo con conteggio
        String title = showHiddenDisks
                ? String.format("Disks (all, %d)", diskCount)
                : String.format("Disks (%d)", diskCount);

        textGraphics.putString(boxX + 2, boxY + 1, title);

        // Righe dei dischi + separatori
        int row = boxY + 2;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (row >= boxY + boxHeight - 1) {
                break;
            }
            textGraphics.putString(boxX + 2, row, line);
            row++;

            // Se ci sono almeno 2 dischi e non siamo sull'ultimo, disegna una linea di separazione
            if (diskCount >= 2 && i < lines.size() - 1 && row < boxY + boxHeight - 1) {
                textGraphics.drawLine(
                        boxX + 1,
                        row,
                        boxX + boxWidth - 2,
                        row,
                        '-'  // carattere separatore
                );
                row++;
            }
        }

        textGraphics.putString(2, size.getRows() - 2, "Terminal size: " + size.getColumns() + "x" + size.getRows());

        screen.refresh();
    }
}