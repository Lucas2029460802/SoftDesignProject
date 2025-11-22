package com.editor.command;

import com.editor.editor.TextEditor;
import com.editor.logging.Logger;
import com.editor.workspace.Workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 命令解析器
 */
public class CommandParser {
    private final Workspace workspace;
    private final Logger logger;

    public CommandParser(Workspace workspace, Logger logger) {
        this.workspace = workspace;
        this.logger = logger;
        // 将logger注册为workspace的观察者
        workspace.attach(logger);
    }

    /**
     * 解析并执行命令
     */
    public String execute(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String[] parts = input.trim().split("\\s+", 2);
        String command = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        try {
            switch (command) {
                case "load":
                    return executeLoad(args);
                case "save":
                    return executeSave(args);
                case "init":
                    return executeInit(args);
                case "close":
                    return executeClose(args);
                case "edit":
                    return executeEdit(args);
                case "editor-list":
                    return executeEditorList();
                case "dir-tree":
                    return executeDirTree(args);
                case "undo":
                    return executeUndo();
                case "redo":
                    return executeRedo();
                case "exit":
                    return executeExit();
                case "append":
                    return executeAppend(args);
                case "insert":
                    return executeInsert(args);
                case "delete":
                    return executeDelete(args);
                case "replace":
                    return executeReplace(args);
                case "show":
                    return executeShow(args);
                case "log-on":
                    return executeLogOn(args);
                case "log-off":
                    return executeLogOff(args);
                case "log-show":
                    return executeLogShow(args);
                default:
                    return "未知命令: " + command;
            }
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

    private String executeLoad(String args) throws IOException {
        if (args.isEmpty()) {
            return "错误: load命令需要文件路径";
        }
        String filePath = args.trim();
        workspace.loadFile(filePath);
        TextEditor editor = workspace.getEditor(filePath);
        if (editor != null && workspace.getLogStatus(filePath)) {
            logger.enableLog(filePath);
            editor.attach(logger);
        }
        logger.logCommand(filePath, "load " + filePath);
        return "文件已加载: " + filePath;
    }

    private String executeSave(String args) throws IOException {
        if (args.isEmpty()) {
            // 保存当前活动文件
            TextEditor editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
            workspace.saveFile(editor.getFilePath());
            logger.logCommand(editor.getFilePath(), "save");
            return "文件已保存: " + editor.getFilePath();
        } else if ("all".equals(args)) {
            workspace.saveAll();
            logger.logCommand(null, "save all");
            return "所有文件已保存";
        } else {
            String filePath = args.trim();
            workspace.saveFile(filePath);
            logger.logCommand(filePath, "save " + filePath);
            return "文件已保存: " + filePath;
        }
    }

    private String executeInit(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length == 0) {
            return "错误: init命令需要文件路径";
        }
        String filePath = parts[0];
        boolean withLog = parts.length > 1 && "with-log".equals(parts[1]);
        workspace.initFile(filePath, withLog);
        TextEditor editor = workspace.getEditor(filePath);
        if (editor != null) {
            if (withLog || workspace.getLogStatus(filePath)) {
                logger.enableLog(filePath);
                editor.attach(logger);
            }
        }
        logger.logCommand(filePath, "init " + args);
        return "新缓冲区已创建: " + filePath;
    }

    private String executeClose(String args) {
        String filePath;
        if (args.isEmpty()) {
            TextEditor editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
            filePath = editor.getFilePath();
        } else {
            filePath = args.trim();
        }
        logger.logCommand(filePath, "close " + filePath);
        workspace.closeFile(filePath);
        return "文件已关闭: " + filePath;
    }

    private String executeEdit(String args) {
        if (args.isEmpty()) {
            return "错误: edit命令需要文件路径";
        }
        String filePath = args.trim();
        workspace.setActiveFile(filePath);
        logger.logCommand(filePath, "edit " + filePath);
        return "已切换到文件: " + filePath;
    }

    private String executeEditorList() {
        List<String> files = workspace.getOpenFiles();
        if (files.isEmpty()) {
            return "没有打开的文件";
        }
        StringBuilder sb = new StringBuilder();
        String activeFile = workspace.getActiveEditor() != null ? 
            workspace.getActiveEditor().getFilePath() : null;
        for (String file : files) {
            String status = workspace.isModified(file) ? "*" : " ";
            String active = file.equals(activeFile) ? ">" : " ";
            sb.append(active).append(status).append(" ").append(file).append("\n");
        }
        return sb.toString();
    }

    private String executeDirTree(String args) {
        String pathStr = args.isEmpty() ? "." : args.trim();
        Path path = Paths.get(pathStr);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return "错误: 路径不存在或不是目录: " + pathStr;
        }
        return buildDirTree(path.toFile(), "", true);
    }

