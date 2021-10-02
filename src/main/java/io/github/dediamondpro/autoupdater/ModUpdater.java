package io.github.dediamondpro.autoupdater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.dediamondpro.autoupdater.config.Config;
import io.github.dediamondpro.autoupdater.data.ModData;
import io.github.dediamondpro.autoupdater.utils.WebUtils;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModUpdater {

    private static final Pattern githubPattern = Pattern.compile("(https://)?(github\\.com/)?(?<user>[\\w-]{0,39})(/)(?<repo>[\\w-]{0,40})(.*)");
    private static final HashMap<String, File> modFiles = new HashMap<>();

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
                    if (info.has("modid")) {
                        String modid = info.get("modid").getAsString();
                        String url = null;
                        String tag = null;
                        if (Config.modData.containsKey(modid))
                            tag = Config.modData.get(modid).tag;
                        if (info.has("updateUrl") && githubPattern.matcher(info.get("updateUrl").getAsString()).matches())
                            url = info.get("updateUrl").getAsString();
                        else if (info.has("url") && githubPattern.matcher(info.get("url").getAsString()).matches())
                            url = info.get("url").getAsString();
                        else if (Config.modData.containsKey(modid))
                            url = Config.modData.get(modid).url;
                        newData.put(modid, new ModData(modid, tag, url));
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
            if (data.url != null) {
                Matcher githubMatcher = githubPattern.matcher(data.url);
                if (githubMatcher.matches()) {
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
                                        WebUtils.downloadFile(downloadUrl, cache);
                                        if (validateMCVersion(cache)) {
                                            if (modFiles.get(data.id).delete()) {
                                                Files.copy(cache.toPath(), modFiles.get(data.id).toPath(), StandardCopyOption.REPLACE_EXISTING);
                                                if (!cache.delete())
                                                    System.out.println("Could not delete cache for mod: " + data.id);
                                                Config.modData.get(data.id).tag = release.get("tag_name").getAsString();
                                            } else
                                                System.out.println("Could not update " + data.id + ".");
                                            done = true;
                                            break;
                                        } else {
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
}
