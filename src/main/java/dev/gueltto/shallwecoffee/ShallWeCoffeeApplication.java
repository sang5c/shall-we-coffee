package dev.gueltto.shallwecoffee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@ServletComponentScan
@SpringBootApplication
public class ShallWeCoffeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShallWeCoffeeApplication.class, args);
    }

}
