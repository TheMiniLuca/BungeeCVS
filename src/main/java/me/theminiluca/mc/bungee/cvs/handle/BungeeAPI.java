package me.theminiluca.mc.bungee.cvs.handle;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class BungeeAPI {
    private static final WeakHashMap<Plugin, BungeeAPI> registeredInstances = new WeakHashMap();
    private final PluginMessageListener messageListener;
    private final Plugin plugin;
    private final Map<String, Queue<CompletableFuture<?>>> callbackMap;
    private Map<String, BungeeAPI.ForwardConsumer> forwardListeners;
    private BungeeAPI.ForwardConsumer globalForwardListener;

    public static synchronized BungeeAPI of(Plugin plugin) {
        return (BungeeAPI)registeredInstances.compute(plugin, (k, v) -> {
            if (v == null) {
                v = new BungeeAPI(plugin);
            }

            return v;
        });
    }

    public BungeeAPI(Plugin plugin) {
        this.plugin = (Plugin) Objects.requireNonNull(plugin, "plugin cannot be null");
        this.callbackMap = new HashMap();
        synchronized(registeredInstances) {
            registeredInstances.compute(plugin, (k, oldInstance) -> {
                if (oldInstance != null) {
                    oldInstance.unregister();
                }

                return this;
            });
        }

        this.messageListener = this::onPluginMessageReceived;
        Messenger messenger = Bukkit.getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(plugin, "BungeeCord");
        messenger.registerIncomingPluginChannel(plugin, "BungeeCord", this.messageListener);
    }

    public void registerForwardListener(BungeeAPI.ForwardConsumer globalListener) {
        this.globalForwardListener = globalListener;
    }

    public void registerForwardListener(String channelName, BungeeAPI.ForwardConsumer listener) {
        if (this.forwardListeners == null) {
            this.forwardListeners = new HashMap();
        }

        synchronized(this.forwardListeners) {
            this.forwardListeners.put(channelName, listener);
        }
    }

    public CompletableFuture<Integer> getPlayerCount(String serverName) {
        Player player = this.getFirstPlayer();
        CompletableFuture<Integer> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("PlayerCount-" + serverName, this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("PlayerCount");
        output.writeUTF(serverName);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public CompletableFuture<List<String>> getPlayerList(String serverName) {
        Player player = this.getFirstPlayer();
        CompletableFuture<List<String>> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("PlayerList-" + serverName, this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("PlayerList");
        output.writeUTF(serverName);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public CompletableFuture<List<String>> getServers() {
        Player player = this.getFirstPlayer();
        CompletableFuture<List<String>> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("GetServers", this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("GetServers");
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public void connect(Player player, String serverName) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(serverName);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }

    public void connectOther(String playerName, String server) {
        Player player = this.getFirstPlayer();
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("ConnectOther");
        output.writeUTF(playerName);
        output.writeUTF(server);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }

    public CompletableFuture<InetSocketAddress> getIp(Player player) {
        CompletableFuture<InetSocketAddress> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("IP", this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("IP");
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public void sendMessage(String playerName, String message) {
        Player player = this.getFirstPlayer();
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Message");
        output.writeUTF(playerName);
        output.writeUTF(message);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }

    public CompletableFuture<String> getServer() {
        Player player = this.getFirstPlayer();
        CompletableFuture<String> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("GetServer", this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("GetServer");
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public CompletableFuture<String> getUUID(Player player) {
        CompletableFuture<String> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("UUID", this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("UUID");
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public CompletableFuture<String> getUUID(String playerName) {
        Player player = this.getFirstPlayer();
        CompletableFuture<String> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("UUIDOther-" + playerName, this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("UUIDOther");
        output.writeUTF(playerName);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public CompletableFuture<InetSocketAddress> getServerIp(String serverName) {
        Player player = this.getFirstPlayer();
        CompletableFuture<InetSocketAddress> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("ServerIP-" + serverName, this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("ServerIP");
        output.writeUTF(serverName);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
        return future;
    }

    public void kickPlayer(String playerName, String kickMessage) {
        Player player = this.getFirstPlayer();
        CompletableFuture<InetSocketAddress> future = new CompletableFuture();
        synchronized(this.callbackMap) {
            this.callbackMap.compute("KickPlayer", this.computeQueueValue(future));
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("KickPlayer");
        output.writeUTF(playerName);
        output.writeUTF(kickMessage);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }

    public void forward(String server, String channelName, byte[] data) {
        Player player = this.getFirstPlayer();
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Forward");
        output.writeUTF(server);
        output.writeUTF(channelName);
        output.writeShort(data.length);
        output.write(data);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }

    public void forwardToPlayer(String playerName, String channelName, byte[] data) {
        Player player = this.getFirstPlayer();
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("ForwardToPlayer");
        output.writeUTF(playerName);
        output.writeUTF(channelName);
        output.writeShort(data.length);
        output.write(data);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }

    private void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equalsIgnoreCase("BungeeCord")) {
            ByteArrayDataInput input = ByteStreams.newDataInput(message);
            String subchannel = input.readUTF();
            synchronized(this.callbackMap) {
                Queue callbacks;
                if (!subchannel.equals("PlayerCount") && !subchannel.equals("PlayerList") && !subchannel.equals("UUIDOther") && !subchannel.equals("ServerIP")) {
                    callbacks = (Queue)this.callbackMap.get(subchannel);
                    if (callbacks == null) {
                        short dataLength = input.readShort();
                        byte[] data = new byte[dataLength];
                        input.readFully(data);
                        if (this.globalForwardListener != null) {
                            this.globalForwardListener.accept(subchannel, player, data);
                        }

                        if (this.forwardListeners != null) {
                            synchronized(this.forwardListeners) {
                                BungeeAPI.ForwardConsumer listener = (BungeeAPI.ForwardConsumer)this.forwardListeners.get(subchannel);
                                if (listener != null) {
                                    listener.accept(subchannel, player, data);
                                }
                            }
                        }

                    } else if (!callbacks.isEmpty()) {
                        CompletableFuture callback = (CompletableFuture)callbacks.poll();

                        try {
                            byte var10 = -1;
                            switch(subchannel.hashCode()) {
                                case -1500810727:
                                    if (subchannel.equals("GetServer")) {
                                        var10 = 1;
                                    }
                                    break;
                                case 2343:
                                    if (subchannel.equals("IP")) {
                                        var10 = 3;
                                    }
                                    break;
                                case 2616251:
                                    if (subchannel.equals("UUID")) {
                                        var10 = 2;
                                    }
                                    break;
                                case 719507834:
                                    if (subchannel.equals("GetServers")) {
                                        var10 = 0;
                                    }
                            }

                            switch(var10) {
                                case 0:
                                    callback.complete(Arrays.asList(input.readUTF().split(", ")));
                                    break;
                                case 1:
                                case 2:
                                    callback.complete(input.readUTF());
                                    break;
                                case 3:
                                    String ip = input.readUTF();
                                    int port = input.readInt();
                                    callback.complete(new InetSocketAddress(ip, port));
                            }
                        } catch (Exception var17) {
                            callback.completeExceptionally(var17);
                        }

                    }
                } else {
                    String identifier = input.readUTF();
                    callbacks = (Queue)this.callbackMap.get(subchannel + "-" + identifier);
                    if (callbacks != null && !callbacks.isEmpty()) {
                        CompletableFuture callback = (CompletableFuture)callbacks.poll();

                        try {
                            byte var11 = -1;
                            switch(subchannel.hashCode()) {
                                case -2095967602:
                                    if (subchannel.equals("PlayerCount")) {
                                        var11 = 0;
                                    }
                                    break;
                                case -205896897:
                                    if (subchannel.equals("PlayerList")) {
                                        var11 = 1;
                                    }
                                    break;
                                case 1186775061:
                                    if (subchannel.equals("UUIDOther")) {
                                        var11 = 2;
                                    }
                                    break;
                                case 1443747786:
                                    if (subchannel.equals("ServerIP")) {
                                        var11 = 3;
                                    }
                            }

                            switch(var11) {
                                case 0:
                                    callback.complete(input.readInt());
                                    break;
                                case 1:
                                    callback.complete(Arrays.asList(input.readUTF().split(", ")));
                                    break;
                                case 2:
                                    callback.complete(input.readUTF());
                                    break;
                                case 3:
                                    String ip = input.readUTF();
                                    int port = input.readUnsignedShort();
                                    callback.complete(new InetSocketAddress(ip, port));
                            }
                        } catch (Exception var18) {
                            callback.completeExceptionally(var18);
                        }

                    }
                }
            }
        }
    }

    public void unregister() {
        Messenger messenger = Bukkit.getServer().getMessenger();
        messenger.unregisterIncomingPluginChannel(this.plugin, "BungeeCord", this.messageListener);
        messenger.unregisterOutgoingPluginChannel(this.plugin);
        this.callbackMap.clear();
    }

    private BiFunction<String, Queue<CompletableFuture<?>>, Queue<CompletableFuture<?>>> computeQueueValue(CompletableFuture<?> queueValue) {
        return (key, value) -> {
            if (value == null) {
                value = new ArrayDeque();
            }

            ((Queue)value).add(queueValue);
            return (Queue)value;
        };
    }

    private Player getFirstPlayer() {
        Player firstPlayer = this.getFirstPlayer0(Bukkit.getOnlinePlayers());
        if (firstPlayer == null) {
            throw new IllegalArgumentException("Bungee Messaging Api requires at least one player to be online.");
        } else {
            return firstPlayer;
        }
    }

    private Player getFirstPlayer0(Player[] playerArray) {
        return playerArray.length > 0 ? playerArray[0] : null;
    }

    private Player getFirstPlayer0(Collection<? extends Player> playerCollection) {
        return (Player) Iterables.getFirst(playerCollection, (Object)null);
    }

    @FunctionalInterface
    public interface ForwardConsumer {
        void accept(String var1, Player var2, byte[] var3);
    }
}
