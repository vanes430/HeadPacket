package github.vanes430.headpacket.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.gson.JsonArray;
import github.vanes430.headpacket.common.*;
import github.vanes430.headpacket.common.hooks.MotdHandler;
import github.vanes430.headpacket.common.manager.JsonCacheManager;
import github.vanes430.headpacket.paper.commands.HeadPacketCommand;
import github.vanes430.headpacket.paper.manager.PaperConfigManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperHeadPacket extends JavaPlugin implements HeadPacketPlugin {
    private final List<List<String>> motdUrls = new CopyOnWriteArrayList<>();
    private final PaperConfigManager config = new PaperConfigManager(this);
    private final JsonCacheManager jsonCache = new JsonCacheManager();
    private ImageProcessor processor;
    private TextureCache mappingCache;

    @Override
    public void onLoad() {
        if (!PacketEvents.getAPI().isLoaded()) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(true);
            PacketEvents.getAPI().load();
        }
    }

    @Override
    public void onEnable() {
        config.load();
        File imagesFolder = new File(getDataFolder(), config.getString("images-folder", "images"));
        if (!imagesFolder.exists()) imagesFolder.mkdirs();
        this.mappingCache = new TextureCache(new File(getDataFolder(), "mapping.json"));
        this.processor = new ImageProcessor(new MineSkinClient(config.getString("mineskin-api-key", "")),
            new TextureCache(new File(getDataFolder(), "cache.json")), getDataFolder(), config::getMessage, this);
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new MotdHandler(this), PacketListenerPriority.HIGHEST);
        reloadPlugin();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> new HeadPacketCommand(this).register(e.registrar()));
    }

    @Override public void onDisable() { PacketEvents.getAPI().terminate(); }

    @Override public void reloadPlugin() {
        config.load();
        File imagesFolder = new File(getDataFolder(), config.getString("images-folder", "images"));
        if (!imagesFolder.exists()) imagesFolder.mkdirs();
        if (this.processor != null) this.processor.shutdown();
        this.processor = new ImageProcessor(new MineSkinClient(config.getString("mineskin-api-key", "")),
            new TextureCache(new File(getDataFolder(), "cache.json")), getDataFolder(), config::getMessage, this);
        motdUrls.clear(); mappingCache.load();
        String motdCache = mappingCache.get("motd");
        if (motdCache != null && !motdCache.isEmpty()) {
            for (String row : motdCache.split(";")) if (!row.isEmpty()) motdUrls.add(Arrays.asList(row.split(",")));
        }
        jsonCache.buildMotdCache(motdUrls);
        jsonCache.buildHoverCache(config.getStringList("hover-messages"));
    }

    @Override public String getMessage(String k, boolean p) { return config.getMessage(k, p); }
    @Override public void info(String m) { getLogger().info(m); }
    @Override public void warn(String m) { getLogger().warning(m); }
    @Override public void error(String m, Throwable e) { getLogger().log(java.util.logging.Level.SEVERE, m, e); }

    public void processMotd(CommandSender sender, int pct) {
        String apiKey = config.getString("mineskin-api-key", "");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            sender.sendMessage(config.getMessage("mineskin-key-missing", true));
            return;
        }
        File file = new File(new File(getDataFolder(), config.getString("images-folder", "images")), config.getString("motd-image", "motd.png"));
        if (!file.exists()) { sender.sendMessage(config.getMessage("image-not-found", true).replace("{file}", file.getName())); return; }
        sender.sendMessage(config.getMessage("processing-start", true));
        getServer().getGlobalRegionScheduler().run(this, t -> processor.process(file, wrapSender(sender), pct).thenAccept(rows -> {
            motdUrls.clear(); motdUrls.addAll(rows);
            List<String> rowStrings = new ArrayList<>();
            rows.forEach(urls -> rowStrings.add(String.join(",", urls)));
            mappingCache.put("motd", String.join(";", rowStrings));
            jsonCache.buildMotdCache(motdUrls);
            sender.sendMessage(config.getMessage("processing-complete", true).replace("{rows}", String.valueOf(rows.size())));
        }).exceptionally(ex -> { sender.sendMessage(config.getMessage("processing-failed", true).replace("{error}", ex.getMessage())); return null; }));
    }

    private HeadPacketSender wrapSender(CommandSender s) {
        return new HeadPacketSender() {
            @Override public void sendMessage(String m) { s.sendMessage(m); }
            @Override public void sendMessage(Component c) { s.sendMessage(c); }
        };
    }

    @Override public int getMotdMinimumProtocol() { return config.getInt("motd_minimum_protocol", 0); }
    @Override public List<List<String>> getMotdUrls() { return motdUrls; }
    @Override public JsonArray getCachedMotdJson() { return jsonCache.getMotdCache(); }
    @Override public JsonArray getCachedHoverJson() { return jsonCache.getHoverCache(); }
    @Override public boolean isAlwaysPlusOne() { return config.getBoolean("always_plus_one", false); }
    @Override public boolean isIgnoreBedrock() { return config.getBoolean("ignore-bedrock", true); }

    @Override
    public List<String> getFallbackMotd() {
        return List.of(
            config.getString("fallback-motd.line1", ""),
            config.getString("fallback-motd.line2", "")
        );
    }

    @Override
    public int getMineSkinDelay() {
        return config.getInt("mineskin-delay", 2000);
    }
}
