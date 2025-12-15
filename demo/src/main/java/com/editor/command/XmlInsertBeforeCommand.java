package com.editor.command;

import com.editor.editor.XmlEditor;
import com.editor.editor.XmlElement;

/**
 * XML插入元素命令（在指定元素前插入）
 */
public class XmlInsertBeforeCommand implements Command {
    private final XmlEditor editor;
    private final XmlElement newElement;
    private final XmlElement refElement;
    private final XmlElement parent;

    public XmlInsertBeforeCommand(XmlEditor editor, XmlElement newElement, XmlElement refElement) {
        this.editor = editor;
        this.newElement = newElement;
        this.refElement = refElement;
        this.parent = refElement.getParent();
    }

    @Override
    public void execute() {
        parent.insertBefore(newElement, refElement);
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


