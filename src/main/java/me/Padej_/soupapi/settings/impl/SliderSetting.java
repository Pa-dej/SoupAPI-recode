package me.Padej_.soupapi.settings.impl;

import me.Padej_.soupapi.settings.Setting;

public class SliderSetting extends Setting<Float> {
    private final float min, max;

    public SliderSetting(String name, String description, float defaultValue, float min, float max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }
}

