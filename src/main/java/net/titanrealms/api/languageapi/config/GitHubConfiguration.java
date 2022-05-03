package net.titanrealms.api.languageapi.config;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@ConfigurationProperties("github")
public class GitHubConfiguration {
    private String token;

    // API has horrible documentation, no idea what "the io exception" is, so just gonna throw it.
    @Bean
    public GitHub createClient() throws IOException {
        return GitHub.connectUsingOAuth(this.token);
    }

    public void setToken(String token) {
        this.token = token;
    }
}
