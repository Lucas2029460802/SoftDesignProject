package com.editor.command;

import com.editor.editor.TextEditor;
import java.util.List;

/**
 * 插入命令
 */
public class InsertCommand implements Command {
    private final TextEditor editor;
    private final int line;
    private final int col;
    private final String text;

    public InsertCommand(TextEditor editor, int line, int col, String text) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.insert(line, col, text);
    }

    @Override
    public void undo() {
        List<String> lines = editor.getMutableLines();
        if (line <= lines.size()) {
            String currentLine = lines.get(line - 1);
            if (col <= currentLine.length() && col + text.length() <= currentLine.length()) {
                String newLine = currentLine.substring(0, col - 1) + 
                               currentLine.substring(col - 1 + text.length());
                lines.set(line - 1, newLine);
            } else if (line > lines.size() - 1) {
                // 如果是新行，删除它
                lines.remove(lines.size() - 1);
            }
            editor.setModified(true);
        }
    }

    @Override
    public boolean canUndo() {
        return true;
    }
}

