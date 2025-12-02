# 文本编辑器

基于命令行的文本编辑器，支持多文件编辑、撤销/重做、日志记录等功能。

## 功能特性

### 工作区命令
- `load <file>` - 加载文件
- `save [file|all]` - 保存文件
- `init <file> [with-log]` - 创建新缓冲区
- `close [file]` - 关闭文件
- `edit <file>` - 切换活动文件
- `editor-list` - 显示文件列表
- `dir-tree [path]` - 显示目录树
- `undo` - 撤销操作
- `redo` - 重做操作
- `exit` - 退出程序

### 文本编辑命令
- `append "text"` - 追加文本
- `insert <line:col> "text"` - 插入文本
- `delete <line:col> <len>` - 删除字符
- `replace <line:col> <len> "text"` - 替换文本
- `show [start:end]` - 显示内容

### 日志命令
- `log-on [file]` - 启用日志
- `log-off [file]` - 关闭日志
- `log-show [file]` - 显示日志

## 编译和运行

### 编译
```bash
mvn clean compile
```

### 运行
先进入bash目录
 cd D:\subjectResource\softwareDesign\pj\demo
```bash
java -cp target/classes com.editor.App
```

### 运行测试
```bash
mvn test
```

## 使用示例

```
> load test.txt
文件已加载: test.txt

> append "Hello World"
文本已追加

> show
1: Hello World

> insert 1:6 "Beautiful "
文本已插入

> show
1: Hello Beautiful World

> save
文件已保存: test.txt

> exit
```

## 架构设计

### 设计模式
- **观察者模式 (Observer)**: 用于事件通知和日志记录
- **命令模式 (Command)**: 实现撤销/重做功能
- **备忘录模式 (Memento)**: 用于工作区状态持久化

### 模块结构
- `com.editor.observer` - 观察者模式实现
- `com.editor.command` - 命令模式实现
- `com.editor.memento` - 备忘录模式实现
- `com.editor.editor` - 编辑器模块
- `com.editor.workspace` - 工作区模块
- `com.editor.logging` - 日志模块

## 注意事项

1. 文件编码统一使用UTF-8
2. 工作区状态保存在 `.editor_workspace` 文件中
3. 日志文件保存在与源文件同目录的 `.filename.log` 文件中
4. 如果文件首行是 `# log`，打开文件时自动启用日志记录




