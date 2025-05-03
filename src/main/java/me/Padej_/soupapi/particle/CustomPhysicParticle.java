package me.Padej_.soupapi.particle;

import me.Padej_.soupapi.utils.Palette;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class CustomPhysicParticle extends AnimatedParticle {
    private static final float GRAVITY = 0.08f; // Гравитация, как у большинства объектов в Minecraft
    private static final float BOUNCE_FACTOR = 0.8f; // Коэффициент упругости при отскоке
    private static final float FRICTION = 0.99f; // Трение при движении по поверхности
    private static final float MIN_VELOCITY = 0.01f; // Минимальная скорость, ниже которой частица останавливается
    private static final int FADE_TICKS = 10; // Количество тиков для анимации исчезновения
    private final float initialScale; // Начальный масштаб частицы

    protected CustomPhysicParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z, spriteProvider, 0.0f);
        int minLiveTime = 60;
        int maxLiveTime = 100;
        this.maxAge = minLiveTime + this.random.nextInt(maxLiveTime - minLiveTime); // Время жизни частицы (3-5 секунд)
        this.setColor(0); // Установить цвет из палитры
        this.setAlpha(0.3f);
        this.initialScale = 0.5f; // Начальный масштаб
        this.scale = this.initialScale; // Установить начальный масштаб
    }

    @Override
    public void setColor(float red, float green, float blue) {
        Color c = Palette.getColor(0);
        super.setColor(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
    }

    @Override
    public void tick() {
        // Сохраняем предыдущую позицию для интерполяции
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        // Уменьшаем время жизни
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        // Анимация исчезновения за последние 10 тиков
        int ticksLeft = this.maxAge - this.age;
        if (ticksLeft <= FADE_TICKS) {
            float fadeProgress = (float) ticksLeft / FADE_TICKS; // От 1.0 (полный масштаб) до 0.0
            this.scale = this.initialScale * fadeProgress; // Линейное уменьшение масштаба
        }

        // Применяем гравитацию
        if (!this.onGround) {
            this.velocityY -= GRAVITY;
        }

        // Применяем трение к горизонтальной скорости, если на земле
        if (this.onGround) {
            this.velocityX *= FRICTION;
            this.velocityZ *= FRICTION;
        }

        // Проверяем, достаточно ли скорости для продолжения движения
        if (Math.abs(this.velocityX) < MIN_VELOCITY && Math.abs(this.velocityZ) < MIN_VELOCITY && this.onGround) {
            this.velocityX = 0;
            this.velocityZ = 0;
        }

        // Двигаем частицу
        this.move(this.velocityX, this.velocityY, this.velocityZ);

        // Обновляем состояние onGround
        this.onGround = this.y == this.prevPosY && this.velocityY <= 0;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        // Проверяем столкновения с блоками
        Box particleBox = new Box(this.x, this.y, this.z, this.x, this.y, this.z).expand(0.1);
        Box moveBox = particleBox.offset(dx, dy, dz);
        BlockPos minPos = new BlockPos(MathHelper.floor(moveBox.minX), MathHelper.floor(moveBox.minY), MathHelper.floor(moveBox.minZ));
        BlockPos maxPos = new BlockPos(MathHelper.floor(moveBox.maxX), MathHelper.floor(moveBox.maxY), MathHelper.floor(moveBox.maxZ));

        // Проверяем столкновения по осям
        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            BlockState state = this.world.getBlockState(pos);
            if (!state.isAir()) {
                var collisionShape = state.getCollisionShape(this.world, pos);
                if (!collisionShape.isEmpty()) { // Проверяем, что форма не пустая
                    Box blockBox = collisionShape.getBoundingBox().offset(pos);
                    if (blockBox.intersects(moveBox)) {
                        // Определяем, с какой стороны произошло столкновение
                        Vec3d normal = calculateCollisionNormal(dx, dy, dz, blockBox, moveBox);
                        if (normal != null) {
                            // Обрабатываем отскок
                            if (Math.abs(normal.x) > 0) {
                                this.velocityX = -this.velocityX * BOUNCE_FACTOR;
                                dx = 0; // Останавливаем движение по X
                            }
                            if (Math.abs(normal.y) > 0) {
                                this.velocityY = -this.velocityY * BOUNCE_FACTOR;
                                dy = 0; // Останавливаем движение по Y
                                if (normal.y > 0) {
                                    this.onGround = true; // Частица на земле
                                }
                            }
                            if (Math.abs(normal.z) > 0) {
                                this.velocityZ = -this.velocityZ * BOUNCE_FACTOR;
                                dz = 0; // Останавливаем движение по Z
                            }
                        }
                    }
                }
            }
        }

        // Обновляем позицию
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    // Вычисляет нормаль столкновения для определения стороны блока
    private Vec3d calculateCollisionNormal(double dx, double dy, double dz, Box blockBox, Box moveBox) {
        double deltaX = dx > 0 ? moveBox.maxX - blockBox.minX : blockBox.maxX - moveBox.minX;
        double deltaY = dy > 0 ? moveBox.maxY - blockBox.minY : blockBox.maxY - moveBox.minY;
        double deltaZ = dz > 0 ? moveBox.maxZ - blockBox.minZ : blockBox.maxZ - moveBox.minZ;

        // Находим минимальное пересечение
        double minDelta = Math.min(Math.min(Math.abs(deltaX), Math.abs(deltaY)), Math.abs(deltaZ));

        if (MathHelper.approximatelyEquals(minDelta, Math.abs(deltaX))) {
            return new Vec3d(dx > 0 ? -1 : 1, 0, 0);
        } else if (MathHelper.approximatelyEquals(minDelta, Math.abs(deltaY))) {
            return new Vec3d(0, dy > 0 ? -1 : 1, 0);
        } else if (MathHelper.approximatelyEquals(minDelta, Math.abs(deltaZ))) {
            return new Vec3d(0, 0, dz > 0 ? -1 : 1);
        }
        return null;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }
}
