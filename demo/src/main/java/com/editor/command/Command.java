package com.editor.command;

/**
 * 命令接口，用于命令模式
 */
public interface Command {
    void execute();
    void undo();
    boolean canUndo();
}












