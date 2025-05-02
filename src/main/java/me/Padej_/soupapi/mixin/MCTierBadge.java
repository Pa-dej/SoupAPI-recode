package me.Padej_.soupapi.mixin;

import me.Padej_.soupapi.utils.CursorUtils;
import me.Padej_.soupapi.utils.Rectangle;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

@Mixin(InventoryScreen.class)
public class MCTierBadge extends Screen {
    private float x = 5;
    private float y = 5;
    private float vx = 2;
    private float vy = 0;

    private final float gravity = 0.5f;
    private final float bounce = 0.7f;
    private final float airResistance = 0.98f;
    private final float scale = 14;

    private boolean dragging = false;
    private float dragOffsetX;
    private float dragOffsetY;

    @Unique
    private long lastUpdateTime = System.currentTimeMillis();

    @Unique
    private final List<Rectangle> effectRects = new ArrayList<>();

    private static final File SAVE_FILE = new File("mctierbadge_pos.txt");

    public MCTierBadge(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        loadPosition();
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        updateEffectRects();

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        float frameTime = 1.0f / 60.0f;
        float normalizedDelta = deltaTime / frameTime;

        if (!dragging) {
            vy += gravity * normalizedDelta;
            x += vx * normalizedDelta;
            y += vy * normalizedDelta;
            vx *= (float) Math.pow(airResistance, normalizedDelta);
            vy *= (float) Math.pow(airResistance, normalizedDelta);

            int width = this.width;
            int height = this.height;

            if (x < 0) {
                x = 0;
                vx *= -bounce;
            } else if (x + scale > width) {
                x = width - scale;
                vx *= -bounce;
            }

            if (y < 0) {
                y = 0;
                vy *= -bounce;
            } else if (y + scale > height) {
                y = height - scale;
                vy *= -bounce;
            }

            int invWidth = 176;
            int invHeight = 166;
            int invX = (width - invWidth) / 2;
            int invY = (height - invHeight) / 2;

            bounceFromRect(invX, invY, invWidth, invHeight);

            for (Rectangle rect : effectRects) {
                bounceFromRect(rect.x, rect.y, rect.width, rect.height);
            }
        } else {
            x = (float) mouseX - dragOffsetX;
            y = (float) mouseY - dragOffsetY;
            vx = 0;
            vy = 0;
        }

        context.drawTexture(RenderLayer::getGuiTextured, TexturesManager.MC_TIERS_LOGO, (int) x, (int) y, 0, 0, (int) scale, (int) scale, 1268, 1153, 1268, 1153);

        // Смена курсора
        if (isInside(mouseX, mouseY)) {
            CursorUtils.setHandCursor();
        } else {
            CursorUtils.setDefaultCursor();
        }
    }

    private void updateEffectRects() {
        effectRects.clear();

        int i = (this.width - 176) / 2 + 176 + 2;
        int j = this.width - i;
        Collection<StatusEffectInstance> collection = client.player.getStatusEffects();

        if (!collection.isEmpty() && j >= 32) {
            boolean wide = j >= 120;
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            int yPos = (this.height - 166) / 2;
            for (StatusEffectInstance ignored : collection) {
                if (wide) {
                    effectRects.add(new Rectangle(i, yPos, 120, 32));
                } else {
                    effectRects.add(new Rectangle(i, yPos, 32, 32));
                }
                yPos += k;
            }
        }
    }

    private void bounceFromRect(int rectX, int rectY, int rectWidth, int rectHeight) {
        if (x + scale > rectX && x < rectX + rectWidth && y + scale > rectY && y < rectY + rectHeight) {
            float dxLeft = Math.abs(x + scale - rectX);
            float dxRight = Math.abs(x - (rectX + rectWidth));
            float dyTop = Math.abs(y + scale - rectY);
            float dyBottom = Math.abs(y - (rectY + rectHeight));

            float minDist = Math.min(Math.min(dxLeft, dxRight), Math.min(dyTop, dyBottom));

            if (minDist == dxLeft) {
                x = rectX - scale;
                vx *= -bounce;
            } else if (minDist == dxRight) {
                x = rectX + rectWidth;
                vx *= -bounce;
            } else if (minDist == dyTop) {
                y = rectY - scale;
                vy *= -bounce;
            } else if (minDist == dyBottom) {
                y = rectY + rectHeight;
                vy *= -bounce;
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && isInside((int) mouseX, (int) mouseY)) {
            if (!dragging) {
                dragging = true;
                dragOffsetX = (float) mouseX - x;
                dragOffsetY = (float) mouseY - y;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            vx = (float) (mouseX - x - dragOffsetX);
            vy = (float) (mouseY - y - dragOffsetY);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInside(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + scale && mouseY >= y && mouseY <= y + scale;
    }

    @Override
    public void close() {
        savePosition();
        super.close();
    }

    private void savePosition() {
        try {
            PrintWriter writer = new PrintWriter(SAVE_FILE);
            writer.println(x);
            writer.println(y);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPosition() {
        if (SAVE_FILE.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE));
                String xLine = reader.readLine();
                String yLine = reader.readLine();
                if (xLine != null && yLine != null) {
                    x = Float.parseFloat(xLine);
                    y = Float.parseFloat(yLine) + 3;
                    vx = 0;
                    vy = 0;
                }
                reader.close();
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
