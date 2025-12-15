package com.editor;

import java.util.Scanner;

import com.editor.command.CommandParser;
import com.editor.logging.Logger;
import com.editor.workspace.Workspace;

/**
 * 文本编辑器主程序
 */
public class App {
    public static void main(String[] args) {
        Workspace workspace = new Workspace();
        Logger logger = new Logger();
        CommandParser parser = new CommandParser(workspace, logger);

        System.out.println("文本编辑器 v1.0");
        System.out.println("输入 'exit' 退出程序");
        System.out.println();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                
                if (input == null || input.trim().isEmpty()) {
                    continue;
                }

                String result = parser.execute(input.trim());
                
                if ("exit".equals(result)) {
                    // 停止统计计时
                    workspace.getStatistics().stopAll();
                    break;
                }
                
                if (!result.isEmpty()) {
                    System.out.println(result);
                }
            }
        }
    }
}
