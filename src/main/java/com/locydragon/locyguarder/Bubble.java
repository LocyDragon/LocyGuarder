package com.locydragon.locyguarder;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.locydragon.locyguarder.commands.BubbleCommands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Bubble extends JavaPlugin {
    public static ProtocolManager manager;
    public static Bubble instance;
    public static List<String> safePlayers = new ArrayList<>();
    public static int time = 7;
    public static String kickMsg = "";
    public static List<String> info = new ArrayList<>();
    public static String success;
    public static String failed;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        safePlayers = getConfig().getStringList("LegalPlayers");
        info = getConfig().getStringList("Messages");
        time = getConfig().getInt("Time");
        kickMsg = getConfig().getString("KickOut");
        success = getConfig().getString("Success");
        failed = getConfig().getString("Failed");
        Bukkit.getPluginCommand("bubble").setExecutor(new BubbleCommands());
        instance = this;
        manager = ProtocolLibrary.getProtocolManager();
        ProtocolListenerAdder.setUpPackets();
        ProtocolListenerAdder.addListener();
        Bukkit.getLogger().info("=========================");
        Bukkit.getLogger().info("欢迎使用Bubble反压测！！！");
        Bukkit.getLogger().info("作者：绿毛");
        Bukkit.getLogger().info("禁止白嫖~！快去评分~！");
        Bukkit.getLogger().info("=========================");
    }

    public static void addSafePlayer(String who) {
        safePlayers.add(who);
        instance.getConfig().set("LegalPlayers", who);
        instance.saveConfig();
    }
}
