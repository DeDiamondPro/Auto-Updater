package io.github.dediamondpro.autoupdater.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dediamondpro.autoupdater.utils.WebUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SkyClientUpdater {
    public static HashMap<String, URL> modsList = new HashMap<>();

    public static void fetchRepo() {
        JsonElement data = WebUtils.getRequest("https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/mods.json");
        if (data == null || !data.isJsonArray())
            return;
        JsonArray mods = data.getAsJsonArray();
        for (JsonElement element : mods) {
            if (element.isJsonObject()) {
                JsonObject mod = element.getAsJsonObject();
                if (mod.has("forge_id") && mod.has("url")) {
                    try {
                        modsList.put(mod.get("forge_id").getAsString(), new URL(mod.get("url").getAsString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (mod.has("forge_id") && mod.has("file")){
                    try {
                        modsList.put(mod.get("forge_id").getAsString(),
                                new URL("https://github.com/nacrt/SkyblockClient-REPO/raw/main/files/mods/" + mod.get("file").getAsString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
