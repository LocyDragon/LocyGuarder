package com.locydragon.locyguarder;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class Manager {
    public void sendServerPacket(Player who, PacketContainer container) throws InvocationTargetException {
        Bubble.managerPL.sendServerPacket(who, container);
    }
}
