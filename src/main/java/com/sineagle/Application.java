package com.sineagle;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
// 扫描 mybatis mapper包路径
@MapperScan(basePackages = "com.sineagle.mapper")
@ComponentScan(basePackages = {"com.sineagle", "org.n3r.idworker"})
public class Application {
    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
