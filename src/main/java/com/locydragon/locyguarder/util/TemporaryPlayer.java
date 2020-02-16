package com.locydragon.locyguarder.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public class TemporaryPlayer {
    public static Class<?> chat = Class.forName("net.minecraft.server.")

    public static void kickPlayer(Player temp, String reason) {
        PacketContainer kick = new PacketContainer(PacketType.Play.Server.KICK_DISCONNECT);

    }
}
