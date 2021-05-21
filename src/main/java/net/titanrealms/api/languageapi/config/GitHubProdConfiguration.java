package net.titanrealms.api.languageapi.config;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("prod")
public class GitHubProdConfiguration {

    @Bean
    @Profile("prod")
    public GitHub createClient() throws IOException {
        String token = System.getenv("GITHUB_TOKEN");
        return new GitHubBuilder().withOAuthToken(token).build();
    }
}
