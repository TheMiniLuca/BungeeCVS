package me.theminiluca.mc.bungee.cvs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.IOException;
import java.net.Socket;

import static me.theminiluca.mc.bungee.cvs.handle.Messages.PropertiesKey;
import static me.theminiluca.mc.bungee.cvs.handle.Messages.getProperties;


public class BukkitEvents implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity target = event.getRightClicked();
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (target instanceof Player) {
            if (Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(target.getCustomName())).findAny().orElse(null) == null) {
                String name = event.getRightClicked().getCustomName();
                if (name != null) {
                    BungeeCVS.instance.bungee.getServerIp(name).whenComplete((result, error) -> {
                        Socket socket = null;
                        try {
                            socket = new Socket(result.getAddress(), result.getPort());
                            if (socket.isConnected()) {
                                BungeeCVS.instance.bungee.sendMessage(player.getName(), getProperties(PropertiesKey.CONNECTION_READY_MESSAGE).replace("%server%", name));
                                BungeeCVS.instance.bungee.connect(player, name);
                                BungeeCVS.instance.bungee.sendMessage(player.getName(), getProperties(PropertiesKey.CONNECTION_SUCCESS).replace("%server%", name));
                            }
                        } catch (IOException e) {
                            player.sendMessage(getProperties(PropertiesKey.CONNECTION_REFUSED));
                        }
                    });
                }
            }
        }
    }
}
