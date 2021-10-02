package io.github.dediamondpro.autoupdater.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FileUtils {
    public static String readFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder builder = new StringBuilder();
        try {
            while (scanner.hasNextLine()) {
                builder.append(scanner.next());
            }
            scanner.close();
        } catch (NoSuchElementException ignored) {
        }
        return builder.toString();
    }

    public static void writeFile(String file, String data) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(data);
        writer.close();
    }
}
