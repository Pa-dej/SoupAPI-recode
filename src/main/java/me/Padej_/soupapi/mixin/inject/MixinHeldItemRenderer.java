package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.modules.SwingHand;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Shadow
    protected abstract void applyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player);

    @Shadow protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private void modifyPosAndRot(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!CONFIG.swingHandEnabled) return;
        if (item.isEmpty()) return;

        boolean isMainHand = hand == Hand.MAIN_HAND;
        Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        matrices.push();

        boolean usingItem = player.isUsingItem() && player.getActiveHand() == hand;
        boolean isVanillaAction = false;

        if (usingItem) {
            switch (item.getUseAction()) {
                case EAT, DRINK -> {
                    this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item, player);
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    isVanillaAction = true;
                }
                case BOW -> {
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    applyBowTransform(matrices, player, item, tickDelta, arm);
                    isVanillaAction = true;
                }
                case SPEAR -> {
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    applySpearTransform(matrices, player, item, tickDelta, arm);
                    isVanillaAction = true;
                }
                case CROSSBOW -> {
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    applyCrossbowTransform(matrices, player, item, tickDelta, arm);
                    isVanillaAction = true;
                }
            }
        }

        if (!isVanillaAction) {
            customSwing(player, hand, swingProgress, matrices);
        }

        matrices.scale(SwingHand.getScale(), SwingHand.getScale(), SwingHand.getScale());

        HeldItemRenderer heldItemRenderer = (HeldItemRenderer) (Object) this;
        heldItemRenderer.renderItem(player,
                item, arm == Arm.RIGHT ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                arm == Arm.LEFT, matrices, vertexConsumers, light);
        matrices.pop();
        ci.cancel();
    }

    @Unique
    private void customSwing(AbstractClientPlayerEntity player, Hand hand, float swingProgress, MatrixStack matrices) {
        boolean isMainHand = hand == Hand.MAIN_HAND;
        Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        float ad = arm == Arm.RIGHT ? 1 : -1;

        float speed = SwingHand.getSpeed() / 100f;
        float swingAmount = MathHelper.sin(MathHelper.sqrt(Math.min(swingProgress / speed, 1.0f)) * (float) Math.PI);

        matrices.translate(SwingHand.getxPos() * ad, SwingHand.getyPos(), SwingHand.getzPos());

        float swingRotationX = MathHelper.lerp(swingAmount, 0.0F, SwingHand.getxSwingRot());
        float swingRotationY = MathHelper.lerp(swingAmount, 0.0F, SwingHand.getySwingRot() * ad);
        float swingRotationZ = MathHelper.lerp(swingAmount, 0.0F, SwingHand.getzSwingRot());

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(SwingHand.getRotX() + swingRotationX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(SwingHand.getRotY() + swingRotationY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(SwingHand.getRotZ() + swingRotationZ));
    }

    @Unique
    private void applyBowTransform(MatrixStack matrices, AbstractClientPlayerEntity player, ItemStack item, float tickDelta, Arm arm) {
        int dir = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float)dir * -0.2785682F, 0.18344387F, 0.15731531F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)dir * 35.3F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)dir * -9.785F));
        float m = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
        float f = m / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) f = 1.0F;
        if (f > 0.1F) {
            float g = MathHelper.sin((m - 0.1F) * 1.3F);
            float h = f - 0.1F;
            float j = g * h;
            matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
        }
        matrices.translate(f * 0.0F, f * 0.0F, f * 0.04F);
        matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)dir * 45.0F));
    }

    @Unique
    private void applySpearTransform(MatrixStack matrices, AbstractClientPlayerEntity player, ItemStack item, float tickDelta, Arm arm) {
        int dir = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float)dir * -0.5F, 0.7F, 0.1F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)dir * 35.3F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)dir * -9.785F));
        float m = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
        float f = m / 10.0F;
        if (f > 1.0F) f = 1.0F;
        if (f > 0.1F) {
            float g = MathHelper.sin((m - 0.1F) * 1.3F);
            float h = f - 0.1F;
            float j = g * h;
            matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
        }
        matrices.translate(0.0F, 0.0F, f * 0.2F);
        matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)dir * 45.0F));
    }

    @Unique
    private void applyCrossbowTransform(MatrixStack matrices, AbstractClientPlayerEntity player, ItemStack item, float tickDelta, Arm arm) {
        int dir = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float)dir * -0.4785682F, -0.094387F, 0.05731531F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)dir * 65.3F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)dir * -9.785F));

        float useTime = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
        float pullProgress = useTime / (float) CrossbowItem.getPullTime(item, player);
        if (pullProgress > 1.0F) pullProgress = 1.0F;

        if (pullProgress > 0.1F) {
            float sin = MathHelper.sin((useTime - 0.1F) * 1.3F);
            float delta = pullProgress - 0.1F;
            float offset = sin * delta;
            matrices.translate(offset * 0.0F, offset * 0.004F, offset * 0.0F);
        }

        matrices.translate(0.0F, 0.0F, pullProgress * 0.04F);
        matrices.scale(1.0F, 1.0F, 1.0F + pullProgress * 0.2F);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)dir * 45.0F));
    }

}


