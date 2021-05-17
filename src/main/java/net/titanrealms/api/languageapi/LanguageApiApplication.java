package net.titanrealms.api.languageapi;

import lombok.SneakyThrows;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LanguageApiApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(LanguageApiApplication.class, args);
    }
}
