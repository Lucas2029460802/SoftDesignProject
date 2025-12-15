package com.editor.editor;

import com.editor.command.Command;
import com.editor.observer.Event;
import com.editor.observer.Subject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML编辑器类
 */
public class XmlEditor implements Editor {
    private final String filePath;
    private XmlElement root;
    private final Map<String, XmlElement> idMap; // id -> element 映射
    private boolean modified;
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private final List<com.editor.observer.Observer> observers;
    private boolean logEnabled;

    public XmlEditor(String filePath) {
        this.filePath = filePath;
        this.idMap = new HashMap<>();
        this.modified = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.observers = new ArrayList<>();
        this.logEnabled = false;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public XmlElement getRoot() {
        return root;
    }

    public void setRoot(XmlElement root) {
        this.root = root;
        rebuildIdMap();
    }

    public XmlElement getElementById(String id) {
        return idMap.get(id);
    }

    public void setLogEnabled(boolean enabled) {
        this.logEnabled = enabled;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    /**
     * 从文件加载XML
     */
    public void loadFromFile() throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }
        List<String> lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
        String content = String.join("\n", lines);
        this.root = parseXml(content);
        rebuildIdMap();
        
        // 检查根元素是否有 log="true" 属性
        if (root != null && "true".equals(root.getAttribute("log"))) {
            logEnabled = true;
        }
        
        this.modified = false;
    }

    /**
     * 简单的XML解析器（适配器模式的基础）
     */
    private XmlElement parseXml(String content) {
        // 移除XML声明
        content = content.replaceFirst("<\\?xml[^>]*\\?>", "").trim();
        
        if (content.isEmpty()) {
            return null;
        }

        // 使用简单的正则表达式解析（实际项目中应使用专业的XML解析器）
        return parseElement(content, 0).element;
    }

    private ParseResult parseElement(String content, int start) {
        // 跳过空白
        while (start < content.length() && Character.isWhitespace(content.charAt(start))) {
            start++;
        }
        
        if (start >= content.length() || content.charAt(start) != '<') {
            return new ParseResult(null, start);
        }

        // 解析开始标签
        int tagEnd = content.indexOf('>', start);
        if (tagEnd == -1) {
            throw new IllegalArgumentException("XML格式错误: 未找到标签结束符");
        }

        String tagContent = content.substring(start + 1, tagEnd);
        boolean isSelfClosing = tagContent.endsWith("/");
        if (isSelfClosing) {
            tagContent = tagContent.substring(0, tagContent.length() - 1).trim();
        }

        // 解析标签名和属性
        String[] parts = tagContent.split("\\s+", 2);
        String tagName = parts[0];
        Map<String, String> attrs = new HashMap<>();
        String id = null;

        if (parts.length > 1) {
            // 解析属性
            Pattern attrPattern = Pattern.compile("(\\w+)=\"([^\"]+)\"");
            Matcher matcher = attrPattern.matcher(parts[1]);
            while (matcher.find()) {
                String attrName = matcher.group(1);
                String attrValue = matcher.group(2);
                attrs.put(attrName, attrValue);
                if ("id".equals(attrName)) {
                    id = attrValue;
                }
            }
        }

        if (id == null) {
            throw new IllegalArgumentException("XML元素缺少必需的id属性: " + tagName);
        }

        XmlElement element = new XmlElement(tagName, id);
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            element.setAttribute(entry.getKey(), entry.getValue());
        }

        if (isSelfClosing) {
            return new ParseResult(element, tagEnd + 1);
        }

        // 查找结束标签
        int nextStart = tagEnd + 1;
        String endTag = "</" + tagName + ">";
        int endTagPos = content.indexOf(endTag, nextStart);

        if (endTagPos == -1) {
            throw new IllegalArgumentException("XML格式错误: 未找到结束标签 " + endTag);
        }

        // 检查是否有子元素或文本内容
        String innerContent = content.substring(nextStart, endTagPos).trim();
        
        if (innerContent.isEmpty()) {
            // 空元素
            return new ParseResult(element, endTagPos + endTag.length());
        }

        if (innerContent.startsWith("<")) {
            // 有子元素
            int pos = 0;
            while (pos < innerContent.length()) {
                ParseResult childResult = parseElement(innerContent, pos);
                if (childResult.element == null) {
                    break;
                }
                element.addChild(childResult.element);
                pos = childResult.nextPos;
            }
        } else {
            // 只有文本内容
            element.setTextContent(innerContent);
        }

        return new ParseResult(element, endTagPos + endTag.length());
    }

    private static class ParseResult {
        XmlElement element;
        int nextPos;

        ParseResult(XmlElement element, int nextPos) {
            this.element = element;
            this.nextPos = nextPos;
        }
    }

    /**
     * 重建id映射
     */
    private void rebuildIdMap() {
        idMap.clear();
        if (root != null) {
            buildIdMap(root);
        }
    }

    private void buildIdMap(XmlElement element) {
        idMap.put(element.getId(), element);
        for (XmlElement child : element.getChildren()) {
            buildIdMap(child);
        }
    }

    /**
     * 序列化为XML字符串
     */
    public String toXmlString() {
        if (root == null) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        serializeElement(root, sb, 0);
        return sb.toString();
    }

    private void serializeElement(XmlElement element, StringBuilder sb, int indent) {
        // 缩进
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }

        // 开始标签
        sb.append("<").append(element.getTagName());
        
        // 属性
        Map<String, String> attrs = element.getAttributes();
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            sb.append(" ").append(entry.getKey()).append("=\"").append(escapeXml(entry.getValue())).append("\"");
        }

        if (element.hasChildren()) {
            // 有子元素
            sb.append(">\n");
            for (XmlElement child : element.getChildren()) {
                serializeElement(child, sb, indent + 1);
            }
            // 缩进
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append("</").append(element.getTagName()).append(">\n");
        } else if (element.hasTextContent()) {
            // 有文本内容
            sb.append(">").append(escapeXml(element.getTextContent())).append("</").append(element.getTagName()).append(">\n");
        } else {
            // 空元素
            sb.append("/>\n");
        }
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }

    @Override
    public void save() throws IOException {
        Path path = Paths.get(filePath);
        String xmlContent = toXmlString();
        Files.write(path, xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        modified = false;
    }

    @Override
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        modified = true;
        notifyObservers(new Event("EDIT", "xml-command", filePath));
    }

    @Override
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

    @Override
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

    @Override
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    @Override
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 更新id映射（当元素id改变时调用）
     */
    public void updateIdMapping(String oldId, String newId, XmlElement element) {
        if (oldId != null) {
            idMap.remove(oldId);
        }
        if (newId != null) {
            idMap.put(newId, element);
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


