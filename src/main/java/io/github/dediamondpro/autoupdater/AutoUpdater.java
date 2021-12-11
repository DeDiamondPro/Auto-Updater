package io.github.dediamondpro.autoupdater;

import io.github.dediamondpro.autoupdater.commands.SettingsCommand;
import io.github.dediamondpro.autoupdater.listeners.OnTick;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "autoupdater", name = "Auto Updater", version = "1.1.1")
public class AutoUpdater {
    @Mod.EventHandler
    public void onFMLInitialization(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new OnTick());

        ClientCommandHandler.instance.registerCommand(new SettingsCommand());
    }
}
