package io.github.dediamondpro.autoupdater.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dediamondpro.autoupdater.config.Config;
import io.github.dediamondpro.autoupdater.data.ModData;
import io.github.dediamondpro.autoupdater.utils.WebUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class SkyClientUpdater {
    public static HashMap<String, String> modsList = new HashMap<>();
    public static HashMap<String, String> skyClientVersions = new HashMap<>();

    public static void fetchRepo() {
        System.out.println("Fetching SkyClient repo");
        JsonElement commits = WebUtils.getRequest("https://api.github.com/repos/nacrt/SkyblockClient-REPO/commits");
        if (commits == null)
            return;
        String commitId;
        try {
            commitId = commits.getAsJsonArray().get(0).getAsJsonObject().get("sha").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        JsonElement data = WebUtils.getRequest("https://cdn.jsdelivr.net/gh/nacrt/SkyblockClient-REPO@" + commitId + "/files/mods.json");
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

    public static void updateSkyClient(ModData mod) {
        String url = modsList.get(mod.id);
        if (mod.skyClientVersion == null || !mod.skyClientVersion.equals(url)) {
            File cacheDir = new File("config/AutoUpdater/cache");
            if (!cacheDir.exists() && !cacheDir.mkdir())
                throw new IllegalStateException("Could not create cache folder");
            File cache = new File(cacheDir, url.substring(url.lastIndexOf("/") + 1));
            try {
                System.out.println("Downloading " + url);
                WebUtils.downloadFile(url, cache);
                if (ModUpdater.modFiles.get(mod.id).delete()) {
                    Files.copy(cache.toPath(), ModUpdater.modFiles.get(mod.id).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if (!cache.delete())
                        System.out.println("Could not delete cache for mod: " + mod.id);
                    Config.modData.get(mod.id).skyClientVersion = url;
                    System.out.println("Successfully updated " + mod.id + " to " + url);
                } else {
                    System.out.println("Could not update " + mod.id + ", This will be retried at shutdown.");
                    ModUpdater.cachedFiles.put(mod.id, cache);
                    skyClientVersions.put(mod.id, url);
                    if (!ModUpdater.hasShutdownHook) {
                        UpdateUtils.addShutdownHook();
                        ModUpdater.hasShutdownHook = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
