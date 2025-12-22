package com.editor.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计模块：记录每个文件的编辑时长
 */
public class Statistics {
    private final Map<String, Long> editTimes; // 文件路径 -> 累计编辑时长（毫秒）
    private final Map<String, Long> startTimes; // 文件路径 -> 开始编辑时间
    private String currentActiveFile;

    public Statistics() {
        this.editTimes = new HashMap<>();
        this.startTimes = new HashMap<>();
        this.currentActiveFile = null;
    }

    /**
     * 文件成为活动文件时调用
     */
    public void onFileActivated(String filePath) {
        // 停止之前文件的计时
        if (currentActiveFile != null && !currentActiveFile.equals(filePath)) {
            stopTiming(currentActiveFile);
        }
        
        // 开始新文件的计时
        currentActiveFile = filePath;
        startTiming(filePath);
    }

    /**
     * 文件关闭时调用
     */
    public void onFileClosed(String filePath) {
        stopTiming(filePath);
        if (currentActiveFile != null && currentActiveFile.equals(filePath)) {
            currentActiveFile = null;
        }
    }

    /**
     * 开始计时
     */
    private void startTiming(String filePath) {
        startTimes.put(filePath, System.currentTimeMillis());
    }

    /**
     * 停止计时并累计
     */
    private void stopTiming(String filePath) {
        Long startTime = startTimes.remove(filePath);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            editTimes.put(filePath, editTimes.getOrDefault(filePath, 0L) + duration);
        }
    }

    /**
     * 获取文件的累计编辑时长（毫秒）
     */
    public long getEditTime(String filePath) {
        // 如果文件正在编辑，需要加上当前会话的时长
        long total = editTimes.getOrDefault(filePath, 0L);
        if (currentActiveFile != null && currentActiveFile.equals(filePath)) {
            Long startTime = startTimes.get(filePath);
            if (startTime != null) {
                total += System.currentTimeMillis() - startTime;
            }
        }
        return total;
    }

    /**
     * 获取格式化的编辑时长字符串
     */
    public String getFormattedEditTime(String filePath) {
        long totalMs = getEditTime(filePath);
        return formatDuration(totalMs);
    }

    /**
     * 格式化时长
     */
    private String formatDuration(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        
        if (totalSeconds < 60) {
            return totalSeconds + "秒";
        }
        
        long totalMinutes = totalSeconds / 60;
        if (totalMinutes < 60) {
            return totalMinutes + "分钟";
        }
        
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (minutes == 0) {
            return hours + "小时";
        }
        return hours + "小时" + minutes + "分钟";
    }

    /**
     * 文件关闭后重置时长（当文件重新加载时）
     */
    public void resetEditTime(String filePath) {
        editTimes.remove(filePath);
        startTimes.remove(filePath);
    }

    /**
     * 程序退出时停止所有计时
     */
    public void stopAll() {
        if (currentActiveFile != null) {
            stopTiming(currentActiveFile);
            currentActiveFile = null;
        }
    }
}







