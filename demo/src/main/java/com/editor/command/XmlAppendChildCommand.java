package com.editor.command;

import com.editor.editor.XmlEditor;
import com.editor.editor.XmlElement;

/**
 * XML追加子元素命令
 */
public class XmlAppendChildCommand implements Command {
    private final XmlEditor editor;
    private final XmlElement newElement;
    private final XmlElement parent;

    public XmlAppendChildCommand(XmlEditor editor, XmlElement newElement, XmlElement parent) {
        this.editor = editor;
        this.newElement = newElement;
        this.parent = parent;
    }

    @Override
    public void execute() {
        parent.addChild(newElement);
        editor.updateIdMapping(null, newElement.getId(), newElement);
    }

    @Override
    public void undo() {
        parent.removeChild(newElement);
        editor.updateIdMapping(newElement.getId(), null, null);
    }

    @Override
    public boolean canUndo() {
        return true;
    }
}

