package net.titanrealms.api.languageapi.controllers;

import net.titanrealms.api.languageapi.models.language.Language;
import net.titanrealms.api.languageapi.models.server.ServerType;
import net.titanrealms.api.languageapi.services.LanguageService;
import net.titanrealms.lang.formatter.strings.LangString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
public class LanguageController {
    private final LanguageService languageService;

    @Autowired
    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping("/get")
    public Map<Language, Map<String, LangString>> getServerPack(ServerType serverType) {
        return this.languageService.getServerPack(serverType);
    }
}
