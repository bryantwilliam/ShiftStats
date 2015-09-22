package com.gmail.gogobebe2.shiftstats;

import org.bukkit.plugin.java.JavaPlugin;

public class ShiftStats extends JavaPlugin {
    public static String CONFIG_HOSTNAME = "HostName";
    public static String CONFIG_PORT = "Port";
    public static String CONFIG_DATABASE = "Database";
    public static String CONFIG_USERNAME = "Username";
    public static String CONFIG_PASSWORD = "Password";

    @Override
    public void onEnable() {
        getLogger().info("Starting up " + this.getName() + ". If you need me to update this plugin, email at gogobebe2@gmail.com");
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling " + this.getName() + ". If you need me to update this plugin, email at gogobebe2@gmail.com");
    }
}
