package com.editor.command;

import com.editor.editor.TextEditor;
import java.util.List;

/**
 * 删除命令
 */
public class DeleteCommand implements Command {
    private final TextEditor editor;
    private final int line;
    private final int col;
    private final int len;
    private String deletedText;

    public DeleteCommand(TextEditor editor, int line, int col, int len) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
    }

    @Override
    public void execute() {
        List<String> lines = editor.getMutableLines();
        if (line <= lines.size()) {
            String currentLine = lines.get(line - 1);
            int endPos = Math.min(col - 1 + len, currentLine.length());
            deletedText = currentLine.substring(col - 1, endPos);
        }
        editor.delete(line, col, len);
    }

    @Override
    public void undo() {
        if (deletedText != null) {
            List<String> lines = editor.getMutableLines();
            if (line <= lines.size()) {
                String currentLine = lines.get(line - 1);
                String newLine = currentLine.substring(0, col - 1) + deletedText + 
                               currentLine.substring(col - 1);
                lines.set(line - 1, newLine);
                editor.setModified(true);
            }
        }
    }

    @Override
    public boolean canUndo() {
        return deletedText != null;
    }
}

