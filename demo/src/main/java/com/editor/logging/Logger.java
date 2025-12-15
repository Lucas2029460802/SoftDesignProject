package com.editor.logging;

import com.editor.observer.Event;
import com.editor.observer.Observer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志记录器，实现观察者模式
 */
public class Logger implements Observer {
    private final Map<String, Boolean> logEnabled;
    private final Map<String, PrintWriter> logWriters;
    private final Map<String, Boolean> sessionStarted;
    private final SimpleDateFormat dateFormat;

    public Logger() {
        this.logEnabled = new HashMap<>();
        this.logWriters = new HashMap<>();
        this.sessionStarted = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    }

    /**
     * 为文件启用日志
     */
    public void enableLog(String filePath) {
        logEnabled.put(filePath, true);
        if (!sessionStarted.getOrDefault(filePath, false)) {
            startSession(filePath);
        }
    }

    /**
     * 为文件关闭日志
     */
    public void disableLog(String filePath) {
        logEnabled.put(filePath, false);
    }

    /**
     * 检查文件是否启用了日志
     */
    public boolean isLogEnabled(String filePath) {
        return logEnabled.getOrDefault(filePath, false);
    }

    /**
     * 开始新的会话
     */
    private void startSession(String filePath) {
        try {
            Path logPath = getLogPath(filePath);
            PrintWriter writer = new PrintWriter(new FileWriter(logPath.toFile(), true));
            logWriters.put(filePath, writer);
            sessionStarted.put(filePath, true);
            
            String timestamp = dateFormat.format(new Date());
            writer.println("session start at " + timestamp);
            writer.flush();
        } catch (IOException e) {
            System.err.println("警告: 无法创建日志文件 " + getLogPath(filePath) + ": " + e.getMessage());
        }
    }

    /**
     * 记录命令
     */
    public void logCommand(String filePath, String command) {
        if (!isLogEnabled(filePath)) {
            return;
        }

        try {
            PrintWriter writer = logWriters.get(filePath);
            if (writer == null) {
                startSession(filePath);
                writer = logWriters.get(filePath);
            }
            
            if (writer != null) {
                String timestamp = dateFormat.format(new Date());
                writer.println(timestamp + " " + command);
                writer.flush();
            }
        } catch (Exception e) {
            System.err.println("警告: 日志记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志文件路径
     */
    private Path getLogPath(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        Path parent = path.getParent();
        if (parent == null) {
            return Paths.get("." + fileName + ".log");
        }
        return parent.resolve("." + fileName + ".log");
    }

    /**
     * 读取并显示日志内容
     */
    public String readLog(String filePath) {
        try {
            Path logPath = getLogPath(filePath);
            if (!Files.exists(logPath)) {
                return "日志文件不存在";
            }
            return Files.readString(logPath);
        } catch (IOException e) {
            return "读取日志文件失败: " + e.getMessage();
        }
    }

    /**
     * 关闭所有日志文件
     */
    public void closeAll() {
        for (PrintWriter writer : logWriters.values()) {
            if (writer != null) {
                writer.close();
            }
        }
        logWriters.clear();
    }

    @Override
    public void update(Event event) {
        String filePath = event.getFilePath();
        if (isLogEnabled(filePath)) {
            logCommand(filePath, event.getCommand());
        }
    }
}






