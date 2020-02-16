package com.locydragon.locyguarder.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.locydragon.locyguarder.Bubble;
import com.locydragon.locyguarder.async.AsyncPacketSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TemporaryPlayer {
    static Class<?> craftChat = null;

    static {
        try {
            craftChat = Class.forName("org.bukkit.craftbukkit." + AsyncPacketSender.version + ".util.CraftChatMessage");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void kickPlayer(Player temp, String reason) {
        PacketContainer kick = new PacketContainer(PacketType.Play.Server.KICK_DISCONNECT);
        kick.getChatComponents().write(0,
                WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', reason)));
        try {
            Bubble.manager.sendServerPacket(temp, kick);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public static void sendMsgReal(Player temp, String msg) {
        try {
            for (Object obj : (Object[])craftChat.getMethod("fromString", String.class)
                    .invoke(null, ChatColor.translateAlternateColorCodes('&', msg))) {
                PacketContainer message = new PacketContainer(PacketType.Play.Server.CHAT);
                message.getModifier().write(0, obj);
                message.getBytes().write(0, (byte) 1);
                try {
                    Bubble.manager.sendServerPacket(temp, message);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMsg(Player temp, String string) {
        for (String obj : splitBySize(string, 35)) {
            sendMsg(temp, obj);
        }
    }


    public static List<String> splitBySize(String value, int length) {
        char[] cs = value.toCharArray();
        StringBuilder result = new StringBuilder();
        List<String> resultList = new ArrayList<String>();
        int index = 0;
        for (char c : cs) {
            index += String.valueOf(c).getBytes().length;
            if (index > length) {
                resultList.add(result.toString());
                result.delete(0, index - 1);
                index = 0;
            } else {
                result.append(c);
            }
        }
        return resultList;
    }
}
