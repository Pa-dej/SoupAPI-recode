package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.Render3D_Shapes;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.Palette;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;

public class Trajectories extends ConfigurableModule {

    private static final Color red = new Color(0x88ff0000, true);
    private static final Color green = new Color(0x8800ff00, true);
    private static final Color blue = new Color(0x880000ff, true);
    private static final Color yellow = new Color(0x88ffff00, true);
    private static final Color cyan = new Color(0x8800ffff, true);

    public static void onTick() {
    }

    public static void render(WorldRenderContext context) {
        if (mc.options.hudHidden) return;
        if (mc.player == null || mc.world == null || !mc.options.getPerspective().isFirstPerson() || !CONFIG.trajectoriesPreviewEnabled) return;
        Hand hand;
        float tickDelta = context.tickCounter().getTickDelta(true);

        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();

        if (mainHand.getItem() instanceof BowItem || mainHand.getItem() instanceof CrossbowItem || isThrowable(mainHand.getItem())) {
            hand = Hand.MAIN_HAND;
        } else if (offHand.getItem() instanceof BowItem || offHand.getItem() instanceof CrossbowItem || isThrowable(offHand.getItem())) {
            hand = Hand.OFF_HAND;
        } else return;

        boolean prev_bob = mc.options.getBobView().getValue();
        mc.options.getBobView().setValue(false);

        RegistryWrapper.Impl<Enchantment> enchantmentWrapper =
                mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> multishotEntry =
                enchantmentWrapper.getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, Enchantments.MULTISHOT.getValue()))
                        .orElse(null);

        if (multishotEntry != null && ((offHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(multishotEntry, offHand) > 0) ||
                        (mainHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(multishotEntry, mainHand) > 0))) {

            calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw() - 10, tickDelta);
            calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw(), tickDelta);
            calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw() + 10, tickDelta);

        } else
            calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw(), tickDelta);
        mc.options.getBobView().setValue(prev_bob);
    }

    private static boolean isThrowable(Item item) {
        return item instanceof EnderPearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private static float getDistance(Item item) {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private static float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) return 0.5f;
        if (item instanceof ExperienceBottleItem) return 0.59f;
        if (item instanceof TridentItem) return 2f;
        return 1.5f;
    }

    private static int getThrowPitch(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem)
            return 20;
        return 0;
    }

    private static void calcTrajectory(Item item, float yaw, float tickDelta) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        double x = Render2D.interpolate(player.prevX, player.getX(), tickDelta);
        double y = Render2D.interpolate(player.prevY, player.getY(), tickDelta);
        double z = Render2D.interpolate(player.prevZ, player.getZ(), tickDelta);

        y = y + player.getEyeHeight(player.getPose()) - 0.1000000014901161;

        if (item == player.getMainHandStack().getItem()) {
            x = x - MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
            z = z - MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;
        } else {
            x = x + MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
            z = z + MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;
        }

        final float maxDist = getDistance(item);
        double motionX = -MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(player.getPitch() / 180.0f * 3.1415927f) * maxDist;
        double motionY = -MathHelper.sin((player.getPitch() - getThrowPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(player.getPitch() / 180.0f * 3.1415927f) * maxDist;

        float power = player.getItemUseTime() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;

        if (power > 1.0f || power == 0) {
            power = 1.0f;
        }

        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = (item instanceof BowItem ? (power * 2.0f) : item instanceof CrossbowItem ? (2.2f) : 1.0f) * getThrowVelocity(item);

        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;
        if (!player.isOnGround())
            motionY += player.getVelocity().getY();

        Vec3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }

            if (item instanceof BowItem) motionY -= 0.05000000074505806;
            else if (player.getMainHandStack().getItem() instanceof CrossbowItem) motionY -= 0.05000000074505806;
            else motionY -= 0.03f;


            Vec3d pos = new Vec3d(x, y, z);

            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
            Color color = Render2D.injectAlpha(Palette.getColor(0.33f), 220).brighter();
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) {
                if (CONFIG.trajectoriesPreviewLandSideOutline) {
                    Render3D_Shapes.OUTLINE_SIDE_QUEUE.add(new Render3D_Shapes.OutlineSideAction(new Box(bhr.getBlockPos()), color, 2f, bhr.getSide())); // Land Side Outline
                }
                if (CONFIG.trajectoriesPreviewLandSideFill) {
                    Render3D_Shapes.FILLED_SIDE_QUEUE.add(new Render3D_Shapes.FillSideAction(new Box(bhr.getBlockPos()), color, bhr.getSide())); // Land Side Fill
                }
                break;
            }

            if (y <= -65) break;
            if (motionX == 0 && motionY == 0 && motionZ == 0) continue;

            Render3D_Shapes.drawLine(lastPos, pos, color);
        }
    }
}
