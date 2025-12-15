package com.editor.command;

import com.editor.editor.Editor;
import com.editor.editor.TextEditor;
import com.editor.editor.XmlEditor;
import com.editor.editor.XmlElement;
import com.editor.logging.Logger;
import com.editor.spellcheck.SpellChecker;
import com.editor.spellcheck.SpellError;
import com.editor.spellcheck.SimpleSpellChecker;
import com.editor.workspace.Workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 命令解析器
 */
public class CommandParser {
    private final Workspace workspace;
    private final Logger logger;
    private final SpellChecker spellChecker;

    public CommandParser(Workspace workspace, Logger logger) {
        this.workspace = workspace;
        this.logger = logger;
        this.spellChecker = new SimpleSpellChecker(); // 使用适配器模式
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
                case "insert-before":
                    return executeInsertBefore(args);
                case "append-child":
                    return executeAppendChild(args);
                case "edit-id":
                    return executeEditId(args);
                case "edit-text":
                    return executeEditText(args);
                case "delete-element":
                    return executeDeleteElement(args);
                case "xml-tree":
                    return executeXmlTree(args);
                case "spell-check":
                    return executeSpellCheck(args);
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
        Editor editor = workspace.getEditor(filePath);
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
            Editor editor = workspace.getActiveEditor();
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
        Editor editor = workspace.getEditor(filePath);
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
            Editor editor = workspace.getActiveEditor();
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
            String editTime = workspace.getStatistics().getFormattedEditTime(file);
            sb.append(active).append(status).append(" ").append(file)
              .append(" (").append(editTime).append(")").append("\n");
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
        Editor editor = workspace.getActiveEditor();
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
        Editor editor = workspace.getActiveEditor();
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
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isTextEditor()) {
            return "错误: append命令只能用于文本文件";
        }
        TextEditor textEditor = (TextEditor) editor;
        String text = parseQuotedText(args);
        AppendCommand cmd = new AppendCommand(textEditor, text);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), "append \"" + text + "\"");
        return "文本已追加";
    }

    private String executeInsert(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isTextEditor()) {
            return "错误: insert命令只能用于文本文件";
        }
        TextEditor textEditor = (TextEditor) editor;
        String[] parts = parseInsertArgs(args);
        int line = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        String text = parts[2];
        InsertCommand cmd = new InsertCommand(textEditor, line, col, text);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), 
            "insert " + line + ":" + col + " \"" + text + "\"");
        return "文本已插入";
    }

    private String executeDelete(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isTextEditor()) {
            return "错误: delete命令只能用于文本文件";
        }
        TextEditor textEditor = (TextEditor) editor;
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 2) {
            return "错误: delete命令需要行号:列号和长度";
        }
        String[] pos = parts[0].split(":");
        int line = Integer.parseInt(pos[0]);
        int col = Integer.parseInt(pos[1]);
        int len = Integer.parseInt(parts[1]);
        DeleteCommand cmd = new DeleteCommand(textEditor, line, col, len);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), 
            "delete " + line + ":" + col + " " + len);
        return "文本已删除";
    }

    private String executeReplace(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isTextEditor()) {
            return "错误: replace命令只能用于文本文件";
        }
        TextEditor textEditor = (TextEditor) editor;
        String[] parts = parseReplaceArgs(args);
        int line = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        int len = Integer.parseInt(parts[2]);
        String text = parts[3];
        ReplaceCommand cmd = new ReplaceCommand(textEditor, line, col, len, text);
        editor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), 
            "replace " + line + ":" + col + " " + len + " \"" + text + "\"");
        return "文本已替换";
    }

    private String executeShow(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isTextEditor()) {
            return "错误: show命令只能用于文本文件";
        }
        TextEditor textEditor = (TextEditor) editor;
        if (args.isEmpty()) {
            return textEditor.show(1, -1);
        }
        String[] range = args.split(":");
        if (range.length == 2) {
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            return textEditor.show(start, end);
        }
        return "错误: show命令参数格式错误";
    }

    private String executeLogOn(String args) {
        String filePath;
        if (args.isEmpty()) {
            Editor editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
            filePath = editor.getFilePath();
        } else {
            filePath = args.trim();
        }
        logger.enableLog(filePath);
        workspace.setLogStatus(filePath, true);
        Editor editor = workspace.getEditor(filePath);
        if (editor != null) {
            editor.attach(logger);
        }
        logger.logCommand(filePath, "log-on " + filePath);
        return "日志已启用: " + filePath;
    }

    private String executeLogOff(String args) {
        String filePath;
        if (args.isEmpty()) {
            Editor editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
            filePath = editor.getFilePath();
        } else {
            filePath = args.trim();
        }
        logger.disableLog(filePath);
        workspace.setLogStatus(filePath, false);
        Editor editor = workspace.getEditor(filePath);
        if (editor != null) {
            editor.detach(logger);
        }
        logger.logCommand(filePath, "log-off " + filePath);
        return "日志已关闭: " + filePath;
    }

    private String executeLogShow(String args) {
        String filePath;
        if (args.isEmpty()) {
            Editor editor = workspace.getActiveEditor();
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

    // ========== XML编辑命令 ==========

    private String executeInsertBefore(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isXmlEditor()) {
            return "错误: insert-before命令只能用于XML文件";
        }
        XmlEditor xmlEditor = (XmlEditor) editor;
        
        String[] parts = args.trim().split("\\s+", 3);
        if (parts.length < 3) {
            return "错误: insert-before命令格式: insert-before <ref-id> <tag> <id> [属性...]";
        }
        
        String refId = parts[0];
        String tag = parts[1];
        String newId = parts[2];
        
        XmlElement refElement = xmlEditor.getElementById(refId);
        if (refElement == null) {
            return "错误: 未找到ID为 " + refId + " 的元素";
        }
        
        XmlElement parent = refElement.getParent();
        if (parent == null) {
            return "错误: 根元素不能作为参考元素";
        }
        
        XmlElement newElement = new XmlElement(tag, newId);
        
        // 解析属性（如果有）
        if (parts.length > 3) {
            String attrsStr = parts[3];
            // 简单解析属性 key="value" 格式
            java.util.regex.Pattern attrPattern = java.util.regex.Pattern.compile("(\\w+)=\"([^\"]+)\"");
            java.util.regex.Matcher matcher = attrPattern.matcher(attrsStr);
            while (matcher.find()) {
                newElement.setAttribute(matcher.group(1), matcher.group(2));
            }
        }
        
        try {
            XmlInsertBeforeCommand cmd = new XmlInsertBeforeCommand(xmlEditor, newElement, refElement);
            xmlEditor.executeCommand(cmd);
            workspace.setModified(editor.getFilePath(), true);
            logger.logCommand(editor.getFilePath(), "insert-before " + args);
            return "元素已插入";
        } catch (IllegalArgumentException e) {
            return "错误: " + e.getMessage();
        }
    }

    private String executeAppendChild(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isXmlEditor()) {
            return "错误: append-child命令只能用于XML文件";
        }
        XmlEditor xmlEditor = (XmlEditor) editor;
        
        String[] parts = args.trim().split("\\s+", 3);
        if (parts.length < 3) {
            return "错误: append-child命令格式: append-child <parent-id> <tag> <id> [属性...]";
        }
        
        String parentId = parts[0];
        String tag = parts[1];
        String newId = parts[2];
        
        XmlElement parent = xmlEditor.getElementById(parentId);
        if (parent == null) {
            return "错误: 未找到ID为 " + parentId + " 的元素";
        }
        
        XmlElement newElement = new XmlElement(tag, newId);
        
        // 解析属性（如果有）
        if (parts.length > 3) {
            String attrsStr = parts[3];
            java.util.regex.Pattern attrPattern = java.util.regex.Pattern.compile("(\\w+)=\"([^\"]+)\"");
            java.util.regex.Matcher matcher = attrPattern.matcher(attrsStr);
            while (matcher.find()) {
                newElement.setAttribute(matcher.group(1), matcher.group(2));
            }
        }
        
        try {
            XmlAppendChildCommand cmd = new XmlAppendChildCommand(xmlEditor, newElement, parent);
            xmlEditor.executeCommand(cmd);
            workspace.setModified(editor.getFilePath(), true);
            logger.logCommand(editor.getFilePath(), "append-child " + args);
            return "子元素已追加";
        } catch (IllegalArgumentException e) {
            return "错误: " + e.getMessage();
        }
    }

    private String executeEditId(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isXmlEditor()) {
            return "错误: edit-id命令只能用于XML文件";
        }
        XmlEditor xmlEditor = (XmlEditor) editor;
        
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 2) {
            return "错误: edit-id命令格式: edit-id <old-id> <new-id>";
        }
        
        String oldId = parts[0];
        String newId = parts[1];
        
        XmlElement element = xmlEditor.getElementById(oldId);
        if (element == null) {
            return "错误: 未找到ID为 " + oldId + " 的元素";
        }
        
        if (xmlEditor.getElementById(newId) != null && !newId.equals(oldId)) {
            return "错误: ID " + newId + " 已存在";
        }
        
        XmlEditIdCommand cmd = new XmlEditIdCommand(xmlEditor, element, newId);
        xmlEditor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), "edit-id " + args);
        return "元素ID已修改";
    }

    private String executeEditText(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isXmlEditor()) {
            return "错误: edit-text命令只能用于XML文件";
        }
        XmlEditor xmlEditor = (XmlEditor) editor;
        
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            return "错误: edit-text命令格式: edit-text <id> \"text\"";
        }
        
        String id = parts[0];
        String text = parseQuotedText(parts[1]);
        
        XmlElement element = xmlEditor.getElementById(id);
        if (element == null) {
            return "错误: 未找到ID为 " + id + " 的元素";
        }
        
        try {
            XmlEditTextCommand cmd = new XmlEditTextCommand(xmlEditor, element, text);
            xmlEditor.executeCommand(cmd);
            workspace.setModified(editor.getFilePath(), true);
            logger.logCommand(editor.getFilePath(), "edit-text " + args);
            return "元素文本已修改";
        } catch (IllegalArgumentException e) {
            return "错误: " + e.getMessage();
        }
    }

    private String executeDeleteElement(String args) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            return "错误: 没有活动文件";
        }
        if (!editor.isXmlEditor()) {
            return "错误: delete-element命令只能用于XML文件";
        }
        XmlEditor xmlEditor = (XmlEditor) editor;
        
        String id = args.trim();
        if (id.isEmpty()) {
            return "错误: delete-element命令需要元素ID";
        }
        
        XmlElement element = xmlEditor.getElementById(id);
        if (element == null) {
            return "错误: 未找到ID为 " + id + " 的元素";
        }
        
        XmlElement parent = element.getParent();
        if (parent == null) {
            return "错误: 不能删除根元素";
        }
        
        XmlDeleteElementCommand cmd = new XmlDeleteElementCommand(xmlEditor, element);
        xmlEditor.executeCommand(cmd);
        workspace.setModified(editor.getFilePath(), true);
        logger.logCommand(editor.getFilePath(), "delete-element " + id);
        return "元素已删除";
    }

    private String executeXmlTree(String args) {
        Editor editor;
        if (args.isEmpty()) {
            editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
        } else {
            editor = workspace.getEditor(args.trim());
            if (editor == null) {
                return "错误: 文件未打开: " + args.trim();
            }
        }
        
        if (!editor.isXmlEditor()) {
            return "错误: xml-tree命令只能用于XML文件";
        }
        
        XmlEditor xmlEditor = (XmlEditor) editor;
        XmlElement root = xmlEditor.getRoot();
        if (root == null) {
            return "XML文件为空";
        }
        
        return buildXmlTreeString(root, "", true);
    }

    private String buildXmlTreeString(XmlElement element, String prefix, boolean isLast) {
        StringBuilder sb = new StringBuilder();
        
        // 构建元素信息
        sb.append(prefix);
        if (!prefix.isEmpty()) {
            sb.append(isLast ? "└── " : "├── ");
        }
        
        sb.append(element.getTagName());
        sb.append(" [");
        java.util.Map<String, String> attrs = element.getAttributes();
        boolean first = true;
        for (java.util.Map.Entry<String, String> entry : attrs.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("]");
        
        if (element.hasTextContent()) {
            sb.append("\n").append(prefix);
            if (!prefix.isEmpty()) {
                sb.append(isLast ? "    " : "│   ");
            }
            sb.append("└── \"").append(element.getTextContent()).append("\"");
        }
        
        sb.append("\n");
        
        // 处理子元素
        List<XmlElement> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean last = (i == children.size() - 1);
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            sb.append(buildXmlTreeString(children.get(i), newPrefix, last));
        }
        
        return sb.toString();
    }

    private String executeSpellCheck(String args) {
        Editor editor;
        if (args.isEmpty()) {
            editor = workspace.getActiveEditor();
            if (editor == null) {
                return "错误: 没有活动文件";
            }
        } else {
            editor = workspace.getEditor(args.trim());
            if (editor == null) {
                return "错误: 文件未打开: " + args.trim();
            }
        }
        
        StringBuilder result = new StringBuilder();
        result.append("拼写检查结果:\n");
        
        if (editor.isTextEditor()) {
            // 文本文件拼写检查
            TextEditor textEditor = (TextEditor) editor;
            List<String> lines = textEditor.getLines();
            StringBuilder text = new StringBuilder();
            for (String line : lines) {
                text.append(line).append("\n");
            }
            
            List<SpellError> errors = spellChecker.checkSpelling(text.toString());
            if (errors.isEmpty()) {
                result.append("未发现拼写错误");
            } else {
                for (SpellError error : errors) {
                    result.append("第").append(error.getLine())
                          .append("行，第").append(error.getColumn())
                          .append("列: \"").append(error.getWord())
                          .append("\" -> 建议: ").append(error.getSuggestion()).append("\n");
                }
            }
        } else if (editor.isXmlEditor()) {
            // XML文件拼写检查（只检查元素文本内容）
            XmlEditor xmlEditor = (XmlEditor) editor;
            List<com.editor.spellcheck.XmlSpellError> xmlErrors = new java.util.ArrayList<>();
            collectXmlTextErrors(xmlEditor.getRoot(), xmlErrors);
            
            if (xmlErrors.isEmpty()) {
                result.append("未发现拼写错误");
            } else {
                for (com.editor.spellcheck.XmlSpellError error : xmlErrors) {
                    result.append("元素 ").append(error.getElementId())
                          .append(": \"").append(error.getWord())
                          .append("\" -> 建议: ").append(error.getSuggestion()).append("\n");
                }
            }
        }
        
        return result.toString();
    }

    private void collectXmlTextErrors(XmlElement element, List<com.editor.spellcheck.XmlSpellError> errors) {
        if (element == null) {
            return;
        }
        
        if (element.hasTextContent()) {
            String text = element.getTextContent();
            List<SpellError> textErrors = spellChecker.checkSpelling(text);
            for (SpellError error : textErrors) {
                errors.add(new com.editor.spellcheck.XmlSpellError(
                    element.getId(), 
                    error.getWord(), 
                    error.getSuggestion()
                ));
            }
        }
        
        for (XmlElement child : element.getChildren()) {
            collectXmlTextErrors(child, errors);
        }
    }
}

