package io.github.dediamondpro.autoupdater.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RenderUtils {
    public static void renderImage(ResourceLocation image, int x, int y, int width, int height) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }
}
