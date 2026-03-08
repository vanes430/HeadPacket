package github.vanes430.headpacket.paper.manager;

import github.vanes430.headpacket.paper.PaperHeadPacket;
import java.io.File;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PaperConfigManager {
    private final PaperHeadPacket plugin;
    private FileConfiguration messages;

    public PaperConfigManager(PaperHeadPacket plugin) { this.plugin = plugin; }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        File msgFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!msgFile.exists()) plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(msgFile);
    }

    public String getMessage(String key, boolean withPrefix) {
        String msg = messages.getString(key, "Missing message: " + key);
        msg = color(msg);
        if (withPrefix) return color(messages.getString("prefix", "")) + msg;
        return msg;
    }

    private String color(String t) {
        if (t == null) return "";
        Component c = LegacyComponentSerializer.legacyAmpersand().deserialize(t);
        return LegacyComponentSerializer.legacySection().serialize(c);
    }

    public String getString(String p, String d) { return plugin.getConfig().getString(p, d); }
    public int getInt(String p, int d) { return plugin.getConfig().getInt(p, d); }
    public boolean getBoolean(String p, boolean d) { return plugin.getConfig().getBoolean(p, d); }
    public List<String> getStringList(String p) { return plugin.getConfig().getStringList(p); }
}
