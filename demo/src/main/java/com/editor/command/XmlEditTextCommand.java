package com.editor.command;

import com.editor.editor.XmlEditor;
import com.editor.editor.XmlElement;

/**
 * XML修改元素文本命令
 */
public class XmlEditTextCommand implements Command {
    private final XmlEditor editor;
    private final XmlElement element;
    private final String oldText;
    private final String newText;

    public XmlEditTextCommand(XmlEditor editor, XmlElement element, String newText) {
        this.editor = editor;
        this.element = element;
        this.oldText = element.getTextContent();
        this.newText = newText;
    }

    @Override
    public void execute() {
        element.setTextContent(newText);
    }

    @Override
    public void undo() {
        element.setTextContent(oldText);
    }

    @Override
    public boolean canUndo() {
        return true;
    }
}

