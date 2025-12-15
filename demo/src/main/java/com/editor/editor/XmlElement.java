package com.editor.editor;

import java.util.*;

/**
 * XML元素类，用于表示XML树形结构
 */
public class XmlElement {
    private String tagName;
    private String id;
    private Map<String, String> attributes;
    private String textContent;
    private List<XmlElement> children;
    private XmlElement parent;

    public XmlElement(String tagName, String id) {
        this.tagName = tagName;
        this.id = id;
        this.attributes = new HashMap<>();
        this.attributes.put("id", id);
        this.children = new ArrayList<>();
        this.textContent = null;
        this.parent = null;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.attributes.put("id", id);
    }

    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }

    public void setAttribute(String name, String value) {
        if ("id".equals(name)) {
            this.id = value;
        }
        this.attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        if (!"id".equals(name)) {
            attributes.remove(name);
        }
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        // 检查混合内容：如果有子元素，不能设置文本
        if (!children.isEmpty() && textContent != null && !textContent.trim().isEmpty()) {
            throw new IllegalArgumentException("XML元素不能同时包含文本和子元素（混合内容）");
        }
        this.textContent = textContent;
    }

    public List<XmlElement> getChildren() {
        return new ArrayList<>(children);
    }

    public void addChild(XmlElement child) {
        // 检查混合内容：如果有文本内容，不能添加子元素
        if (textContent != null && !textContent.trim().isEmpty()) {
            throw new IllegalArgumentException("XML元素不能同时包含文本和子元素（混合内容）");
        }
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        child.parent = this;
        children.add(child);
    }

    public void removeChild(XmlElement child) {
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    public void insertBefore(XmlElement newChild, XmlElement refChild) {
        // 检查混合内容
        if (textContent != null && !textContent.trim().isEmpty()) {
            throw new IllegalArgumentException("XML元素不能同时包含文本和子元素（混合内容）");
        }
        if (newChild.parent != null) {
            newChild.parent.removeChild(newChild);
        }
        newChild.parent = this;
        int index = children.indexOf(refChild);
        if (index >= 0) {
            children.add(index, newChild);
        } else {
            children.add(newChild);
        }
    }

    public XmlElement getParent() {
        return parent;
    }

    public boolean hasTextContent() {
        return textContent != null && !textContent.trim().isEmpty();
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * 检查是否有混合内容
     */
    public boolean hasMixedContent() {
        return hasTextContent() && hasChildren();
    }
}

