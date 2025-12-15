package com.editor.spellcheck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 简单的拼写检查器实现（适配器）
 * 使用简单的字典进行拼写检查
 */
public class SimpleSpellChecker implements SpellChecker {
    private final Set<String> dictionary;
    
    // 常见英语单词字典（简化版）
    private static final String[] COMMON_WORDS = {
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "hello", "world", "book", "title", "author", "year", "price", "category",
        "everyday", "italian", "harry", "potter", "rowling", "cooking", "children"
    };

    public SimpleSpellChecker() {
        this.dictionary = new HashSet<>();
        // 初始化字典（转换为小写）
        for (String word : COMMON_WORDS) {
            dictionary.add(word.toLowerCase());
        }
        // 添加一些常见变体
        addVariants();
    }

    private void addVariants() {
        // 添加常见单词的复数形式等
        String[] baseWords = {"book", "title", "author", "year", "price", "category"};
        for (String word : baseWords) {
            dictionary.add(word.toLowerCase() + "s");
        }
    }

    @Override
    public List<SpellError> checkSpelling(String text) {
        List<SpellError> errors = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return errors;
        }

        String[] lines = text.split("\n");
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            String[] words = extractWords(line);
            int currentPos = 0;
            
            for (String word : words) {
                if (word.isEmpty()) {
                    continue;
                }
                
                // 找到单词在行中的位置
                int wordStart = line.indexOf(word, currentPos);
                if (wordStart == -1) {
                    currentPos += word.length();
                    continue;
                }
                
                // 检查拼写
                String cleanWord = cleanWord(word);
                if (!isValidWord(cleanWord)) {
                    String suggestion = getSuggestion(cleanWord);
                    errors.add(new SpellError(word, lineNum + 1, wordStart + 1, suggestion));
                }
                currentPos = wordStart + word.length();
            }
        }
        
        return errors;
    }

    /**
     * 从文本中提取单词
     */
    private String[] extractWords(String text) {
        // 使用正则表达式提取单词（字母序列）
        return text.split("[^a-zA-Z]+");
    }

    /**
     * 清理单词（移除标点符号等）
     */
    private String cleanWord(String word) {
        return word.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }

    /**
     * 检查单词是否有效
     */
    private boolean isValidWord(String word) {
        if (word.isEmpty() || word.length() < 2) {
            return true; // 忽略单字符和空字符串
        }
        return dictionary.contains(word.toLowerCase());
    }

    @Override
    public String getSuggestion(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        
        String lowerWord = word.toLowerCase();
        
        // 简单的拼写建议算法（编辑距离）
        String bestMatch = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (String dictWord : dictionary) {
            int distance = editDistance(lowerWord, dictWord);
            if (distance < minDistance && distance <= 2) { // 最多2个字符差异
                minDistance = distance;
                bestMatch = dictWord;
            }
        }
        
        return bestMatch != null ? capitalize(word, bestMatch) : word;
    }

    /**
     * 计算编辑距离（Levenshtein距离）
     */
    private int editDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }

    /**
     * 保持原单词的大小写格式
     */
    private String capitalize(String original, String suggestion) {
        if (original.isEmpty()) {
            return suggestion;
        }
        if (Character.isUpperCase(original.charAt(0))) {
            return suggestion.substring(0, 1).toUpperCase() + suggestion.substring(1);
        }
        return suggestion;
    }
}

