package com.editor.command;

import com.editor.editor.TextEditor;
import java.util.List;

/**
 * 替换命令
 */
public class ReplaceCommand implements Command {
    private final TextEditor editor;
    private final int line;
    private final int col;
    private final int len;
    private final String newText;
    private String originalText;

    public ReplaceCommand(TextEditor editor, int line, int col, int len, String newText) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
        this.newText = newText;
    }

    @Override
    public void execute() {
        List<String> lines = editor.getMutableLines();
        if (line <= lines.size()) {
            String currentLine = lines.get(line - 1);
            int endPos = Math.min(col - 1 + len, currentLine.length());
            originalText = currentLine.substring(col - 1, endPos);
        }
        editor.replace(line, col, len, newText);
    }

    @Override
    public void undo() {
        if (originalText != null) {
            List<String> lines = editor.getMutableLines();
            if (line <= lines.size()) {
                String currentLine = lines.get(line - 1);
                int newTextLen = newText.length();
                String newLine = currentLine.substring(0, col - 1) + originalText + 
                               currentLine.substring(col - 1 + newTextLen);
                lines.set(line - 1, newLine);
                editor.setModified(true);
            }
        }
    }

    @Override
    public boolean canUndo() {
        return originalText != null;
    }
}

