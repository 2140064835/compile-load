package com.clay.loader;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * 将编译好的Class文件保存到内存当中，这里的内存也就是Map映射当中
 *
 * @author clay
 */
public final class MemoryJavaFileManager extends ForwardingJavaFileManager {

    /**
     * 用于存放Class文件的内存
     */
    private Map<String, byte[]> classBytes;

    /**
     * Java源文件的扩展名
     */
    private final static String EXT = ".java";

    public MemoryJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
        classBytes = new HashMap<String, byte[]>();
    }

    public Map<String, byte[]> getClassBytes() {
        return classBytes;
    }

    @Override
    public void close() throws IOException {
        classBytes = new HashMap<String, byte[]>();
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location, String className,
            JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            return new ClassOutputBuffer(className);
        } else {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }

    public JavaFileObject makeStringSource(String name, String code) {
        return new StringInputBuffer(name, code);
    }

    public static URI toURI(String name) {
        File file = new File(name);
        if (file.exists()) {
            return file.toURI();
        } else {
            try {
                final StringBuilder newUri = new StringBuilder();
                newUri.append("mfm:///");
                newUri.append(name.replace('.', '/'));
                if (name.endsWith(EXT)) {
                    newUri.replace(newUri.length() - EXT.length(), newUri.length(), EXT);
                }
                return URI.create(newUri.toString());
            } catch (Exception exp) {
                return URI.create("mfm:///com/sun/script/java/java_source");
            }
        }
    }

    /**
     * 一个文件对象，用来表示从String中获取到的Source，以下内容是按照JDK给出的例子写的
     */
    private static class StringInputBuffer extends SimpleJavaFileObject {

        private final String code;

        /**
         * @param name 此文件对象表示的编译单元的name
         * @param code 此文件对象表示的编译单元source的code
         */
        StringInputBuffer(String name, String code) {
            super(toURI(name), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
            return CharBuffer.wrap(code);
        }

        public Reader openReader() {
            return new StringReader(code);
        }
    }

    /**
     * 将Java字节码存储到classBytes映射中的文件对象
     */
    private class ClassOutputBuffer extends SimpleJavaFileObject {

        private String name;

        ClassOutputBuffer(String name) {
            super(toURI(name), Kind.CLASS);
            this.name = name;
        }

        @Override
        public OutputStream openOutputStream() {
            return new FilterOutputStream(new ByteArrayOutputStream()) {
                @Override
                public void close() throws IOException {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                    // 这里可能需要修改
                    classBytes.put(name, bos.toByteArray());
                }
            };
        }
    }

}
