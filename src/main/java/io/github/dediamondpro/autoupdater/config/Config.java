package io.github.dediamondpro.autoupdater.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.github.dediamondpro.autoupdater.data.ModData;
import io.github.dediamondpro.autoupdater.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class Config {
    public static HashMap<String, ModData> modData = new HashMap<>();

    public static void load() {
        File folder = new File("config/AutoUpdater");
        if(!folder.exists() && !folder.mkdir())
            throw new IllegalStateException("Could not create AutoUpdater folder");
        File config = new File("config/AutoUpdater/AutoUpdater.json");
        if (!config.exists())
            return;
        try {
            String json = FileUtils.readFile(config);
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, ModData>>() {
            }.getType();
            modData = gson.fromJson(json, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        File folder = new File("config/AutoUpdater");
        if(!folder.exists() && !folder.mkdir())
            throw new IllegalStateException("Could not create AutoUpdater folder");
        try {
            String json = new Gson().toJson(modData);
            FileUtils.writeFile("config/AutoUpdater/AutoUpdater.json", json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
