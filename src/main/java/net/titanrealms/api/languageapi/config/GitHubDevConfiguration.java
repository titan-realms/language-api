package net.titanrealms.api.languageapi.config;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@ConfigurationProperties("services.github")
@Profile("dev")
public class GitHubDevConfiguration {
    private String token;

    // API has horrible documentation, no idea what "the io exception" is, so just gonna throw it.
    @Bean
    @Profile("dev")
    public GitHub createClient() throws IOException {
        return new GitHubBuilder().withOAuthToken(this.token).build();
    }

    public void setToken(String token) {
        this.token = token;
    }
}
