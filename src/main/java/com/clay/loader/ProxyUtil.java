package com.clay.loader;

import com.clay.domain.Store;
import com.clay.domain.Supermarket;
import org.springframework.util.Assert;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * @author clay
 */
public class ProxyUtil {

    /**
     * 获取Java代码
     *
     * @return
     */
    public String getJavaCode() {
        String rt = "\r\n";
        // 这里定义的Java类代码里，首行不要带包名，否则容易出现编译失败的问题
        String code = "import com.clay.domain.Store;" + rt
                + "public class Dealer implements Store" + rt
                + "{" + rt
                + "private Store s;" + rt
                + "public Dealer(Store s)" + rt + " {" + "  this.s = s;" + rt
                + " }" + rt
                + "@Override" + rt
                + "public void sell()" + " {" + rt
                + "System.out.println(\"invoke dealer sell method\");" + rt
                + "s.sell();" + rt
                + " }" + rt
                + "}";
        return code;
    }

    /**
     * 动态编译
     *
     * @throws Exception
     */
    public void handle() throws Exception {
        String javaName = "Dealer.java";

        // 对Java代码进行编译，并将生成Class文件存放在Map中
        DynamicLoader dynamicLoader = new DynamicLoader();
        Map<String, byte[]> bytecode = dynamicLoader.compile(javaName, getJavaCode());

        // 加载字节码到虚拟机中
        DynamicLoader.MemoryClassLoader classLoader = new DynamicLoader.MemoryClassLoader(bytecode);
        Class<?> clazz = classLoader.loadClass("Dealer");
        Assert.notNull(clazz, "");

        // 通过反射进行调用
        Constructor constructor = clazz.getConstructor(Store.class);
        Store store = (Store) constructor.newInstance(new Supermarket());
        store.sell();
    }

}
