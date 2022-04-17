package me.theminiluca.mc.bungee.cvs.handle;

import me.theminiluca.mc.bungee.cvs.BungeeCVS;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Properties;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender.isOp()) {
            if (args.length == 0) {
                sender.sendMessage("BungeeCVS ( BungeeCord Convenience )");
                sender.sendMessage("/bungeeCVS reload - Reload the files.");
                sender.sendMessage("author : MiniLuca#7822");
                return false;
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                    new Messages(BungeeCVS.instance);
                    sender.sendMessage(Messages.getProperties(Messages.PropertiesKey.PLUGIN_RELOAD));
                    if (Messages.isProperties(Messages.PropertiesKey.PLUGIN_RELOAD_MESSAGES_ENABLE))
                    for (Messages.PropertiesKey key : Messages.PropertiesKey.values()) {
                        sender.sendMessage(ChatColor.GRAY + key.property() + "=" + ChatColor.DARK_GRAY + Messages.getProperties(key));
                    }
                }
            }
        }
        return false;
    }
}
