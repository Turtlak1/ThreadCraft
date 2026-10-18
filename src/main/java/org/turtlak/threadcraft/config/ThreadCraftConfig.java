package org.turtlak.threadcraft.config;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ThreadCraftConfig {

    private static Path configDir;
    private static final Path CONFIG_FILE = null;

    public static int generationThreads = Runtime.getRuntime().availableProcessors();
    public static int meshingThreads = Runtime.getRuntime().availableProcessors();
    public static int lightingThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    public static int maxPendingChunks = 0;
    public static boolean enableAsyncGen = true;
    public static boolean enableAsyncMesh = true;

    public static void init(Path configDir) {
        ThreadCraftConfig.configDir = configDir;
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config dir", e);
        }
        maxPendingChunks = generationThreads * 4;
    }

    public static void load() {
        Path file = configDir.resolve("threadcraft.properties");
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (var in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException ignored) {
            }
        }
        generationThreads = getInt(props, "generationThreads", generationThreads);
        meshingThreads = getInt(props, "meshingThreads", meshingThreads);
        lightingThreads = getInt(props, "lightingThreads", lightingThreads);
        maxPendingChunks = getInt(props, "maxPendingChunks", generationThreads * 4);
        enableAsyncGen = getBoolean(props, "enableAsyncGen", enableAsyncGen);
        enableAsyncMesh = getBoolean(props, "enableAsyncMesh", enableAsyncMesh);
        save();
    }

    public static void save() {
        Path file = configDir.resolve("threadcraft.properties");
        Properties props = new Properties();
        props.setProperty("generationThreads", String.valueOf(generationThreads));
        props.setProperty("meshingThreads", String.valueOf(meshingThreads));
        props.setProperty("lightingThreads", String.valueOf(lightingThreads));
        props.setProperty("maxPendingChunks", String.valueOf(maxPendingChunks));
        props.setProperty("enableAsyncGen", String.valueOf(enableAsyncGen));
        props.setProperty("enableAsyncMesh", String.valueOf(enableAsyncMesh));
        try (OutputStream out = Files.newOutputStream(file)) {
            props.store(out, "ThreadCraft configuration");
        } catch (IOException ignored) {
        }
    }

    private static int getInt(Properties props, String key, int def) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean getBoolean(Properties props, String key, boolean def) {
        return Boolean.parseBoolean(props.getProperty(key, String.valueOf(def)));
    }

    public static int generationThreads() { return generationThreads; }
    public static int meshingThreads() { return meshingThreads; }
    public static int lightingThreads() { return lightingThreads; }
    public static int maxPendingChunks() { return maxPendingChunks; }
    public static boolean enableAsyncGen() { return enableAsyncGen; }
    public static boolean enableAsyncMesh() { return enableAsyncMesh; }
}
