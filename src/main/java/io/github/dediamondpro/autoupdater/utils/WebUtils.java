package io.github.dediamondpro.autoupdater.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class WebUtils {
    public static JsonElement getRequest(String site) {
        try {
            URL url = new URL(site);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            if (status != 200) {
                System.out.println("API request failed, status code " + status);
                return null;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            JsonParser parser = new JsonParser();
            return parser.parse(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void downloadFile(String url, File location) {
        try {
            URLConnection con = new URL(url).openConnection();
            con.setRequestProperty("User-Agent", "mmc-auto-updater");
            InputStream in = con.getInputStream();
            Files.copy(in, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}