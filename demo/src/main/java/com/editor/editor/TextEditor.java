package com.editor.editor;

import com.editor.command.Command;
import com.editor.observer.Event;
import com.editor.observer.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 文本编辑器类
 */
public class TextEditor implements Editor {
    private final String filePath;
    private List<String> lines;
    private boolean modified;
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private final List<com.editor.observer.Observer> observers;

    public TextEditor(String filePath) {
        this.filePath = filePath;
        this.lines = new ArrayList<>();
        this.modified = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.observers = new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    /**
     * 获取可修改的行列表（用于命令模式）
     */
    public List<String> getMutableLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = new ArrayList<>(lines);
        this.modified = true;
    }

    /**
     * 追加文本到文件末尾
     */
    public void append(String text) {
        if (text == null) {
            text = "";
        }
        lines.add(text);
        modified = true;
        notifyObservers(new Event("EDIT", "append", filePath));
    }

    /**
     * 在指定位置插入文本
     */
    public void insert(int line, int col, String text) {
        if (line < 1 || line > lines.size() + 1) {
            throw new IllegalArgumentException("行号超出范围: " + line);
        }
        if (col < 1) {
            throw new IllegalArgumentException("列号必须大于0: " + col);
        }

        if (text == null) {
            text = "";
        }

        if (line > lines.size()) {
            // 追加新行
            lines.add(text);
        } else {
            String currentLine = lines.get(line - 1);
            if (col > currentLine.length() + 1) {
                throw new IllegalArgumentException("列号超出范围: " + col);
            }
            String newLine = currentLine.substring(0, col - 1) + text + currentLine.substring(col - 1);
            lines.set(line - 1, newLine);
        }
        modified = true;
        notifyObservers(new Event("EDIT", "insert", filePath));
    }

    /**
     * 删除指定位置的字符
     */
    public void delete(int line, int col, int len) {
        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号超出范围: " + line);
        }
        if (col < 1) {
            throw new IllegalArgumentException("列号必须大于0: " + col);
        }
        if (len < 0) {
            throw new IllegalArgumentException("长度不能为负数: " + len);
        }

        String currentLine = lines.get(line - 1);
        if (col > currentLine.length() + 1) {
            throw new IllegalArgumentException("列号超出范围: " + col);
        }

        int endPos = Math.min(col - 1 + len, currentLine.length());
        String newLine = currentLine.substring(0, col - 1) + currentLine.substring(endPos);
        lines.set(line - 1, newLine);
        modified = true;
        notifyObservers(new Event("EDIT", "delete", filePath));
    }

    /**
     * 替换指定位置的文本
     */
    public void replace(int line, int col, int len, String text) {
        if (text == null) {
            text = "";
        }
        // 先删除，再插入
        delete(line, col, len);
        insert(line, col, text);
        notifyObservers(new Event("EDIT", "replace", filePath));
    }

    /**
     * 显示指定范围的内容
     */
    public String show(int startLine, int endLine) {
        if (lines.isEmpty()) {
            return "";
        }

        if (startLine < 1) {
            startLine = 1;
        }
        if (endLine < 0 || endLine > lines.size()) {
            endLine = lines.size();
        }
        if (startLine > endLine) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            sb.append(i).append(": ").append(lines.get(i - 1)).append("\n");
        }
        return sb.toString();
    }

    /**
     * 执行命令并加入撤销栈
     */
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * 撤销操作
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        modified = true;
        notifyObservers(new Event("EDIT", "undo", filePath));
        return true;
    }

    /**
     * 重做操作
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        modified = true;
        notifyObservers(new Event("EDIT", "redo", filePath));
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    @Override
    public void save() throws java.io.IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        java.nio.file.Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8);
        modified = false;
    }

    @Override
    public void attach(com.editor.observer.Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(com.editor.observer.Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        for (com.editor.observer.Observer observer : observers) {
            observer.update(event);
        }
    }
}

