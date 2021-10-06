package io.github.dediamondpro.autoupdater.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dediamondpro.autoupdater.utils.WebUtils;

import java.util.HashMap;

public class SkyClientUpdater {
    public static HashMap<String, String> modsList = new HashMap<>();

    public static void fetchRepo() {
        System.out.println("Fetching SkyClient repo");
        JsonElement data = WebUtils.getRequest("https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/mods.json");
        if (data == null || !data.isJsonArray())
            return;
        JsonArray mods = data.getAsJsonArray();
        for (JsonElement element : mods) {
            if (element.isJsonObject()) {
                JsonObject mod = element.getAsJsonObject();
                if (mod.has("forge_id") && mod.has("url")) {
                    modsList.put(mod.get("forge_id").getAsString(), mod.get("url").getAsString());
                } else if (mod.has("forge_id") && mod.has("file")) {
                    modsList.put(mod.get("forge_id").getAsString(), "https://github.com/nacrt/SkyblockClient-REPO/raw/main/files/mods/"
                            + mod.get("file").getAsString());
                }
            }
        }
    }
}
