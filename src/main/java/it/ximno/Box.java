package it.ximno;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;

import java.util.List;

public class Box {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String title;
    private final List<String> lines;

    /**
     * Creates a rectangular box model with a title and text lines, computing height from content.
     */
    public Box(int x, int y, int width, String title, List<String> lines, boolean withSeparators) {
        this.x = x;
        this.y = y;
        this.width = width;

        int separators = withSeparators && lines.size() >= 2 ? lines.size() - 1 : 0;
        this.height = 3 + lines.size() + separators;

        this.title = title;
        this.lines = lines;
    }

    /**
     * Renders the box on the given TextGraphics, optionally drawing separators between lines.
     */
    public void draw(TextGraphics g, boolean drawSeparators) {
        g.drawRectangle(
                new TerminalPosition(x, y),
                new TerminalSize(width, height),
                '*'
        );

        g.putString(x + 2, y + 1, title);

        int row = y + 2;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (row >= y + height - 1) {
                break;
            }
            g.putString(x + 2, row, line);
            row++;

            if (drawSeparators && lines.size() >= 2 && i < lines.size() - 1 && row < y + height - 1) {
                g.drawLine(
                        x + 1,
                        row,
                        x + width - 2,
                        row,
                        '-'
                );
                row++;
            }
        }
    }
}