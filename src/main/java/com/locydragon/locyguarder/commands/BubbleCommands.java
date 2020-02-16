package com.locydragon.locyguarder.commands;
import	java.nio.channels.FileChannel.MapMode;

import com.locydragon.locyguarder.ProtocolListenerAdder;
import com.locydragon.locyguarder.util.PictureRender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.io.File;

public class BubbleCommands implements CommandExecutor  {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.isOp()) {
            return false;
        }
        if (args[0].equalsIgnoreCase("login") && args.length == 2) {
            String target = args[1];
            if (ProtocolListenerAdder.login(target)) {
                sender.sendMessage(ChatColor.WHITE + "" +
                        ChatColor.UNDERLINE + "[泡沫防御]成功帮助 " + target + " 登入服务器.");
            } else {
                sender.sendMessage(ChatColor.WHITE + "" +
                        ChatColor.UNDERLINE + "[泡沫防御]错误，不存在这个玩家，请注意大小写……");
            }
        } else {
            sender.sendMessage(ChatColor.WHITE + "" +
                    ChatColor.UNDERLINE + "[泡沫防御]使用: /bubble login [玩家名] —帮助一个玩家登入服务器.");
        }
        return false;
    }
}
