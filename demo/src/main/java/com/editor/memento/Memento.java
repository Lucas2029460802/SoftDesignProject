package com.editor.memento;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 备忘录类，用于保存工作区状态
 */
public class Memento implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<String> openFiles;
    private final String activeFile;
    private final Map<String, Boolean> modifiedStatus;
    private final Map<String, Boolean> logStatus;

    public Memento(List<String> openFiles, String activeFile, 
                   Map<String, Boolean> modifiedStatus, 
                   Map<String, Boolean> logStatus) {
        this.openFiles = openFiles;
        this.activeFile = activeFile;
        this.modifiedStatus = modifiedStatus;
        this.logStatus = logStatus;
    }

    public List<String> getOpenFiles() {
        return openFiles;
    }

    public String getActiveFile() {
        return activeFile;
    }

    public Map<String, Boolean> getModifiedStatus() {
        return modifiedStatus;
    }

    public Map<String, Boolean> getLogStatus() {
        return logStatus;
    }
}

