package github.vanes430.headpacket.common;

import net.kyori.adventure.text.Component;

public interface HeadPacketSender {
    void sendMessage(String message);
    void sendMessage(Component component);
}