    private String buildDirTree(File file, String prefix, boolean isLast) {
        StringBuilder sb = new StringBuilder();
        String name = file.getName();
        if (name.isEmpty()) {
            name = file.getPath();
        }
        sb.append(prefix).append(isLast ? "└── " : "├── ").append(name).append("\n");

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean last = (i == children.length - 1);
                    String newPrefix = prefix + (isLast ? "    " : "│   ");
                    sb.append(buildDirTree(children[i], newPrefix, last));
                }
            }
        }
        return sb.toString();
    }

    private String executeUndo() {
        TextEditor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (editor.undo()) {
            logger.logCommand(editor.getFilePath(), "undo");
            return "已撤销";
        }
        return "无法撤销";
    }

    private String executeRedo() {
        TextEditor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (editor.redo()) {
            logger.logCommand(editor.getFilePath(), "redo");
            return "已重做";
        }
        return "无法重做";
    }

    private String executeExit() {
        workspace.saveWorkspace();
        logger.closeAll();
        return "exit";
    }

    private String executeAppend(String args) {
        TextEditor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        String text = parseQuotedText(args);
        AppendCommand cmd = new AppendCommand(editor, text);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), "append \"" + text + "\"");
        return "文本已追加";
    }

    private String executeInsert(String args) {
        TextEditor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        String[] parts = parseInsertArgs(args);
        int line = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        String text = parts[2];
        InsertCommand cmd = new InsertCommand(editor, line, col, text);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), 
            "insert " + line + ":" + col + " \"" + text + "\"");
        return "文本已插入";
    }

    private String executeDelete(String args) {
        TextEditor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 2) {
            return "错误: delete命令需要行号:列号和长度";
        }
        String[] pos = parts[0].split(":");
        int line = Integer.parseInt(pos[0]);
        int col = Integer.parseInt(pos[1]);
        int len = Integer.parseInt(parts[1]);
        DeleteCommand cmd = new DeleteCommand(editor, line, col, len);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), 
            "delete " + line + ":" + col + " " + len);
        return "文本已删除";
    }

    private String executeReplace(String args) {
        TextEditor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        String[] parts = parseReplaceArgs(args);
        int line = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        int len = Integer.parseInt(parts[2]);
        String text = parts[3];
        ReplaceCommand cmd = new ReplaceCommand(editor, line, col, len, text);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), 
            "replace " + line + ":" + col + " " + len + " \"" + text + "\"");
        return "文本已替换";
    }

    private String executeShow(String args) {
        TextEditor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (args.isEmpty()) {
            return editor.show(1, -1);
        }
        String[] range = args.split(":");
        if (range.length == 2) {
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            return editor.show(start, end);
        }
        return "错误: show命令参数格式错误";
    }

    private String executeLogOn(String args) {
        String filePath;
        if (args.isEmpty()) {
            TextEditor editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
            filePath = editor.getFilePath();
        } else {
            filePath = args.trim();
        }
        logger.enableLog(filePath);
        workspace.setLogStatus(filePath, true);
        TextEditor editor = workspace.getEditor(filePath);
        if (editor != null) {
            editor.attach(logger);
        }
        logger.logCommand(filePath, "log-on " + filePath);
        return "日志已启用: " + filePath;
    }

    private String executeLogOff(String args) {
        String filePath;
        if (args.isEmpty()) {
            TextEditor editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
            filePath = editor.getFilePath();
        } else {
            filePath = args.trim();
        }
        logger.disableLog(filePath);
        workspace.setLogStatus(filePath, false);
        TextEditor editor = workspace.getEditor(filePath);
        if (editor != null) {
            editor.detach(logger);
        }
        logger.logCommand(filePath, "log-off " + filePath);
        return "日志已关闭: " + filePath;
    }

    private String executeLogShow(String args) {
        String filePath;
        if (args.isEmpty()) {
            TextEditor editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
            filePath = editor.getFilePath();
        } else {
            filePath = args.trim();
        }
        return logger.readLog(filePath);
    }

    /**
     * 解析带引号的文本
     */
    private String parseQuotedText(String args) {
        args = args.trim();
        if (args.startsWith("\"") && args.endsWith("\"")) {
            return args.substring(1, args.length() - 1);
        }
        return args;
    }

    /**
     * 解析insert命令参数
     */
    private String[] parseInsertArgs(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        String[] pos = parts[0].split(":");
        String text = parts.length > 1 ? parseQuotedText(parts[1]) : "";
        return new String[]{pos[0], pos[1], text};
    }

    /**
     * 解析replace命令参数
     */
    private String[] parseReplaceArgs(String args) {
        String[] parts = args.trim().split("\\s+", 3);
        String[] pos = parts[0].split(":");
        int len = Integer.parseInt(parts[1]);
        String text = parts.length > 2 ? parseQuotedText(parts[2]) : "";
        return new String[]{pos[0], pos[1], String.valueOf(len), text};
    }
}

