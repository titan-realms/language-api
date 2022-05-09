package net.titanrealms.api.languageapi.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import net.titanrealms.api.languageapi.models.language.Language;
import net.titanrealms.api.languageapi.models.server.ServerType;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Repository
public class LanguageRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageRepository.class);

    private final GHRepository repository;
    private final ObjectMapper objectMapper;
    private final Jedis jedis;

    private Map<ServerType, Map<Language, Map<String, String>>> languageKeys;
    private String currentCommitSha;

    @Autowired
    public LanguageRepository(GitHub apiClient, ObjectMapper objectMapper, Jedis jedis) throws IOException {
        this.repository = apiClient.getOrganization("titan-realms").getRepository("lang");
        this.objectMapper = objectMapper;
        this.jedis = jedis;
    }

    @Scheduled(initialDelay = 0, fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    protected void updateLanguages() throws IOException {
        GHCommit latestCommit = this.repository.listCommits().toList().get(0);
        String latestSha = latestCommit.getSHA1();
        if (latestSha.equals(this.currentCommitSha)) {
            LOGGER.info("No new changes found (current sha: {})", this.currentCommitSha);
        } else {
            LOGGER.info("Updating lang (current: {} previous: {})", latestSha, this.currentCommitSha);
            this.currentCommitSha = latestSha;

            Map<ServerType, Map<Language, Map<String, String>>> newValues = this.loadLanguages();

            for (Map.Entry<ServerType, Map<Language, Map<String, String>>> entry : newValues.entrySet()) {
                if (this.languageKeys != null && this.languageKeys.get(entry.getKey()).equals(entry.getValue()))
                    continue;
                // server type lang has changed
                LOGGER.info("Notifying " + entry.getKey() + " servers of language changes");
                this.jedis.publish("language_api_change_" + entry.getKey().toString(), this.objectMapper.writeValueAsString(entry.getValue()));
            }

            this.languageKeys = newValues;
        }
    }

    private Map<ServerType, Map<Language, Map<String, String>>> loadLanguages() throws IOException {
        Map<ServerType, Map<Language, Map<String, String>>> newLanguageKeys = new EnumMap<>(ServerType.class);
        File baseFile = this.getAndUnzipRepo(this.repository);
        for (File serverDir : baseFile.listFiles()) { // global, prison-cell, etc..
            if (serverDir.isDirectory()) {
                ServerType serverType = ServerType.valueOf(serverDir.getName().toUpperCase().replace('-', '_'));
                Map<Language, Map<String, String>> languageMap = new EnumMap<>(Language.class);
                for (File languageFile : serverDir.listFiles()) { // en_UK.conf, etc..
                    Language language = Language.fromLangFile(languageFile.getName());
                    Map<String, String> langStrings = this.parseLanguage(ConfigFactory.parseFile(languageFile));
                    languageMap.put(language, langStrings);
                }
                newLanguageKeys.put(serverType, languageMap);
            }
        }
        boolean deleteResult = baseFile.delete();

        if (!deleteResult)
            LOGGER.warn("Failed to delete temp language files for some reason");

        LOGGER.info("Loaded languages for {} server types:", newLanguageKeys.size());
        for (Map.Entry<ServerType, Map<Language, Map<String, String>>> entry : newLanguageKeys.entrySet()) {
            LOGGER.info("----- [ {} ] -----", entry.getKey());
            for (Map.Entry<Language, Map<String, String>> innerEntry : entry.getValue().entrySet()) {
                LOGGER.info("{}: {} strings", innerEntry.getKey().toString().toLowerCase(Locale.ROOT), innerEntry.getValue().size());
            }
        }
        LOGGER.info("-------------------");

        return newLanguageKeys;
    }

    private Map<String, String> parseLanguage(Config config) {
        Map<String, String> languageKeys = new HashMap<>();
        for (String key : config.root().keySet()) {
            ConfigValue configValue = config.getValue(key);
            if (configValue.valueType() == ConfigValueType.STRING) {
                String value = (String) configValue.unwrapped();
                languageKeys.put(key, value);
            } else if (configValue.valueType() == ConfigValueType.LIST) {
                List<String> value = (List<String>) configValue.unwrapped();
                languageKeys.put(key, String.join("<newLine>", value));
            } else {
                LOGGER.error("Mismatched language key? " + configValue.origin() + " with value " + configValue);
            }
        }
        return languageKeys;
    }

    private File getAndUnzipRepo(GHRepository repository) throws IOException {
        return repository.readZip(input -> {
            try (ZipInputStream zipInputStream = new ZipInputStream(input)) {
                File workDir = new File("tmp");
                this.createIfAbsent(workDir);
                String zipDirName = null;
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    File unpacked = new File(workDir, zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        unpacked.mkdirs();
                        if (zipDirName == null) {
                            zipDirName = zipEntry.getName();
                        }
                    } else {
                        unpacked.createNewFile();
                        try (FileChannel channel = new FileOutputStream(unpacked).getChannel()) {
                            byte[] bytes = new byte[2048];
                            int len;
                            long size = zipEntry.getSize();
                            while (size > 0 && (len = zipInputStream.read(bytes, 0, 2048)) > 0) {
                                ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, len);
                                channel.write(buffer);
                                size -= len;
                            }
                        }
                    }
                }
                if (zipDirName == null) {
                    throw new IllegalStateException("No directory name found. Was zip file empty?");
                }
                return workDir.toPath().resolve(zipDirName).toFile();
            }
        }, "main");
    }

    private void createIfAbsent(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @PreDestroy
    public void cleanup() throws IOException {
        Path tempPath = Path.of("tmp");
        Files.delete(tempPath);
    }

    public Map<ServerType, Map<Language, Map<String, String>>> getLanguageKeys() {
        return this.languageKeys;
    }
}
