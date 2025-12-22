package com.editor.command;

import com.editor.editor.XmlEditor;
import com.editor.editor.XmlElement;

/**
 * XML删除元素命令
 */
public class XmlDeleteElementCommand implements Command {
    private final XmlEditor editor;
    private final XmlElement element;
    private final XmlElement parent;
    private int index;

    public XmlDeleteElementCommand(XmlEditor editor, XmlElement element) {
        this.editor = editor;
        this.element = element;
        this.parent = element.getParent();
        // 记录元素在父元素中的位置
        if (parent != null) {
            this.index = parent.getChildren().indexOf(element);
        }
    }

    @Override
    public void execute() {
        if (parent != null) {
            parent.removeChild(element);
            editor.updateIdMapping(element.getId(), null, null);
        }
    }

    @Override
    public void undo() {
        if (parent != null) {
            // 恢复元素到原来的位置
            if (index >= 0 && index < parent.getChildren().size()) {
                XmlElement refChild = parent.getChildren().get(index);
                parent.insertBefore(element, refChild);
            } else {
                parent.addChild(element);
            }
            editor.updateIdMapping(null, element.getId(), element);
        }
    }

    @Override
    public boolean canUndo() {
        return parent != null;
    }
}







