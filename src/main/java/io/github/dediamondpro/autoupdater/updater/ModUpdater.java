package io.github.dediamondpro.autoupdater.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dediamondpro.autoupdater.config.Config;
import io.github.dediamondpro.autoupdater.data.ModData;
import io.github.dediamondpro.autoupdater.utils.WebUtils;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModUpdater {

    public static final Pattern githubPattern = Pattern.compile("(https://)?(github\\.com/)?(?<user>[\\w-]{0,39})(/)(?<repo>[\\w-]{0,40})(.*)");
    public static final HashMap<String, File> modFiles = new HashMap<>();
    public static final HashMap<String, File> cachedFiles = new HashMap<>();
    public static final HashMap<String, String> tags = new HashMap<>();
    public static boolean hasShutdownHook = false;

    public static void updateMods() {
        Config.load();
        updateConfig();
        SkyClientUpdater.fetchRepo();
        update();
        Config.save();
    }

    private static void updateConfig() {
        HashMap<String, ModData> newData = new HashMap<>();
        File modDir = new File("mods");
        if (!modDir.exists() || !modDir.isDirectory() || modDir.listFiles() == null)
            return;
        List<File> allMods = new ArrayList<>(Arrays.asList(modDir.listFiles()));
        File versionModDir = new File("mods/" + Loader.MC_VERSION);
        if (versionModDir.exists() && versionModDir.isDirectory() && versionModDir.listFiles() != null)
            allMods.addAll(Arrays.asList(versionModDir.listFiles()));
        for (File mod : allMods) {
            if (mod.isFile() && mod.getName().endsWith(".jar")) {
                try {
                    JsonObject info = UpdateUtils.getMcMod(mod);
                    if (info != null && info.has("modid") && info.has("name")) {
                        String modid = info.get("modid").getAsString();
                        ModData data = new ModData(modid, info.get("name").getAsString());
                        if (Config.modData.containsKey(modid))
                            data = Config.modData.get(modid);
                        else {
                            if (Config.modData.containsKey(modid))
                                data.tag = Config.modData.get(modid).tag;
                            if (info.has("updateUrl") && githubPattern.matcher(info.get("updateUrl").getAsString()).matches())
                                data.url = info.get("updateUrl").getAsString();
                            else if (info.has("url") && githubPattern.matcher(info.get("url").getAsString()).matches())
                                data.url = info.get("url").getAsString();
                        }
                        if (info.has("version"))
                            data.modVersion = info.get("version").getAsString();
                        newData.put(modid, data);
                        modFiles.put(modid, mod);
                    }
                } catch (IOException ignored) {
                } catch (Exception e) {
                    System.out.println("Error while trying to process " + mod.getName());
                    e.printStackTrace();
                }
            }
        }
        Config.modData = newData;
    }

    private static void update() {
        for (ModData data : Config.modData.values()) {
            if (data.url != null && data.update && !data.useSkyClient) {
                updateMod(data);
            } else if (data.useSkyClient && SkyClientUpdater.modsList.containsKey(data.id)){
                SkyClientUpdater.updateSkyClient(data);
            }
        }
    }

    private static void updateMod(ModData data) {
        Matcher githubMatcher = githubPattern.matcher(data.url);
        if (githubMatcher.matches()) {
            System.out.println("Fetching https://api.github.com/repos/" + githubMatcher.group("user")
                    + "/" + githubMatcher.group("repo") + "/releases");
            JsonElement json = WebUtils.getRequest("https://api.github.com/repos/" + githubMatcher.group("user")
                    + "/" + githubMatcher.group("repo") + "/releases");
            if (json != null && json.isJsonArray()) {
                JsonArray releases = json.getAsJsonArray();
                try {
                    for (JsonElement element1 : releases) {
                        boolean done = false;
                        JsonObject release = element1.getAsJsonObject();
                        if ((data.tag == null || !data.tag.equals(release.get("tag_name").getAsString())) && release.has("assets") &&
                                (!release.has("prerelease") || !release.get("prerelease").getAsBoolean() || data.usePre)) {
                            for (JsonElement element2 : release.getAsJsonObject().getAsJsonArray("assets")) {
                                JsonObject asset = element2.getAsJsonObject();
                                String downloadUrl = asset.get("browser_download_url").getAsString();
                                String name = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
                                File cacheDir = new File("config/AutoUpdater/cache");
                                if (!cacheDir.exists() && !cacheDir.mkdir())
                                    throw new IllegalStateException("Could not create cache folder");
                                File cache = new File(cacheDir, name);
                                System.out.println("Downloading " + downloadUrl);
                                WebUtils.downloadFile(downloadUrl, cache);
                                if (UpdateUtils.validateMCVersion(cache)) {
                                    if (modFiles.get(data.id).delete()) {
                                        Files.copy(cache.toPath(), modFiles.get(data.id).toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        if (!cache.delete())
                                            System.out.println("Could not delete cache for mod: " + data.id);
                                        Config.modData.get(data.id).tag = release.get("tag_name").getAsString();
                                        System.out.println("Successfully updated " + data.id + " to " + release.get("tag_name").getAsString());
                                    } else {
                                        System.out.println("Could not update " + data.id + ", This will be retried at shutdown.");
                                        cachedFiles.put(data.id, cache);
                                        tags.put(data.id, release.get("tag_name").getAsString());
                                        if (!hasShutdownHook) {
                                            UpdateUtils.addShutdownHook();
                                            hasShutdownHook = true;
                                        }
                                    }
                                    done = true;
                                    break;
                                } else {
                                    System.out.println("MC version does not match.");
                                    Files.delete(cache.toPath());
                                }
                            }
                        } else if (data.tag.equals(release.get("tag_name").getAsString()))
                            break;
                        if (done)
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
