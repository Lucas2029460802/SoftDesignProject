package com.editor.editor;

import com.editor.command.Command;
import com.editor.observer.Subject;

/**
 * 编辑器接口，支持多态设计
 * TextEditor 和 XmlEditor 都实现此接口
 */
public interface Editor extends Subject {
    /**
     * 获取文件路径
     */
    String getFilePath();

    /**
     * 检查是否已修改
     */
    boolean isModified();

    /**
     * 设置修改状态
     */
    void setModified(boolean modified);

    /**
     * 执行命令并加入撤销栈
     */
    void executeCommand(Command command);

    /**
     * 撤销操作
     */
    boolean undo();

    /**
     * 重做操作
     */
    boolean redo();

    /**
     * 检查是否可以撤销
     */
    boolean canUndo();

    /**
     * 检查是否可以重做
     */
    boolean canRedo();

    /**
     * 保存文件
     */
    void save() throws java.io.IOException;

    /**
     * 检查是否为文本编辑器
     */
    default boolean isTextEditor() {
        return this instanceof TextEditor;
    }

    /**
     * 检查是否为XML编辑器
     */
    default boolean isXmlEditor() {
        return this instanceof XmlEditor;
    }
}


