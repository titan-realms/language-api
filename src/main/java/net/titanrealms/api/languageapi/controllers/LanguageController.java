package net.titanrealms.api.languageapi.controllers;

import lombok.RequiredArgsConstructor;
import net.titanrealms.api.languageapi.models.language.Language;
import net.titanrealms.api.languageapi.models.server.ServerType;
import net.titanrealms.api.languageapi.services.LanguageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class LanguageController {
    private final LanguageService languageService;

    @GetMapping("/")
    public Map<Language, Map<String, String>> getServerPack(ServerType serverType) {
        return this.languageService.getServerPack(serverType);
    }
}
