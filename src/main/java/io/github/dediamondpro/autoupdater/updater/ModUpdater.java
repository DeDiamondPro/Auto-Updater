package io.github.dediamondpro.autoupdater.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.dediamondpro.autoupdater.config.Config;
import io.github.dediamondpro.autoupdater.data.ModData;
import io.github.dediamondpro.autoupdater.utils.WebUtils;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModUpdater {

    public static final Pattern githubPattern = Pattern.compile("(https://)?(github\\.com/)?(?<user>[\\w-]{0,39})(/)(?<repo>[\\w-]{0,40})(.*)");
    private static final HashMap<String, File> modFiles = new HashMap<>();
    private static final HashMap<String, File> cachedFiles = new HashMap<>();
    public static final HashMap<String, String> tags = new HashMap<>();
    public static boolean hasShutdownHook = false;

    public static void updateMods() {
        Config.load();
        updateConfig();
        update();
        Config.save();
    }

    private static void updateConfig() {
        HashMap<String, ModData> newData = new HashMap<>();
        File mods = new File("mods");
        for (File mod : mods.listFiles()) {
            if (mod.isFile() && mod.getName().endsWith(".jar")) {
                try {
                    JsonObject info = getMcMod(mod);
                    if (info.has("modid") && info.has("name")) {
                        String modid = info.get("modid").getAsString();
                        ModData data = new ModData(modid, null, null, info.get("name").getAsString());
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
                        newData.put(modid, data);
                        modFiles.put(modid, mod);
                    }
                } catch (IOException ignored) {
                }
            }
        }
        Config.modData = newData;
    }

    private static void update() {
        for (ModData data : Config.modData.values()) {
            if (data.url != null && data.update) {
                Matcher githubMatcher = githubPattern.matcher(data.url);
                if (githubMatcher.matches()) {
                    System.out.println("Fetching https://api.github.com/repos/" + githubMatcher.group("user")
                            + "/" + githubMatcher.group("repo") + "/releases");
                    JsonElement json = WebUtils.getRequest("https://api.github.com/repos/" + githubMatcher.group("user")
                            + "/" + githubMatcher.group("repo") + "/releases");
                    if (json != null) {
                        JsonArray releases = json.getAsJsonArray();
                        try {
                            for (JsonElement element1 : releases) {
                                boolean done = false;
                                JsonObject release = element1.getAsJsonObject();
                                if ((data.tag == null || !data.tag.equals(release.get("tag_name").getAsString())) && release.has("assets")) {
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
                                        if (validateMCVersion(cache)) {
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
                                                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                                        for (String id : cachedFiles.keySet()) {
                                                            File source = cachedFiles.get(id);
                                                            File dest = modFiles.get(id);
                                                            if (source != null && source.exists() && dest != null && dest.exists()) {
                                                                System.out.println("Attempting to update " + id);
                                                                try {
                                                                    copyFile(source, dest);
                                                                    if (!source.delete())
                                                                        System.out.println("Could not delete cache for mod: " + id);
                                                                    System.out.println("Updated successfully.");
                                                                    Config.modData.get(id).tag = tags.get(id);
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } else if (source != null && source.exists()) {
                                                                System.out.println("Attempting to update " + id);
                                                                if (source.renameTo(new File("mods", source.getName()))) {
                                                                    System.out.println("Updated successfully.");
                                                                    Config.modData.get(id).tag = tags.get(id);
                                                                } else
                                                                    System.out.println("Update failed.");
                                                            } else
                                                                System.out.println("Update failed.");
                                                        }
                                                        Config.save();
                                                    }));
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
    }

    private static JsonObject getMcMod(File file) throws IOException {
        URL input = new URL("jar:file:" + file.getAbsolutePath() + "!/mcmod.info");
        JarURLConnection conn = (JarURLConnection) input.openConnection();
        conn.setUseCaches(false);
        JarFile jarFile = conn.getJarFile();
        InputStream in = jarFile.getInputStream(conn.getJarEntry());
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(new InputStreamReader(in)).getAsJsonArray();
        jarFile.close();
        in.close();
        return array.get(0).getAsJsonObject();
    }

    private static boolean validateMCVersion(File file) throws IOException {
        JsonObject info = getMcMod(file);
        if (!info.has("mcversion") || info.get("mcversion").getAsString().equals(""))
            return true;
        return info.get("mcversion").getAsString().equals(Loader.MC_VERSION);
    }

    /**
     * Adapted from SkytilsMod and Wynntils under GNU Affero General Public License v3.0
     * Modified to be more compact
     * https://github.com/Skytils/SkytilsMod/blob/0.x/LICENSE
     * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
     *
     * @param sourceFile The source file
     * @param destFile   Where it will be
     * @author Wynntils
     * Copy a file from a location to another
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        try (InputStream source = new FileInputStream(sourceFile); OutputStream dest = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0)
                dest.write(buffer, 0, length);
        }
    }
}
