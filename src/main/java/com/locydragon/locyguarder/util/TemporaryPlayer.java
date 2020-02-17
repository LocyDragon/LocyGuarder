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
import java.util.List;

public class TemporaryPlayer {
    static Class<?> craftChat = null;
    static Class<?> chat = null;
    static Object chatObj = null;

    static {
        try {
            craftChat = Class.forName("org.bukkit.craftbukkit." + AsyncPacketSender.version + ".util.CraftChatMessage");
            try {
                chat = Class.forName("net.minecraft.server." + AsyncPacketSender.version + ".ChatMessageType");
                for (Object obj : chat.getEnumConstants()) {
                    if (obj.toString().equals("CHAT")) {
                        chatObj = obj;
                        break;
                    }
                }
            } catch (ClassNotFoundException e) {}
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
                if (chatObj == null) {
                    message.getBytes().write(0, (byte) 1);
                } else {
                    message.getModifier().write(2, chatObj);
                }
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
