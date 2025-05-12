package me.Padej_.soupapi.settings.impl;

import me.Padej_.soupapi.settings.Setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }
}

