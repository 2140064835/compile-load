package com.clay;

import com.clay.loader.ProxyUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author clay
 */
@SpringBootApplication
public class ProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);

        try {
            ProxyUtil util = new ProxyUtil();
            util.handle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}