package io.github.dediamondpro.autoupdater.commands;

import io.github.dediamondpro.autoupdater.config.GuiConfig;
import io.github.dediamondpro.autoupdater.listeners.OnTick;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class SettingsCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "autoupdater";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "autoupdater";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        OnTick.guiToOpen = new GuiConfig();
    }
}
