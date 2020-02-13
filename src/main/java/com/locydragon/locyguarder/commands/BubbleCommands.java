package com.locydragon.locyguarder.commands;

import com.locydragon.locyguarder.ProtocolListenerAdder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
