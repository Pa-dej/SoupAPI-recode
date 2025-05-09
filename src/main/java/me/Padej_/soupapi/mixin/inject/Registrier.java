package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.font.FontRenderers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class Registrier {
    @Inject(method = "<init>", at = @At("TAIL"))
    void postWindowInit(RunArgs args, CallbackInfo ci) {
        try {
            FontRenderers.sf_bold = FontRenderers.create(16f, "sf_bold");
            FontRenderers.sf_bold_17 = FontRenderers.create(17f, "sf_bold");
            FontRenderers.sf_bold_mini = FontRenderers.create(14f, "sf_bold");
            FontRenderers.sf_medium = FontRenderers.create(16f, "sf_medium");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"))
    private void onChangeIcon(Window instance, ResourcePack resourcePack, Icons icons) throws IOException {
        // RenderSystem.assertInInitPhase();

        if (GLFW.glfwGetPlatform() == 393218) {
            MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
            return;
        }

        setWindowIcon(SoupAPI_Client.class.getResourceAsStream("/icon.png"), SoupAPI_Client.class.getResourceAsStream("/icon.png"));
    }

    private void setWindowIcon(InputStream img16x16, InputStream img32x32) throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GLFWImage.Buffer imageBuffer = GLFWImage.malloc(2, stack);
            List<InputStream> streams = List.of(img16x16, img32x32);
            List<ByteBuffer> pixelBuffers = new ArrayList<>();

            for (int i = 0; i < streams.size(); i++) {
                NativeImage image = NativeImage.read(streams.get(i));
                int width = image.getWidth();
                int height = image.getHeight();

                ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = image.getColorArgb(x, y);
                        buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                        buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                        buffer.put((byte) (pixel & 0xFF));         // B
                        buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
                    }
                }

                buffer.flip();

                imageBuffer.position(i);
                imageBuffer.width(width);
                imageBuffer.height(height);
                imageBuffer.pixels(buffer);

                pixelBuffers.add(buffer);
                image.close();
            }

            if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
                GLFW.glfwSetWindowIcon(MinecraftClient.getInstance().getWindow().getHandle(), imageBuffer);
            }

            pixelBuffers.forEach(MemoryUtil::memFree);
        }
    }
     */
}

