package it.ximno;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

public class DashboardTuiApp {

    private final SystemStatsService statsService = new SystemStatsService();

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

        textGraphics.putString(2, 1, "Dashboard TUI");
        textGraphics.putString(2, 2, "Press Q to quit");

        textGraphics.drawRectangle(
                new TerminalPosition(2, 4),
                new TerminalSize(30, 5),
                '*'
        );
        textGraphics.putString(4, 5, "CPU");

        String cpuText = String.format("Usage: %.2f%%", cpuUsage);
        textGraphics.putString(4, 6, cpuText);

        textGraphics.drawRectangle(
                new TerminalPosition(35, 4),
                new TerminalSize(30, 5),
                '*'
        );
        textGraphics.putString(37, 5, "RAM");

        String ramText = String.format("Used: %.2f%%", (ramUsage) % 100);
        textGraphics.putString(37, 6, ramText);

        textGraphics.putString(2, size.getRows() - 2, "Terminal size: " + size.getColumns() + "x" + size.getRows());

        screen.refresh();
    }
}