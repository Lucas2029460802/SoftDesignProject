package com.editor.workspace;

import com.editor.editor.TextEditor;
import com.editor.memento.Memento;
import com.editor.observer.Event;
import com.editor.observer.Subject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 工作区类，管理多个编辑器
 */
public class Workspace implements Subject {
    private static final String WORKSPACE_FILE = ".editor_workspace";
    
    private final Map<String, TextEditor> editors;
    private String activeFile;
    private final Map<String, Boolean> modifiedStatus;
    private final Map<String, Boolean> logStatus;
    private final List<com.editor.observer.Observer> observers;

    public Workspace() {
        this.editors = new HashMap<>();
        this.modifiedStatus = new HashMap<>();
        this.logStatus = new HashMap<>();
        this.observers = new ArrayList<>();
        loadWorkspace();
    }

    /**
     * 加载文件
     */
    public void loadFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }

        TextEditor editor = new TextEditor(filePath);
        List<String> lines = Files.readAllLines(path);
        editor.setLines(lines);
        editor.setModified(false);

        editors.put(filePath, editor);
        modifiedStatus.put(filePath, false);
        activeFile = filePath;

        // 检查文件首行是否为 "# log"
        if (!lines.isEmpty() && "# log".equals(lines.get(0).trim())) {
            logStatus.put(filePath, true);
        } else {
            logStatus.put(filePath, false);
        }

        notifyObservers(new Event("LOAD", "load " + filePath, filePath));
    }

    /**
     * 初始化新缓冲区
     */
    public void initFile(String filePath, boolean withLog) {
        TextEditor editor = new TextEditor(filePath);
        if (withLog) {
            editor.append("# log");
        }
        editors.put(filePath, editor);
        modifiedStatus.put(filePath, true);
        logStatus.put(filePath, withLog);
        activeFile = filePath;
        notifyObservers(new Event("INIT", "init " + filePath, filePath));
    }

    /**
     * 保存文件
     */
    public void saveFile(String filePath) throws IOException {
        TextEditor editor = editors.get(filePath);
        if (editor == null) {
            throw new IllegalArgumentException("文件未打开: " + filePath);
        }

        Path path = Paths.get(filePath);
        List<String> lines = editor.getLines();
        Files.write(path, lines);
        editor.setModified(false);
        modifiedStatus.put(filePath, false);
        notifyObservers(new Event("SAVE", "save " + filePath, filePath));
    }

    /**
     * 保存所有文件
     */
    public void saveAll() throws IOException {
        for (String filePath : editors.keySet()) {
            if (modifiedStatus.getOrDefault(filePath, false)) {
                saveFile(filePath);
            }
        }
        notifyObservers(new Event("SAVE", "save all", null));
    }

    /**
     * 关闭文件
     */
    public void closeFile(String filePath) {
        if (editors.remove(filePath) != null) {
            modifiedStatus.remove(filePath);
            logStatus.remove(filePath);
            if (activeFile != null && activeFile.equals(filePath)) {
                // 切换到其他文件
                if (!editors.isEmpty()) {
                    activeFile = editors.keySet().iterator().next();
                } else {
                    activeFile = null;
                }
            }
            notifyObservers(new Event("CLOSE", "close " + filePath, filePath));
        }
    }

    /**
     * 切换活动文件
     */
    public void setActiveFile(String filePath) {
        if (!editors.containsKey(filePath)) {
            throw new IllegalArgumentException("文件未打开: " + filePath);
        }
        activeFile = filePath;
        notifyObservers(new Event("EDIT", "edit " + filePath, filePath));
    }

    /**
     * 获取当前活动文件
     */
    public TextEditor getActiveEditor() {
        if (activeFile == null) {
            return null;
        }
        return editors.get(activeFile);
    }

    /**
     * 获取指定文件的编辑器
     */
    public TextEditor getEditor(String filePath) {
        return editors.get(filePath);
    }

    /**
     * 获取所有打开的文件
     */
    public List<String> getOpenFiles() {
        return new ArrayList<>(editors.keySet());
    }

    /**
     * 检查文件是否已修改
     */
    public boolean isModified(String filePath) {
        return modifiedStatus.getOrDefault(filePath, false);
    }

    /**
     * 设置文件修改状态
     */
    public void setModified(String filePath, boolean modified) {
        modifiedStatus.put(filePath, modified);
    }

    /**
     * 设置日志状态
     */
    public void setLogStatus(String filePath, boolean enabled) {
        logStatus.put(filePath, enabled);
    }

    /**
     * 获取日志状态
     */
    public boolean getLogStatus(String filePath) {
        return logStatus.getOrDefault(filePath, false);
    }

    /**
     * 创建备忘录
     */
    public Memento createMemento() {
        return new Memento(
            new ArrayList<>(editors.keySet()),
            activeFile,
            new HashMap<>(modifiedStatus),
            new HashMap<>(logStatus)
        );
    }

    /**
     * 恢复备忘录
     */
    public void restoreMemento(Memento memento) {
        // 注意：这里只恢复状态信息，不恢复编辑器内容
        // 编辑器内容需要重新加载
        this.activeFile = memento.getActiveFile();
        this.modifiedStatus.clear();
        this.modifiedStatus.putAll(memento.getModifiedStatus());
        this.logStatus.clear();
        this.logStatus.putAll(memento.getLogStatus());
    }

    /**
     * 保存工作区状态
     */
    public void saveWorkspace() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(WORKSPACE_FILE))) {
            Memento memento = createMemento();
            oos.writeObject(memento);
        } catch (IOException e) {
            System.err.println("警告: 保存工作区状态失败: " + e.getMessage());
        }
    }

    /**
     * 加载工作区状态
     */
    private void loadWorkspace() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(WORKSPACE_FILE))) {
            Memento memento = (Memento) ois.readObject();
            restoreMemento(memento);
            
            // 重新加载文件
            for (String filePath : memento.getOpenFiles()) {
                try {
                    loadFile(filePath);
                } catch (IOException e) {
                    System.err.println("警告: 无法加载文件 " + filePath + ": " + e.getMessage());
                }
            }
            
            if (memento.getActiveFile() != null && editors.containsKey(memento.getActiveFile())) {
                activeFile = memento.getActiveFile();
            }
        } catch (FileNotFoundException e) {
            // 首次运行，没有工作区文件
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("警告: 加载工作区状态失败: " + e.getMessage());
        }
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

