package github.vanes430.headpacket.velocity.manager;

import github.vanes430.headpacket.common.HeadPacketPlugin;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;

public class VelocityConfigManager {
    private final Path dataDirectory;
    private final HeadPacketPlugin plugin;
    private Map<String, Object> config = new HashMap<>();
    private Map<String, Object> messages = new HashMap<>();

    public VelocityConfigManager(Path dataDirectory, HeadPacketPlugin plugin) {
        this.dataDirectory = dataDirectory; this.plugin = plugin;
    }

    public void load() {
        if (!Files.exists(dataDirectory)) {
            try { Files.createDirectories(dataDirectory); }
            catch (IOException e) { plugin.error("Could not create data directory", e); }
        }
        config = loadFile("config.yml"); messages = loadFile("messages.yml");
    }

    private Map<String, Object> loadFile(String name) {
        Path path = dataDirectory.resolve(name);
        if (!Files.exists(path)) {
            try (InputStream in = getClass().getResourceAsStream("/" + name)) {
                if (in != null) Files.copy(in, path);
            } catch (IOException e) { plugin.error("Could not save " + name, e); }
        }
        try { return new Yaml().load(Files.newInputStream(path)); }
        catch (IOException e) { plugin.error("Could not load " + name, e); return new HashMap<>(); }
    }

    public String getMessage(String key, boolean withPrefix) {
        String msg = (String) messages.getOrDefault(key, "Missing: " + key);
        return withPrefix ? (String) messages.getOrDefault("prefix", "") + msg : msg;
    }

    public String getString(String path, String def) { return (String) config.getOrDefault(path, def); }
    public int getInt(String path, int def) {
        Object val = config.get(path); return val instanceof Integer ? (Integer) val : def;
    }
    public boolean getBoolean(String path, boolean def) {
        Object val = config.get(path); return val instanceof Boolean ? (Boolean) val : def;
    }
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        return (List<String>) config.getOrDefault(path, Collections.emptyList());
    }
}
