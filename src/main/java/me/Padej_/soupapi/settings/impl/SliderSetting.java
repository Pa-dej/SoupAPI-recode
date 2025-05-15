package me.Padej_.soupapi.settings.impl;

import me.Padej_.soupapi.settings.Setting;

public class SliderSetting extends Setting<Float> {
    private final float min, max, step;

    public SliderSetting(String name, String description, float defaultValue, float min, float max, float step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public float getMin() { return min; }
    public float getMax() { return max; }
    public float getStep() { return step; }
}


