package github.vanes430.headpacket.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import github.vanes430.headpacket.velocity.VelocityHeadPacket;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;

public class VelocityHeadPacketCommand implements SimpleCommand {
    private final VelocityHeadPacket plugin;

    public VelocityHeadPacketCommand(VelocityHeadPacket plugin) { this.plugin = plugin; }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        var source = invocation.source();
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadPlugin();
                source.sendMessage(Component.text(plugin.getMessage("reload-success", true)));
                return;
            }
            if (args[0].equalsIgnoreCase("process-motd")) {
                int percentage = 100;
                if (args.length > 1) {
                    try {
                        percentage = Integer.parseInt(args[1]);
                        if (percentage < 1) percentage = 1;
                        if (percentage > 100) percentage = 100;
                    } catch (NumberFormatException e) {
                        source.sendMessage(Component.text(plugin.getMessage("invalid-percentage", true)));
                        return;
                    }
                }
                plugin.processMotd(source, percentage); return;
            }
        }
        source.sendMessage(Component.text(plugin.getMessage("command-help-reload", true)));
        source.sendMessage(Component.text(plugin.getMessage("command-help-process", true)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            return Stream.of("reload", "process-motd")
                    .filter(s -> s.startsWith(args.length == 0 ? "" : args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override public boolean hasPermission(Invocation invocation) { return invocation.source().hasPermission("headpacket.admin"); }
}
