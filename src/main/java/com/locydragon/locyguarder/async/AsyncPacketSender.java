package com.locydragon.locyguarder.async;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.locydragon.locyguarder.Bubble;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class AsyncPacketSender extends Thread {
    public boolean stop = false;
    public Player target = null;
    public WrappedGameProfile profile;
    public static Class gameProfile = null;
    public static String version = null;
    public static Class<?> WORLDTYPE = null;
    public static Class<?> GAMEMODE = null;
    public static Object survive = null;
    public static Class<?> DIFFICULTY = null;
    public static Object peaceful = null;

    public static Class<?> bukkitITEM = null;
    public static Class<?> nmsITEM = null;

    static {
        try {
            gameProfile = Class.forName("com.mojang.authlib.GameProfile");
            version = org.bukkit.Bukkit.getServer().getClass()
                    .getPackage().getName().replace(".", ",").split(",")[3];
            WORLDTYPE = Class.forName("net.minecraft.server." + version + ".WorldType");
            GAMEMODE = Class.forName("net.minecraft.server." + version + ".EnumGamemode");
            for (Object obj : GAMEMODE.getEnumConstants()) {
                if (obj.toString().equals("SURVIVAL")) {
                    survive = obj;
                    break;
                }
            }
            //EnumDifficulty
            DIFFICULTY = Class.forName("net.minecraft.server." + version + ".EnumDifficulty");
            peaceful = DIFFICULTY.getEnumConstants()[0];
            bukkitITEM = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AsyncPacketSender(Player who, WrappedGameProfile profile) {
        this.target = who;
        this.profile = profile;
    }

    @Override
    public void run() {
        PacketContainer container_SETCOMP = new PacketContainer(PacketType.Login.Server.SET_COMPRESSION);
        container_SETCOMP.getIntegers().write(0, -256);
        PacketContainer container_SUCCESS
                = new PacketContainer(PacketType.Login.Server.SUCCESS);
        if (Bukkit.getOfflinePlayer(this.profile.getName()) != null) {
            container_SUCCESS.getGameProfiles().write(0
                    , WrappedGameProfile.fromOfflinePlayer(Bukkit.getOfflinePlayer(this.profile.getName())));
        } else {
            try {
                container_SUCCESS.getModifier().write(0, gameProfile.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), this.profile.getName()));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        PacketContainer container_SPAWN_POSITION
                = new PacketContainer(PacketType.Play.Server.SPAWN_POSITION);
        container_SPAWN_POSITION.getBlockPositionModifier().write(0, new BlockPosition(0,0,0));
        PacketContainer container_POSITION
                = new PacketContainer(PacketType.Play.Server.POSITION);
        container_POSITION.getDoubles().write(0, 0.0);
        container_POSITION.getDoubles().write(1, 0.0);
        container_POSITION.getDoubles().write(2, 0.0);
        container_POSITION.getFloat().write(0, 0F);
        container_POSITION.getFloat().write(1, 0F);
        container_POSITION.getModifier().write(5,  new HashSet<>());

        PacketContainer container_LOGIN = new PacketContainer(PacketType.Play.Server.LOGIN);
        try {
            container_LOGIN.getModifier().write(0, 0).write(1, true)
                    .write(2, survive).write(3, 0)
                    .write(4, peaceful)
                    .write(5, 0)
                    .write(6, WORLDTYPE.getField("NORMAL").get(null))
                    .write(7, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        PacketContainer container_Item = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        container_Item.getIntegers().write(0, 0).write(1, 42);
        container_Item.getItemModifier().write(0, new ItemStack(Material.DIAMOND));
        try {
            Bubble.manager.sendServerPacket(this.target, container_SETCOMP);
            Bubble.manager.sendServerPacket(this.target, container_SUCCESS);
            Bubble.manager.sendServerPacket(this.target, container_LOGIN);
            Bubble.manager.sendServerPacket(this.target, container_SPAWN_POSITION);
            Bubble.manager.sendServerPacket(this.target, container_POSITION);
            Bubble.manager.sendServerPacket(this.target, container_Item);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Object toNMSItemStack(ItemStack item) {
        try {
            return bukkitITEM.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
