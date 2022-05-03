package net.titanrealms.api.languageapi.services;

import net.titanrealms.api.languageapi.models.language.Language;
import net.titanrealms.api.languageapi.models.server.ServerType;
import net.titanrealms.api.languageapi.repositories.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Service
public class LanguageService {
    private final LanguageRepository languageRepository;

    @Autowired
    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @NonNull
    public Map<Language, Map<String, String>> getServerPack(@PathVariable ServerType serverType) {
        return this.languageRepository.getLanguageKeys().get(serverType);
    }
}
