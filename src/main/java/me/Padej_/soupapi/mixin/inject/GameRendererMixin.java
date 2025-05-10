package me.Padej_.soupapi.mixin.inject;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.mixin.access.GameRendererAccessor;
import me.Padej_.soupapi.modules.AspectRatio;
import me.Padej_.soupapi.modules.TargetHud;
import me.Padej_.soupapi.render.TargetHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique
    private Color floatingTotemColor = null;
    @Unique
    private int lastTotemTimeLeft = -1;

    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    @Shadow
    private float viewDistance;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    public void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrixStack = new MatrixStack();
        RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        TargetHud.lastProjMat.set(RenderSystem.getProjectionMatrix());
        TargetHud.lastModMat.set(RenderSystem.getModelViewMatrix());
        TargetHud.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());

        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        if (!CONFIG.aspectRatioEnabled) return;
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.peek().getPositionMatrix().identity();
        float factor = CONFIG.aspectRatioUsePreset ? AspectRatio.getRatioByPreset() : CONFIG.aspectRatioFactor / 100f;
        if (zoom != 1.0f) {
            matrixStack.translate(zoomX, -zoomY, 0.0f);
            matrixStack.scale(zoom, zoom, 1.0f);
        }
        matrixStack.peek().getPositionMatrix().mul(
                new Matrix4f().setPerspective((float) (fovDegrees * (Math.PI / 180f)),
                        factor,
                        0.05f,
                        viewDistance * 4.0f)
        );
        cir.setReturnValue(matrixStack.peek().getPositionMatrix());
    }

    @Inject(
            method = "renderFloatingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;draw(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void applyShaderColor(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (!CONFIG.totemPopShaderEnabled) return;

        int currentTimeLeft = ((GameRendererAccessor) this).getFloatingItemTimeLeft();
        if (currentTimeLeft != lastTotemTimeLeft) {
            lastTotemTimeLeft = currentTimeLeft;

            // фиксируем цвет только один раз на весь показ
            if (currentTimeLeft >= 40) {
                floatingTotemColor = TargetHudRenderer.bottomLeft;
            }
        }

        if (floatingTotemColor != null) {
            RenderSystem.setShaderColor(
                    floatingTotemColor.getRed() / 255f,
                    floatingTotemColor.getGreen() / 255f,
                    floatingTotemColor.getBlue() / 255f,
                    CONFIG.totemShaderAlpha / 100f
            );
        }
    }

    @Inject(
            method = "renderFloatingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;draw(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void resetShaderColor(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (!CONFIG.totemPopShaderEnabled) return;
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Inject(
            method = "renderFloatingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void afterTranslateInject(DrawContext context, float tickDelta, CallbackInfo ci) {
        MatrixStack matrices = context.getMatrices();
        if (!CONFIG.totemOverwriteScaleEnable) return;
        float scale = CONFIG.totemOverwriteScale / 100f;
        matrices.scale(scale, scale, scale);
    }
}

