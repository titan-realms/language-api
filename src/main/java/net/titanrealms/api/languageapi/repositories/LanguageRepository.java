package net.titanrealms.api.languageapi.repositories;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import net.titanrealms.api.languageapi.config.GitHubConfiguration;
import net.titanrealms.api.languageapi.models.language.Language;
import net.titanrealms.api.languageapi.models.server.ServerType;
import net.titanrealms.lang.formatter.strings.LangString;
import net.titanrealms.lang.formatter.strings.MultiLineLangString;
import net.titanrealms.lang.formatter.strings.SingleLineLangString;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Repository
public class LanguageRepository {
    private Map<ServerType, Map<Language, Map<String, LangString>>> languageKeys;
    private final GHRepository repository;

    private final Logger logger = LoggerFactory.getLogger(LanguageRepository.class);

    @Autowired
    public LanguageRepository(GitHubConfiguration gitHubConfiguration) throws IOException {
        this.repository = gitHubConfiguration.getClient().getOrganization("titan-realms").getRepository("lang");
    }

    @Bean
    private Class<Map<Language, Map<String, String>>> getLanguageClazz() {
        Map<Language, Map<String, String>> map = new HashMap<>();
        return (Class<Map<Language, Map<String, String>>>) map.getClass();
    }

    public Map<ServerType, Map<Language, Map<String, LangString>>> getLanguageKeys() {
        return this.languageKeys;
    }

    @PostConstruct
    private void loadLanguages() throws IOException {
        Map<ServerType, Map<Language, Map<String, LangString>>> newLanguageKeys = new EnumMap<>(ServerType.class);
        File baseFile = this.getZipFiles(this.repository);
        for (File serverDir : baseFile.listFiles()) { // global, prison-cell, etc..
            if (serverDir.isDirectory()) {
                ServerType serverType = ServerType.valueOf(serverDir.getName().toUpperCase().replace('-', '_'));
                Map<Language, Map<String, LangString>> languageMap = new EnumMap<>(Language.class);
                for (File languageFile : serverDir.listFiles()) { // en_UK.conf, etc..
                    Language language = Language.fromLangFile(languageFile.getName());
                    Map<String, LangString> langStrings = this.parseLanguage(ConfigFactory.parseFile(languageFile));
                    languageMap.put(language, langStrings);
                }
                newLanguageKeys.put(serverType, languageMap);
            }
        }
        this.languageKeys = newLanguageKeys;
        baseFile.delete();

        System.out.println(this.languageKeys);
    }

    private Map<String, LangString> parseLanguage(Config config) {
        Map<String, LangString> languageKeys = new HashMap<>();
        for (String key : config.root().keySet()) {
            ConfigValue configValue = config.getValue(key);
            if (configValue.valueType() == ConfigValueType.STRING) {
                String value = (String) configValue.unwrapped();
                languageKeys.put(key, new SingleLineLangString(key, value));
            } else if (configValue.valueType() == ConfigValueType.LIST) {
                List<String> value = (List<String>) configValue.unwrapped();
                languageKeys.put(key, new MultiLineLangString(key, value));
            } else {
                this.logger.error("Mismatched language key? " + configValue.origin() + " with value " + configValue);
            }
        }
        return languageKeys;
    }

    private File getZipFiles(GHRepository repository) throws IOException {
        return repository.readZip(input -> {
            try (ZipInputStream zipInputStream = new ZipInputStream(input)) {
                File workDir = new File("/tmp/private-api");
                this.createIfAbsent(workDir);
                String zipDirName = null;
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    File unpacked = new File(workDir, zipEntry.getName());
                    System.out.println(unpacked);
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
                return workDir.toPath().resolve(zipDirName).toFile();
            }
        }, "main");
    }

    private void createIfAbsent(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
