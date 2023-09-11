package com.clay.loader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * 动态加载器
 *
 * @author clay
 */
public class DynamicLoader {

    /**
     * 编译参数
     */
    private List<String> options = new ArrayList<>();

    /**
     * 添加编译参数
     *
     * @param key
     * @param value
     * @throws NullPointerException
     */
    public void addOption(String key, String value) throws NullPointerException {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("Option key is empty");
        }
        options.add(key);
        options.add(value);
    }

    /**
     * 通过Java文件名和其代码，编译得到字节码，返回类名及其对应类的字节码，封装于Map中，
     * 值得注意的是，平常类中就编译出来的字节码只有一个类，但是考虑到内部类的情况， 会出现很多个类名及其字节码，所以用Map封装方便
     *
     * @param javaName Java文件名，例如Student.java
     * @param javaCode Java源码
     * @return map
     */
    public Map<String, byte[]> compile(String javaName, String javaCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            JavaFileObject javaFileObject = manager.makeStringSource(javaName, javaCode);
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, options, null, Arrays.asList(javaFileObject));
            if (task.call()) {
                return manager.getClassBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 先根据类名在内存中查找是否已存在该类，若不存在则调用URLClassLoader.defineClass()方法加载该类
     * URLClassLoader的具体作用就是将Class文件加载到JVM虚拟机中
     */
    public static class MemoryClassLoader extends URLClassLoader {

        private Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

        public MemoryClassLoader(Map<String, byte[]> classBytes) {
            super(new URL[0], MemoryClassLoader.class.getClassLoader());
            this.classBytes.putAll(classBytes);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] buf = this.classBytes.get(name);
            if (buf == null) {
                return super.findClass(name);
            }
            this.classBytes.remove(name);
            return defineClass(name, buf, 0, buf.length);
        }
    }

}

