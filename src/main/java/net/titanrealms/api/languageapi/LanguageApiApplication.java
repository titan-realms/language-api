package net.titanrealms.api.languageapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LanguageApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LanguageApiApplication.class, args);
    }
}
