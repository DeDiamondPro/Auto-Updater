package io.github.dediamondpro.autoupdater.config;

import io.github.dediamondpro.autoupdater.data.ModData;
import io.github.dediamondpro.autoupdater.updater.ModUpdater;
import io.github.dediamondpro.autoupdater.updater.SkyClientUpdater;
import io.github.dediamondpro.autoupdater.utils.RenderUtils;
import io.github.dediamondpro.autoupdater.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiConfig extends GuiScreen {

    private final ResourceLocation cross = new ResourceLocation("autoupdater", "cross.png");
    private final ResourceLocation finch = new ResourceLocation("autoupdater", "finch.png");

    private boolean repeatKeys;
    private final List<GuiTextField> textFields = new ArrayList<>();
    private int scrollOffset = 0;

    private final FontRenderer ft = Minecraft.getMinecraft().fontRendererObj;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        if (textFields.size() != 0) {
            for (GuiTextField textField : textFields) {
                textField.width = Math.min(this.width - 253, 300);
            }
        } else {
            repeatKeys = Keyboard.areRepeatEventsEnabled();
            Keyboard.enableRepeatEvents(true);
            for (int i = 0; i < Config.modData.size(); i++) {
                ModData mod = (ModData) Config.modData.values().toArray()[i];
                int y = i * 50 + 10;
                textFields.add(new GuiTextField(i, ft, 245, y - 5, Math.min(this.width - 253, 300), 20));
                textFields.get(i).setMaxStringLength(1000);
                if (mod.url != null)
                    textFields.get(i).setText(mod.url);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, this.width, this.height, new Color(10, 10, 10).getRGB());

        for (int i = 0; i < Config.modData.size(); i++) {
            ModData mod = (ModData) Config.modData.values().toArray()[i];
            int y = i * 50 + 10 + scrollOffset;

            Gui.drawRect(5, y - 7, this.width - 5, y - 8, new Color(220, 220, 220).getRGB());
            Gui.drawRect(5, y + 43, this.width - 5, y + 42, new Color(220, 220, 220).getRGB());
            Gui.drawRect(5, y - 7, 6, y + 43, new Color(220, 220, 220).getRGB());
            Gui.drawRect(this.width - 5, y - 8, this.width - 4, y + 43, new Color(220, 220, 220).getRGB());

            TextUtils.drawTextMaxLength(mod.name, 10, y + 12, new Color(220, 220, 220).getRGB(), true, 75);

            ft.drawStringWithShadow("Automatically Update:", 90, y, new Color(220, 220, 220).getRGB());
            if (mod.update)
                RenderUtils.renderImage(finch, ft.getStringWidth("Automatically Update:") + 91, y - 4, 16, 16);
            else
                RenderUtils.renderImage(cross, ft.getStringWidth("Automatically Update:") + 91, y - 4, 16, 16);

            ft.drawStringWithShadow("Use Pre-Release:", 90, y + 25, new Color(220, 220, 220).getRGB());
            if (mod.usePre)
                RenderUtils.renderImage(finch, ft.getStringWidth("Automatically Update:") + 91, y + 21, 16, 16);
            else
                RenderUtils.renderImage(cross, ft.getStringWidth("Automatically Update:") + 91, y + 21, 16, 16);

            ft.drawStringWithShadow("Url:", 225, y, new Color(220, 220, 220).getRGB());
            textFields.get(i).drawTextBox();
            if (!ModUpdater.githubPattern.matcher(textFields.get(i).getText()).matches() && (!SkyClientUpdater.modsList.containsKey(mod.id) || !mod.useSkyClient))
                ft.drawStringWithShadow(EnumChatFormatting.RED + "Invalid GitHub url",
                        245 + Math.min(this.width - 253, 300) - ft.getStringWidth("Invalid GitHub url")
                        , y + 25, new Color(220, 220, 220).getRGB());

            if (SkyClientUpdater.modsList.containsKey(mod.id)) {
                ft.drawStringWithShadow("Use SkyClient repo:", 225, y + 25, new Color(220, 220, 220).getRGB());
                if (mod.useSkyClient)
                    RenderUtils.renderImage(finch, ft.getStringWidth("Use SkyClient repo:") + 226, y + 21, 16, 16);
                else
                    RenderUtils.renderImage(cross, ft.getStringWidth("Use SkyClient repo:") + 226, y + 21, 16, 16);
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (Mouse.getEventButton() == -1 && Mouse.getEventDWheel() != 0 && (scrollOffset + Mouse.getEventDWheel() / 5 <= 0 && Mouse.getEventDWheel() > 0 ||
                scrollOffset + Config.modData.size() * 50 > this.height && Mouse.getEventDWheel() < 0)) {
            scrollOffset += Mouse.getEventDWheel() / 5;
            for (GuiTextField textField : textFields) {
                textField.yPosition += Mouse.getEventDWheel() / 5;
            }
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= ft.getStringWidth("Automatically Update:") + 91 && mouseX <= ft.getStringWidth("Automatically Update:") + 107) {
            for (int i = 0; i < Config.modData.size(); i++) {
                String modid = (String) Config.modData.keySet().toArray()[i];
                int y = i * 50 + 10 + scrollOffset;
                if (mouseY >= y - 4 && mouseY <= y + 12) {
                    Config.modData.get(modid).update = !Config.modData.get(modid).update;
                } else if (mouseY >= y + 21 && mouseY <= y + 37) {
                    Config.modData.get(modid).usePre = !Config.modData.get(modid).usePre;
                }
            }
        } else if (mouseX >= ft.getStringWidth("Use SkyClient repo:") + 226 && mouseX <= ft.getStringWidth("Use SkyClient repo:") + 242) {
            for (int i = 0; i < Config.modData.size(); i++) {
                String modid = (String) Config.modData.keySet().toArray()[i];
                int y = i * 50 + 10 + scrollOffset;
                if (mouseY >= y + 21 && mouseY <= y + 37) {
                    Config.modData.get(modid).useSkyClient = !Config.modData.get(modid).useSkyClient;
                }
            }
        } else {
            for (GuiTextField textField : textFields) {
                textField.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean closable = true;
        for (GuiTextField textField : textFields) {
            if (textField.isFocused()) {
                textField.textboxKeyTyped(typedChar, keyCode);
                closable = false;
                break;
            }
        }
        if (closable)
            super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(repeatKeys);
        for (int i = 0; i < Config.modData.size(); i++) {
            String mod = (String) Config.modData.keySet().toArray()[i];
            Config.modData.get(mod).url = textFields.get(i).getText();
        }
        Config.save();
    }
}
