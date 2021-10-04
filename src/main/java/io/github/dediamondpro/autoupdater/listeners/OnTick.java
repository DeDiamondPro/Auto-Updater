package io.github.dediamondpro.autoupdater.listeners;

import io.github.dediamondpro.autoupdater.updater.ModUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class OnTick {
    public static GuiScreen guiToOpen = null;
    private boolean sent = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (guiToOpen != null) {
            Minecraft.getMinecraft().displayGuiScreen(guiToOpen);
            guiToOpen = null;
        }

        if (ModUpdater.hasShutdownHook && !sent) {
            ChatComponentText component = new ChatComponentText(EnumChatFormatting.DARK_AQUA + "AutoUpdater > " + EnumChatFormatting.YELLOW + " Some mods " +
                    "have failed updating, there will be an attempt to update these" + " mods at shutdown. Affected mods:");
            for (String id : ModUpdater.tags.keySet()) {
                component.appendSibling(new ChatComponentText("\n" + EnumChatFormatting.YELLOW + "- " + id + " to " + ModUpdater.tags.get(id)));
            }
            Minecraft.getMinecraft().thePlayer.addChatMessage(component);
            sent = true;
        }
    }
}
