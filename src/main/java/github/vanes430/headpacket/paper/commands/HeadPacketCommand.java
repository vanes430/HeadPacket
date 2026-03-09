package github.vanes430.headpacket.paper.commands;

import static io.papermc.paper.command.brigadier.Commands.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import github.vanes430.headpacket.paper.PaperHeadPacket;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

@SuppressWarnings("UnstableApiUsage")
public class HeadPacketCommand {
    private final PaperHeadPacket plugin;

    public HeadPacketCommand(PaperHeadPacket plugin) { this.plugin = plugin; }

    public void register(Commands commands) {
        LiteralCommandNode<CommandSourceStack> node = literal("headpacket")
                .then(literal("reload")
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof ConsoleCommandSender)) {
                                sender.sendMessage(plugin.getMessage("console-only", true));
                                return 0;
                            }
                            plugin.reloadPlugin();
                            sender.sendMessage(plugin.getMessage("reload-success", true));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("process-motd")
                        .then(argument("percentage", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    if (!(sender instanceof ConsoleCommandSender)) {
                                        sender.sendMessage(plugin.getMessage("console-only", true));
                                        return 0;
                                    }
                                    plugin.processMotd(sender, IntegerArgumentType.getInteger(ctx, "percentage"));
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof ConsoleCommandSender)) {
                                sender.sendMessage(plugin.getMessage("console-only", true));
                                return 0;
                            }
                            plugin.processMotd(sender, 100);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
        commands.register(node, "HeadPacket command", java.util.List.of("hp"));
    }
}
