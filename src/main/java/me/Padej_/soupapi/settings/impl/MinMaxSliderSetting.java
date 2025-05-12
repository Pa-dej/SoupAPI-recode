package me.Padej_.soupapi.settings.impl;

import me.Padej_.soupapi.settings.Setting;

public class MinMaxSliderSetting extends Setting<float[]> {
    private final float min, max;

    public MinMaxSliderSetting(String name, String description, float[] defaultValue, float min, float max) {
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

    public float getMinValue() {
        return getValue()[0];
    }

    public float getMaxValue() {
        return getValue()[1];
    }

    public void setMinValue(float value) {
        getValue()[0] = value;
    }

    public void setMaxValue(float value) {
        getValue()[1] = value;
    }
}

