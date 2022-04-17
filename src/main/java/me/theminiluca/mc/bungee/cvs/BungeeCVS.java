package me.theminiluca.mc.bungee.cvs;

import me.theminiluca.mc.bungee.cvs.handle.BungeeAPI;
import me.theminiluca.mc.bungee.cvs.handle.Command;
import me.theminiluca.mc.bungee.cvs.handle.Messages;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeCVS extends JavaPlugin {

    public static BungeeCVS instance;
    public BungeeAPI bungee;
    @Override
    public void onEnable() {
        instance = this;
        bungee = BungeeAPI.of(this);
        getCommand("bungeecvs").setExecutor(new Command());
        new Messages(this);
        this.getServer().getPluginManager().registerEvents(new BukkitEvents(), this);
    }

    @Override
    public void onDisable() {

    }
}
