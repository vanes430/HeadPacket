package github.vanes430.headpacket.common;

import com.google.gson.JsonArray;
import java.util.List;

public interface HeadPacketPlugin {
    void reloadPlugin();
    String getMessage(String key, boolean withPrefix);
    void info(String msg);
    void warn(String msg);
    void error(String msg, Throwable e);
    int getMotdMinimumProtocol();
    List<List<String>> getMotdUrls();
    JsonArray getCachedMotdJson();
    JsonArray getCachedHoverJson();
    boolean isAlwaysPlusOne();
    boolean isIgnoreBedrock();
    List<String> getFallbackMotd();
    int getMineSkinDelay();
}
