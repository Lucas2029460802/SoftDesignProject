package com.editor.observer;

/**
 * 事件类，用于观察者模式
 */
public class Event {
    private final String type;
    private final String command;
    private final String filePath;
    private final long timestamp;

    public Event(String type, String command, String filePath) {
        this.type = type;
        this.command = command;
        this.filePath = filePath;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public String getCommand() {
        return command;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getTimestamp() {
        return timestamp;
    }
}












