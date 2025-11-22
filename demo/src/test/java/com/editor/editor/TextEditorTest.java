package com.editor.editor;

import com.editor.command.AppendCommand;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TextEditor测试类
 */
public class TextEditorTest {
    private TextEditor editor;

    @Before
    public void setUp() {
        editor = new TextEditor("test.txt");
    }

    @Test
    public void testAppend() {
        editor.append("Hello");
        assertEquals(1, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertTrue(editor.isModified());
    }

    @Test
    public void testInsert() {
        editor.append("Hello");
        editor.insert(1, 6, " World");
        assertEquals("Hello World", editor.getLines().get(0));
    }

    @Test
    public void testDelete() {
        editor.append("Hello World");
        editor.delete(1, 6, 6);
        assertEquals("Hello", editor.getLines().get(0));
    }

    @Test
    public void testReplace() {
        editor.append("Hello World");
        editor.replace(1, 1, 5, "Hi");
        assertEquals("Hi World", editor.getLines().get(0));
    }

    @Test
    public void testShow() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");
        String result = editor.show(1, 2);
        assertTrue(result.contains("1: Line 1"));
        assertTrue(result.contains("2: Line 2"));
    }

    @Test
    public void testUndoRedo() {
        editor.append("Hello");
        AppendCommand cmd = new AppendCommand(editor, " World");
        editor.executeCommand(cmd);
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals(" World", editor.getLines().get(1));
        
        assertTrue(editor.undo());
        assertEquals(1, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        
        assertTrue(editor.redo());
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals(" World", editor.getLines().get(1));
    }
}

