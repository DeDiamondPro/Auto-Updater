package io.github.dediamondpro.autoupdater.updater;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.dediamondpro.autoupdater.config.Config;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

public class UpdateUtils {

    public static JsonObject getMcMod(File file) throws IOException {
        URL input = new URL("jar:file:" + file.getAbsolutePath() + "!/mcmod.info");
        JarURLConnection conn = (JarURLConnection) input.openConnection();
        conn.setUseCaches(false);
        JarFile jarFile = conn.getJarFile();
        InputStream in = jarFile.getInputStream(conn.getJarEntry());
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(new InputStreamReader(in));
        jarFile.close();
        in.close();
        if (element.isJsonArray())
            return element.getAsJsonArray().get(0).getAsJsonObject();
        if (element.isJsonObject() && element.getAsJsonObject().has("modList"))
            return element.getAsJsonObject().get("modList").getAsJsonArray().get(0).getAsJsonObject();
        System.out.println(file.getName() + "has an unrecognized mcmod.info: " + element);
        return null;
    }

    public static boolean validateMCVersion(File file) throws IOException {
        JsonObject info = getMcMod(file);
        if (info == null || !info.has("mcversion") || info.get("mcversion").getAsString().equals(""))
            return true;
        return info.get("mcversion").getAsString().equals(Loader.MC_VERSION);
    }

    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (String id : ModUpdater.cachedFiles.keySet()) {
                File source =  ModUpdater.cachedFiles.get(id);
                File dest =  ModUpdater.modFiles.get(id);
                if (source != null && source.exists() && dest != null && dest.exists()) {
                    System.out.println("Attempting to update " + id);
                    try {
                        UpdateUtils.copyFile(source, dest);
                        if (!source.delete())
                            System.out.println("Could not delete cache for mod: " + id);
                        System.out.println("Updated successfully.");
                        Config.modData.get(id).tag =  ModUpdater.tags.get(id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (source != null && source.exists()) {
                    System.out.println("Attempting to update " + id);
                    if (source.renameTo(source)) {
                        System.out.println("Updated successfully.");
                        Config.modData.get(id).tag =  ModUpdater.tags.get(id);
                    } else
                        System.out.println("Update failed.");
                } else
                    System.out.println("Update failed.");
            }
            Config.save();
        }));
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
