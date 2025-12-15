# 多文件编辑器 (Lab2)

基于命令行的多文件编辑器，支持文本编辑器和XML编辑器，包含编辑时长统计、拼写检查等功能。

## 功能特性

### 工作区命令
- `load <file>` - 加载文件（自动识别.txt和.xml文件）
- `save [file|all]` - 保存文件
- `init <file> [with-log]` - 创建新缓冲区（支持.txt和.xml）
- `close [file]` - 关闭文件
- `edit <file>` - 切换活动文件
- `editor-list` - 显示文件列表和编辑时长
- `dir-tree [path]` - 显示目录树
- `undo` - 撤销操作
- `redo` - 重做操作
- `exit` - 退出程序

### 文本编辑命令（仅用于.txt文件）
- `append "text"` - 追加文本
- `insert <line:col> "text"` - 插入文本
- `delete <line:col> <len>` - 删除字符
- `replace <line:col> <len> "text"` - 替换文本
- `show [start:end]` - 显示内容

### XML编辑命令（仅用于.xml文件）
- `insert-before <ref-id> <tag> <id> [属性...]` - 在指定元素前插入新元素
- `append-child <parent-id> <tag> <id> [属性...]` - 追加子元素
- `edit-id <old-id> <new-id>` - 修改元素ID
- `edit-text <id> "text"` - 修改元素文本内容
- `delete-element <id>` - 删除元素
- `xml-tree [file]` - 显示XML树形结构

### 拼写检查命令
- `spell-check [file]` - 检查文本文件或XML文件的拼写错误

### 日志命令
- `log-on [file]` - 启用日志
- `log-off [file]` - 关闭日志
- `log-show [file]` - 显示日志

## 编译和运行

### 前置要求
- JDK 8 或更高版本（推荐 JDK 21）
- Maven 3.x

### 编译项目
在项目根目录（`demo`文件夹）下执行：
```bash
mvn clean compile
```

### 运行程序

**方法1：使用 Maven 运行（推荐）**
```bash
mvn exec:java
```

**方法2：使用 Java 命令运行**
```bash
# Windows PowerShell
cd D:\subjectResource\softwareDesign\pj\demo
java -cp target/classes com.editor.App

# 或者使用 Maven 编译后的完整 classpath
java -cp "target/classes;target/dependency/*" com.editor.App
```

**方法3：打包后运行**
```bash
# 打包成 JAR
mvn clean package

# 运行 JAR
java -jar target/demo-1.0-SNAPSHOT.jar
```

### 运行测试
```bash
mvn test
```

## 使用示例

### 文本编辑器示例
```
> init test.txt
新缓冲区已创建: test.txt

> append "Hello World"
文本已追加

> show
1: Hello World

> insert 1:6 "Beautiful "
文本已插入

> show
1: Hello Beautiful World

> editor-list
> * test.txt (45秒)

> save
文件已保存: test.txt

> spell-check
拼写检查结果:
未发现拼写错误
```

### XML编辑器示例
```
> init bookstore.xml
新缓冲区已创建: bookstore.xml

> append-child root book book1 category="COOKING"
子元素已追加

> append-child book1 title title1 lang="en"
子元素已追加

> edit-text title1 "Everyday Italian"
元素文本已修改

> xml-tree
root [id="root"]
└── book [id="book1", category="COOKING"]
    └── title [id="title1", lang="en"]
        └── "Everyday Italian"

> save
文件已保存: bookstore.xml
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

1. **文件编码**：统一使用UTF-8编码
2. **工作区状态**：保存在 `.editor_workspace` 文件中
3. **日志文件**：保存在与源文件同目录的 `.filename.log` 文件中
4. **自动日志**：
   - 文本文件：如果首行是 `# log`，自动启用日志记录
   - XML文件：如果根元素有 `log="true"` 属性，自动启用日志记录
5. **文件类型识别**：
   - `.txt` 文件使用文本编辑器（TextEditor）
   - `.xml` 文件使用XML编辑器（XmlEditor）
6. **编辑时长统计**：
   - 从文件成为活动文件时开始计时
   - 切换文件或关闭文件时停止计时
   - 在 `editor-list` 命令中显示累计编辑时长
7. **XML文件要求**：
   - 所有XML元素必须有唯一的 `id` 属性
   - 不支持混合内容（元素不能同时包含文本和子元素）
8. **拼写检查**：
   - 文本文件：检查所有文本内容
   - XML文件：只检查元素的文本内容（不包括标签名、属性名和属性值）






