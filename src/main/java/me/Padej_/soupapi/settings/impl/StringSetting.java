package me.Padej_.soupapi.settings.impl;

import me.Padej_.soupapi.settings.Setting;

public class StringSetting extends Setting<String> {
    public StringSetting(String name, String description, String defaultValue) {
        super(name, description, defaultValue);
    }
}

