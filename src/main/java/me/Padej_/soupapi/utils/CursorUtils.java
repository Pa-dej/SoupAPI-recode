package me.Padej_.soupapi.utils;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class CursorUtils {
    private static final long window = MinecraftClient.getInstance().getWindow().getHandle();

    private static final long defaultCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
    private static final long handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
    private static final long resizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR);

    private static long currentCursor = -1;

    public static void setDefaultCursor() {
        if (currentCursor != defaultCursor) {
            GLFW.glfwSetCursor(window, defaultCursor);
            currentCursor = defaultCursor;
        }
    }

    public static void setHandCursor() {
        if (currentCursor != handCursor) {
            GLFW.glfwSetCursor(window, handCursor);
            currentCursor = handCursor;
        }
    }

    public static void setResizeCursor() {
        if (currentCursor != resizeCursor) {
            GLFW.glfwSetCursor(window, resizeCursor);
            currentCursor = resizeCursor;
        }
    }

    public static void destroy() {
        GLFW.glfwDestroyCursor(defaultCursor);
        GLFW.glfwDestroyCursor(handCursor);
        GLFW.glfwDestroyCursor(resizeCursor);
    }
}
