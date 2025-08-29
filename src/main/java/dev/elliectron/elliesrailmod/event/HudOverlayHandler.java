package dev.elliectron.elliesrailmod.event;

import dev.elliectron.elliesrailmod.ElliesRailImprovements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = ElliesRailImprovements.MODID, value = Dist.CLIENT)
public class HudOverlayHandler {

    // Cache the values received from server
    private static double currentBrakeTemp = MinecartControlsHandler.DEFAULT_BRAKE_TEMP;
    private static int currentSignalAspect = 0;

    @SubscribeEvent
    public static void onChatMessage(ClientChatReceivedEvent event) {
        String message = event.getMessage().getString();
        if (message.startsWith("HUD_DATA:")) {
            String[] parts = message.substring(9).split(":");
            if (parts.length >= 2) {
                try {
                    currentBrakeTemp = Double.parseDouble(parts[0]);
                    currentSignalAspect = Integer.parseInt(parts[1]);
                    event.setCanceled(true); // Don't show this message in chat
                } catch (NumberFormatException e) {
                    // Ignore malformed messages
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || !player.isPassenger()) return;

        if (!(player.getVehicle() instanceof AbstractMinecart minecart)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Use cached values instead of NBT
        double tempCelsius = currentBrakeTemp - 273;

        // Prepare text
        String brakeText = String.format("Brake Temp: %.0f°C", tempCelsius);
        String signalText = "Status: " + signalAspectToText(currentSignalAspect);

        // Calculate positions (lower center of screen, above hotbar)
        int brakeTextWidth = font.width(brakeText);
        int signalTextWidth = font.width(signalText);

        int brakeX = (screenWidth - brakeTextWidth) / 2;
        int signalX = (screenWidth - signalTextWidth) / 2;
        int baseY = screenHeight - 80; // Above hotbar

        // Draw text with shadow
        graphics.drawString(font, brakeText, brakeX, baseY, 0xFFFFFF, true);
        graphics.drawString(font, signalText, signalX, baseY + 12, 0xFFFFFF, true);
    }

    private static String signalAspectToText(int signalAspect) {
        if (signalAspect == -1) return "Brakes applied";
        if (signalAspect == -2) return "Emergency brakes applied";
        if (signalAspect == -3) return "Signal/ATP/ATO brake application";
        if (signalAspect == 0) return "Normal operation";
        if (signalAspect == 3) return "ATO-controlled acceleration";
        else return "Unknown";
    }
}