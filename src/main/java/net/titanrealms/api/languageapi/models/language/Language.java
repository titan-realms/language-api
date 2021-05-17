package net.titanrealms.api.languageapi.models.language;

public enum Language {
    EN_UK,
    EN_US;

    public static Language fromLangFile(String fileName) {
        return Language.valueOf(fileName.toUpperCase().substring(0, fileName.length() - 5));
    }
}
