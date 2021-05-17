package net.titanrealms.api.languageapi.config;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@ConfigurationProperties("services.github")
@Configuration
public class GitHubConfiguration {
    private String token;

    private GitHub client;

    // API has horrible documentation, no idea what "the io exception" is, so just gonna throw it.
    @PostConstruct
    private void createClient() throws IOException {
        this.client = new GitHubBuilder().withOAuthToken(this.token).build();
    }

    public GitHub getClient() {
        return this.client;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
