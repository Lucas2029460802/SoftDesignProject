package com.editor.spellcheck;

/**
 * XML拼写错误（包含元素ID信息）
 */
public class XmlSpellError {
    private final String elementId;
    private final String word;
    private final String suggestion;

    public XmlSpellError(String elementId, String word, String suggestion) {
        this.elementId = elementId;
        this.word = word;
        this.suggestion = suggestion;
    }

    public String getElementId() {
        return elementId;
    }

    public String getWord() {
        return word;
    }

    public String getSuggestion() {
        return suggestion;
    }
}

