package com.paiyucun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 拍遇存 — 后端启动类
 */
@SpringBootApplication
public class PaiYuCunApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaiYuCunApplication.class, args);
        System.out.println("""

                ============================================
                   📖 拍遇存后端服务启动成功！
                   API: http://localhost:8080
                ============================================
                """);
    }
}
