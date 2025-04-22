package me.Padej_.soupapi.interpolation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class EaseOutCirc {
    private float progress = 0f; // Прогресс анимации (0.0f до 1.0f)
    private float duration = 5f / 60f; // Длительность анимации в секундах (5 тиков = 5/60 сек при 60 FPS)
    private double value, dstValue;

    public EaseOutCirc(float durationInSeconds) {
        this.duration = durationInSeconds;
    }

    public EaseOutCirc() {
        this(5f / 60f); // По умолчанию 5 тиков = 5/60 сек
    }

    public void update(float delta) {
        progress += delta / (duration * 60f); // delta нормализовано для 60 FPS
        progress = MathHelper.clamp(progress, 0f, 1f);
    }

    public static double createAnimation(double value) {
        return Math.sqrt(1 - Math.pow(value - 1, 2));
    }

    public void setValue(double value) {
        if (value != this.dstValue) {
            this.progress = 0f; // Сбрасываем прогресс при смене значения
            this.value = dstValue;
            this.dstValue = value;
        }
    }

    public double getAnimationD() {
        double delta = dstValue - value;
        double animation = createAnimation(progress);
        return value + delta * animation;
    }

    public void reset() {
        this.progress = 0f;
    }
}
