package com.locydragon.locyguarder;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.locydragon.locyguarder.commands.BubbleCommands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Bubble extends JavaPlugin {
    public static ProtocolManager managerPL;
    public static Manager manager = new Manager();
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
        if (time >= 18) {
            time = 18;
        }
        kickMsg = getConfig().getString("KickOut");
        success = getConfig().getString("Success");
        failed = getConfig().getString("Failed");
        Bukkit.getPluginCommand("bubble").setExecutor(new BubbleCommands());
        instance = this;
        managerPL = ProtocolLibrary.getProtocolManager();
        ProtocolListenerAdder.setUpPackets();
        ProtocolListenerAdder.addListener();
        Bukkit.getLogger().info("=========================");
        Bukkit.getLogger().info("欢迎使用Bubble反压测！！！");
        Bukkit.getLogger().info("作者：绿毛");
        Bukkit.getLogger().info("禁止白嫖~！快去评分~！");
        Bukkit.getLogger().info("若插件运行不正常，请将下列信息截图:");
        StringBuilder sb = new StringBuilder();
        for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
            if (pl.getDescription().getDepend().contains("ProtocolLib")) {
                sb.append(pl.getName() + "|");
            }
        }
        Bukkit.getLogger().info(sb.toString());
        Bukkit.getLogger().info("=========================");
    }

    public static void addSafePlayer(String who) {
        safePlayers.add(who);
        instance.getConfig().set("LegalPlayers", safePlayers);
        instance.saveConfig();
    }
}
