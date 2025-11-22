package com.editor.command;

import com.editor.editor.TextEditor;
import java.util.List;

/**
 * 追加命令
 */
public class AppendCommand implements Command {
    private final TextEditor editor;
    private final String text;

    public AppendCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.append(text);
    }

    @Override
    public void undo() {
        // 删除最后一行
        List<String> lines = editor.getMutableLines();
        if (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
            editor.setModified(true);
        }
    }

    @Override
    public boolean canUndo() {
        return true;
    }
}

