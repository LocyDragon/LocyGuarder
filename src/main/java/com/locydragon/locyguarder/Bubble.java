package com.locydragon.locyguarder;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.locydragon.locyguarder.commands.BubbleCommands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Bubble extends JavaPlugin {
    public static ProtocolManager manager;
    public static Bubble instance;
    public static List<String> safePlayers = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        safePlayers = getConfig().getStringList("LegalPlayers");
        Bukkit.getPluginCommand("bubble").setExecutor(new BubbleCommands());
        instance = this;
        manager = ProtocolLibrary.getProtocolManager();
        ProtocolListenerAdder.setUpPackets();
        ProtocolListenerAdder.addListener();
    }
}
