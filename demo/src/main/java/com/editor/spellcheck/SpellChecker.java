package com.editor.spellcheck;

import java.util.List;

/**
 * 拼写检查器接口（适配器模式的目标接口）
 */
public interface SpellChecker {
    /**
     * 检查文本中的拼写错误
     * @param text 要检查的文本
     * @return 拼写错误列表
     */
    List<SpellError> checkSpelling(String text);

    /**
     * 获取建议的拼写
     * @param word 错误的单词
     * @return 建议的正确拼写
     */
    String getSuggestion(String word);
}

