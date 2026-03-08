package github.vanes430.headpacket.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.velocitypowered.api.command.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.*;
import com.velocitypowered.api.plugin.*;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import github.vanes430.headpacket.common.*;
import github.vanes430.headpacket.common.hooks.MotdHandler;
import github.vanes430.headpacket.common.manager.JsonCacheManager;
import github.vanes430.headpacket.velocity.commands.VelocityHeadPacketCommand;
import github.vanes430.headpacket.velocity.manager.VelocityConfigManager;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

@Plugin(id = "headpacket", name = "HeadPacket", version = "1.0.0-SNAPSHOT", authors = {"vanes430"}, dependencies = {@Dependency(id = "packetevents")})
public class VelocityHeadPacket implements HeadPacketPlugin {
    private final ProxyServer server; private final Logger logger; private final Path dataDir; private final PluginContainer container;
    private final List<List<String>> motdUrls = new CopyOnWriteArrayList<>();
    private final VelocityConfigManager config; private final JsonCacheManager jsonCache = new JsonCacheManager();
    private ImageProcessor processor; private TextureCache mappingCache;

    @Inject
    public VelocityHeadPacket(ProxyServer s, Logger l, @DataDirectory Path d, PluginContainer c) {
        this.server = s; this.logger = l; this.dataDir = d; this.container = c;
        this.config = new VelocityConfigManager(d, this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        config.load();
        File imagesFolder = new File(dataDir.toFile(), config.getString("images-folder", "images"));
        if (!imagesFolder.exists()) imagesFolder.mkdirs();
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, container, logger, dataDir));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(true);
        PacketEvents.getAPI().load(); PacketEvents.getAPI().init();
        this.mappingCache = new TextureCache(dataDir.resolve("mapping.json").toFile());
        this.processor = new ImageProcessor(new MineSkinClient(config.getString("mineskin-api-key", "")),
            new TextureCache(dataDir.resolve("cache.json").toFile()), dataDir.toFile(), config::getMessage);
        PacketEvents.getAPI().getEventManager().registerListener(new MotdHandler(this), PacketListenerPriority.HIGHEST);
        reloadPlugin();
        CommandManager cm = server.getCommandManager();
        cm.register(cm.metaBuilder("headpacket").aliases("hp").plugin(this).build(), new VelocityHeadPacketCommand(this));
    }

    @Subscribe public void onProxyShutdown(ProxyShutdownEvent event) { PacketEvents.getAPI().terminate(); }

    @Override public void reloadPlugin() {
        config.load();
        File imagesFolder = new File(dataDir.toFile(), config.getString("images-folder", "images"));
        if (!imagesFolder.exists()) imagesFolder.mkdirs();
        if (this.processor != null) this.processor.shutdown();
        this.processor = new ImageProcessor(new MineSkinClient(config.getString("mineskin-api-key", "")),
            new TextureCache(dataDir.resolve("cache.json").toFile()), dataDir.toFile(), config::getMessage);
        motdUrls.clear(); mappingCache.load();
        String motdCache = mappingCache.get("motd");
        if (motdCache != null && !motdCache.isEmpty()) {
            for (String row : motdCache.split(";")) if (!row.isEmpty()) motdUrls.add(Arrays.asList(row.split(",")));
        }
        jsonCache.buildMotdCache(motdUrls); jsonCache.buildHoverCache(config.getStringList("hover-messages"));
    }

    @Override public String getMessage(String k, boolean p) { return config.getMessage(k, p); }
    @Override public void info(String m) { logger.info(m); }
    @Override public void warn(String m) { logger.warn(m); }
    @Override public void error(String m, Throwable e) { logger.error(m, e); }

    public void processMotd(CommandSource source, int pct) {
        File file = new File(dataDir.resolve(config.getString("images-folder", "images")).toFile(), config.getString("motd-image", "motd.png"));
        if (!file.exists()) { source.sendMessage(Component.text(config.getMessage("image-not-found", true).replace("{file}", file.getName()))); return; }
        source.sendMessage(Component.text(config.getMessage("processing-start", true)));
        processor.process(file, wrapSource(source), pct).thenAccept(rows -> {
            motdUrls.clear(); motdUrls.addAll(rows);
            List<String> rowStrings = new ArrayList<>(); rows.forEach(urls -> rowStrings.add(String.join(",", urls)));
            mappingCache.put("motd", String.join(";", rowStrings));
            jsonCache.buildMotdCache(motdUrls);
            source.sendMessage(Component.text(config.getMessage("processing-complete", true).replace("{rows}", String.valueOf(rows.size()))));
        }).exceptionally(ex -> { source.sendMessage(Component.text(config.getMessage("processing-failed", true).replace("{error}", ex.getMessage()))); return null; });
    }

    @Override public int getMotdMinimumProtocol() { return config.getInt("motd_minimum_protocol", 0); }
    @Override public List<List<String>> getMotdUrls() { return motdUrls; }
    @Override public JsonArray getCachedMotdJson() { return jsonCache.getMotdCache(); }
    @Override public JsonArray getCachedHoverJson() { return jsonCache.getHoverCache(); }
    @Override public boolean isAlwaysPlusOne() { return config.getBoolean("always_plus_one", false); }
    @Override public boolean isIgnoreBedrock() { return config.getBoolean("ignore-bedrock", true); }

    public HeadPacketSender wrapSource(CommandSource s) {
        return new HeadPacketSender() {
            @Override public void sendMessage(String m) { s.sendMessage(Component.text(m)); }
            @Override public void sendMessage(Component c) { s.sendMessage(c); }
        };
    }
}
