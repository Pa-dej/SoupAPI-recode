package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.main.SoupAPI_Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ClientConnection.class)
public class TestMixin {
    @Unique
    private static boolean alreadyDetected = false;
    @Unique
    private static final List<Integer> actionNumbers = new ArrayList<>();
    @Unique
    private static boolean isDetecting = false;
    @Unique
    private static final boolean debug = false;
    @Unique
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof GameJoinS2CPacket) {
            reset();
            isDetecting = true;
            if (debug) logMessage("Received GameJoinS2CPacket, starting anti-cheat detection");
        } else if (packet instanceof CommonPingS2CPacket pingPacket) {
            if (!isDetecting) {
                isDetecting = true;
                if (debug) logMessage("Auto-starting detection on CommonPingS2CPacket");
            }
            if (debug) logMessage("Received CommonPingS2CPacket, processing transaction ID: " + pingPacket.getParameter());
            handleTransaction(pingPacket.getParameter());
        }
    }

    private static void reset() {
        actionNumbers.clear();
        isDetecting = false;
        alreadyDetected = false;
        if (debug) logMessage("Anti-cheat detection reset");
    }

    private static void handleTransaction(int action) {
        actionNumbers.add(action);
        if (debug) logMessage("Added transaction ID: " + action + ", Total IDs: " + actionNumbers.size());
        if (actionNumbers.size() >= 5) {
            if (debug) logMessage("Collected 5+ transaction IDs, analyzing...");
            analyzeActionNumbers();
        }
    }

    private static void analyzeActionNumbers() {
        List<Integer> diffs = new ArrayList<>();
        for (int i = 1; i < actionNumbers.size(); i++) {
            diffs.add(actionNumbers.get(i) - actionNumbers.get(i - 1));
        }

        int first = actionNumbers.getFirst();
        String detectedAntiCheat = null;

        String serverAddress = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "";
        if (debug) logMessage("Server address: " + (serverAddress != null ? serverAddress : "null"));

        if (serverAddress != null && serverAddress.equalsIgnoreCase("hypixel.net")) {
            detectedAntiCheat = "Watchdog";
        } else if (diffs.stream().allMatch(diff -> diff.equals(diffs.getFirst()))) {
            int diff = diffs.getFirst();
            if (diff == 1) {
                if (first >= -23772 && first <= -23762) {
                    detectedAntiCheat = "Vulcan";
                } else if ((first >= 95 && first <= 105) || (first >= -20005 && first <= -19995)) {
                    detectedAntiCheat = "Matrix";
                } else if (first >= -32773 && first <= -32762) {
                    detectedAntiCheat = "Grizzly";
                } else {
                    detectedAntiCheat = "Verus";
                }
            } else if (diff == -1) {
                if (first >= -8287 && first <= -8280) {
                    detectedAntiCheat = "Errata";
                } else if (first < -3000) {
                    detectedAntiCheat = "Intave";
                } else if (first >= -5 && first <= 0) {
                    detectedAntiCheat = "Grim";
                } else if (first >= -3000 && first <= -2995) {
                    detectedAntiCheat = "Karhu";
                } else {
                    detectedAntiCheat = "Polar";
                }
            }
        } else if (actionNumbers.get(0).equals(actionNumbers.get(1))) {
            boolean verusPattern = true;
            for (int i = 3; i < actionNumbers.size(); i++) {
                if (actionNumbers.get(i) - actionNumbers.get(i - 1) != 1) {
                    verusPattern = false;
                    break;
                }
            }
            if (verusPattern) {
                detectedAntiCheat = "Verus";
            }
        } else if (diffs.size() >= 3 && diffs.get(0) >= 100 && diffs.get(1) == -1 && diffs.subList(2, diffs.size()).stream().allMatch(diff -> diff == -1)) {
            detectedAntiCheat = "Polar";
        } else if (first < -3000 && actionNumbers.contains(0)) {
            detectedAntiCheat = "Intave";
        } else if (actionNumbers.size() >= 5 && actionNumbers.get(0) == -30767 && actionNumbers.get(1) == -30766 && actionNumbers.get(2) == -25767) {
            boolean oldVulcanPattern = true;
            for (int i = 4; i < actionNumbers.size(); i++) {
                if (actionNumbers.get(i) - actionNumbers.get(i - 1) != 1) {
                    oldVulcanPattern = false;
                    break;
                }
            }
            if (oldVulcanPattern) {
                detectedAntiCheat = "Old Vulcan";
            }
        }

        if (!alreadyDetected) {
            if (detectedAntiCheat != null) {
//                logMessage("Detected anti-cheat: " + detectedAntiCheat);
                SoupAPI_Main.ac = detectedAntiCheat;
                alreadyDetected = true;
            } else if (debug) {
                logMessage("Unknown anti-cheat");
                logMessage("Action numbers: " + listToString(actionNumbers));
                logMessage("Differences: " + listToString(diffs));
                alreadyDetected = true;
            }
        }

        reset();
    }

    private static String listToString(List<Integer> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    private static void logMessage(String message) {
        if (!debug && !message.startsWith("Detected")) return;
        if (mc.player != null) {
            mc.player.sendMessage(Text.of("[AntiCheatDetector] " + message), false);
        } else {
            System.out.println("[AntiCheatDetector] " + message);
        }
    }
}

