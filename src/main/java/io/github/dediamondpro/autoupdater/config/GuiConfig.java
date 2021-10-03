package io.github.dediamondpro.autoupdater.config;

import io.github.dediamondpro.autoupdater.data.ModData;
import io.github.dediamondpro.autoupdater.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiConfig extends GuiScreen {

    private boolean repeatKeys;
    private List<GuiTextField> textFields = new ArrayList<>();

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        if(textFields.size() != 0)
            return;
        repeatKeys = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(true);
        for (int i = 0; i < Config.modData.size(); i++) {
            ModData mod = (ModData) Config.modData.values().toArray()[i];
            int y = i * 50 + 10;
            textFields.add(new GuiTextField(i, Minecraft.getMinecraft().fontRendererObj, 100, y - 5, 200, 20));
            if (mod.url != null)
                textFields.get(i).setText(mod.url);
            textFields.get(i).setMaxStringLength(1000);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, this.width, this.height, new Color(10, 10, 10).getRGB());

        for (int i = 0; i < Config.modData.size(); i++) {
            ModData mod = (ModData) Config.modData.values().toArray()[i];
            int y = i * 50 + 10;

            Gui.drawRect(5, y - 7, this.width - 5, y - 8, new Color(255, 255, 255).getRGB());
            Gui.drawRect(5, y + 43, this.width - 5, y + 42, new Color(255, 255, 255).getRGB());
            Gui.drawRect(5, y - 7, 6, y + 43, new Color(255, 255, 255).getRGB());
            Gui.drawRect(this.width - 5, y - 7, this.width - 4, y + 43, new Color(255, 255, 255).getRGB());

            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Url:", 80, y, new Color(255, 255, 255).getRGB());
            textFields.get(i).drawTextBox();

            TextUtils.drawTextMaxLength(mod.name, 10, y, new Color(255, 255, 255).getRGB(), true, 75);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (GuiTextField textField : textFields) {
            textField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean closable = true;
        for (GuiTextField textField : textFields) {
            if (textField.isFocused()) {
                textField.textboxKeyTyped(typedChar, keyCode);
                closable = false;
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
