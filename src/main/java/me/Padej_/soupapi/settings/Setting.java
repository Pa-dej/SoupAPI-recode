package me.Padej_.soupapi.settings;

import java.util.Locale;

public class Setting<T> {
    protected String name;
    private final String description;
    private final T defaultValue;
    private T value;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public String getTranslationKey() {
        return "soupapi.setting." + name.toLowerCase(Locale.ROOT).replace(" ", "_");
    }

    public String getDescriptionKey() {
        return getTranslationKey() + ".description";
    }
}

