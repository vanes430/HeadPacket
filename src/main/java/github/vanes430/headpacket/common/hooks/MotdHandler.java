package github.vanes430.headpacket.common.hooks;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.*;
import github.vanes430.headpacket.common.HeadPacketPlugin;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class MotdHandler implements PacketListener {
    private static final AttributeKey<?> FLOODGATE_ATTR = AttributeKey.valueOf("floodgate-player");
    private final HeadPacketPlugin plugin;

    public MotdHandler(HeadPacketPlugin plugin) { this.plugin = plugin; }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Status.Server.RESPONSE) {
            if (plugin.isIgnoreBedrock() && isBedrock(event)) return;
            WrapperStatusServerResponse wrapper = new WrapperStatusServerResponse(event);
            String originalJson = wrapper.getComponentJson();
            JsonObject fullStatus = (originalJson != null) ? JsonParser.parseString(originalJson).getAsJsonObject() : new JsonObject();

            JsonArray hoverCache = plugin.getCachedHoverJson();
            JsonObject players = fullStatus.getAsJsonObject("players");
            if (players == null) {
                players = new JsonObject();
                players.addProperty("max", 0); players.addProperty("online", 0);
                fullStatus.add("players", players);
            }
            if (hoverCache.size() > 0) players.add("sample", hoverCache);

            if (plugin.isAlwaysPlusOne()) {
                int online = players.has("online") ? players.get("online").getAsInt() : 0;
                players.addProperty("max", online + 1);
            }

            int minProtocol = plugin.getMotdMinimumProtocol();
            if (event.getUser().getClientVersion().getProtocolVersion() >= minProtocol) {
                JsonArray motdCache = plugin.getCachedMotdJson();
                if (motdCache.size() > 0) {
                    JsonObject description = new JsonObject();
                    description.addProperty("color", "white"); description.addProperty("italic", false);
                    description.addProperty("bold", false); description.add("extra", motdCache);
                    description.addProperty("shadow_color", -1); description.addProperty("text", "");
                    fullStatus.add("description", description);
                }
            }
            wrapper.setComponentJson(fullStatus.toString());
            wrapper.write();
            event.markForReEncode(true);
        }
    }

    private boolean isBedrock(PacketSendEvent event) {
        try {
            Object channelObj = event.getUser().getChannel();
            if (channelObj instanceof Channel channel) {
                return channel.hasAttr(FLOODGATE_ATTR) && channel.attr(FLOODGATE_ATTR).get() != null;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
