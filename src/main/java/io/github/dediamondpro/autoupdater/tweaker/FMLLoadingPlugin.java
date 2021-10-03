package io.github.dediamondpro.autoupdater.tweaker;

import io.github.dediamondpro.autoupdater.updater.ModUpdater;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class FMLLoadingPlugin implements IFMLLoadingPlugin {
    public FMLLoadingPlugin() {
        ModUpdater.updateMods();
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
