package com.editor.spellcheck;

/**
 * 拼写错误信息
 */
public class SpellError {
    private final String word;
    private final int line;
    private final int column;
    private final String suggestion;

    public SpellError(String word, int line, int column, String suggestion) {
        this.word = word;
        this.line = line;
        this.column = column;
        this.suggestion = suggestion;
    }

    public String getWord() {
        return word;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getSuggestion() {
        return suggestion;
    }
}

