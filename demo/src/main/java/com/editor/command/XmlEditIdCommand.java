package com.editor.command;

import com.editor.editor.XmlEditor;
import com.editor.editor.XmlElement;

/**
 * XML修改元素ID命令
 */
public class XmlEditIdCommand implements Command {
    private final XmlEditor editor;
    private final XmlElement element;
    private final String oldId;
    private final String newId;

    public XmlEditIdCommand(XmlEditor editor, XmlElement element, String newId) {
        this.editor = editor;
        this.element = element;
        this.oldId = element.getId();
        this.newId = newId;
    }

    @Override
    public void execute() {
        editor.updateIdMapping(oldId, newId, element);
        element.setId(newId);
    }

    @Override
    public void undo() {
        editor.updateIdMapping(newId, oldId, element);
        element.setId(oldId);
    }

    @Override
    public boolean canUndo() {
        return true;
    }
}


