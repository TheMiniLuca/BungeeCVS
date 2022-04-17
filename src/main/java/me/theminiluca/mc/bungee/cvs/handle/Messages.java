package me.theminiluca.mc.bungee.cvs.handle;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public class Messages {

    public static Properties properties;

    public Messages(Plugin plugin) {
        File def = new File(plugin.getDataFolder().toString());
        File messagesFile = new File(plugin.getDataFolder() + "\\messages.properties");
        if (!def.exists()) def.mkdir();
        properties = new Properties();
        try {
            properties.load(new FileInputStream(messagesFile.toString()));
        } catch (IOException e) {
            if (!messagesFile.exists()) {
                InputStream in_data = plugin.getResource("translations/messages.properties");
                if (in_data != null) {
                    try {
                        Files.copy(in_data, messagesFile.toPath());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            for (PropertiesKey property : PropertiesKey.values()) {
                properties.setProperty(property.property(), property.defaults());
            }
            try {
                properties.store(new FileOutputStream(messagesFile.toString()), null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }



    public static String getProperties(PropertiesKey key)  {
        String value = properties.getProperty(key.property());
        if (value == null || value.isEmpty()) return "";
        value = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return ChatColor.translateAlternateColorCodes('&', value);
    }
    public static boolean isProperties(PropertiesKey key)  {
        if (key.aClass().equals(Boolean.class)) {
            String value = properties.getProperty(key.property());
            if (value == null || value.isEmpty()) return false;
            value = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            return Boolean.parseBoolean(value);
        }
        throw new IllegalArgumentException();
    }

    public enum PropertiesKey {
        CONNECTION_REFUSED("server.connection.refused", "&c connection refused."),
        CONNECTION_READY_MESSAGE("server.connection.ready", "&asending you to %server%!"),
        PLUGIN_RELOAD("plugin.reload", "&asuccessfully reload to the bungeeCVS"),
        PLUGIN_RELOAD_MESSAGES_ENABLE("plugin.reload.message.enable", true),
        CONNECTION_SUCCESS("server.connection.success", "&aSuccessfully connected to the server!");

        private final String property;
        private final String defaults;
        private final Class<?> clazz;

        PropertiesKey(String property, String defaults) {
            this.property = property;
            this.defaults = defaults;
            this.clazz = String.class;
        }
        PropertiesKey(String property, boolean defaults) {
            this.property = property;
            this.defaults = String.valueOf(defaults);
            this.clazz = Boolean.class;
        }



        public String property() {
            return property;
        }

        public String defaults() {
            return defaults;
        }

        public Class<?> aClass() {
            return clazz;
        }
    }
}
