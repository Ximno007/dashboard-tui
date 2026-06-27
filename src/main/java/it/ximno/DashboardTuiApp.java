package it.ximno;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class DashboardTuiApp {

    public static void main(String[] args) {
        try {
            new DashboardTuiApp().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();

        try {
            TerminalSize size = terminal.getTerminalSize();

            String message = "Dashboard TUI - press Q to quit";
            int row = size.getRows() / 2;
            int col = (size.getColumns() - message.length()) / 2;

            terminal.clearScreen();
            terminal.setCursorPosition(col, row);
            for (char c : message.toCharArray()) {
                terminal.putCharacter(c);
            }
            terminal.flush();

            while (true) {
                KeyStroke key = terminal.readInput();
                if (key == null) {
                    continue;
                }

                switch (key.getKeyType()) {
                    case Character:
                        char ch = key.getCharacter();
                        if (ch == 'q' || ch == 'Q') {
                            return;
                        }
                        break;
                    case EOF:
                        return;
                    default:
                        break;
                }
            }
        } finally {
            terminal.close();
        }
    }
}