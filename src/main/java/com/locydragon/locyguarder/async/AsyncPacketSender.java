package com.locydragon.locyguarder.async;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.locydragon.locyguarder.Bubble;
import com.locydragon.locyguarder.ProtocolListenerAdder;
import com.locydragon.locyguarder.util.PictureRender;
import com.locydragon.locyguarder.util.TemporaryPlayer;
import com.locydragon.locyguarder.util.VerifyCode;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    public static Class<?> mapObs = null;
    public static Class<?> nmsIcon = null;
    public static Class<?> craftPlayer = null;

    public static Class<?> entityPlayer = null;
    public static Class<?> craftWorld = null;
    public static Class<?> craftServer = null;
    public static Class<?> worldServer = null;
    public static Class<?> playerInteractManager = null;
    public static Class<?> minecraftServer = null;
    public static Class<?> world = null;

    public static boolean offhand = false;

    static Object fakePlayer = null;
    static boolean isOK = false;

    static {
        try {
            gameProfile = Class.forName("com.mojang.authlib.GameProfile");
            version = org.bukkit.Bukkit.getServer().getClass()
                    .getPackage().getName().replace(".", ",").split(",")[3];
            WORLDTYPE = Class.forName("net.minecraft.server." + version + ".WorldType");
            try {
                GAMEMODE = Class.forName("net.minecraft.server." + version + ".EnumGamemode");
            } catch (ClassNotFoundException e) {
                GAMEMODE = Class.forName("net.minecraft.server." + version + ".WorldSettings$EnumGamemode");
            }
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
            mapObs = Class.forName("org.bukkit.craftbukkit." + version + ".map.CraftMapView");
            nmsIcon = Class.forName("net.minecraft.server." + version + ".MapIcon");
            craftPlayer = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");

            entityPlayer = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
            craftWorld = Class.forName("org.bukkit.craftbukkit." + version + ".CraftWorld");
            craftServer = Class.forName("org.bukkit.craftbukkit." + version + ".CraftServer");
            worldServer = Class.forName("net.minecraft.server." + version + ".WorldServer");
            playerInteractManager = Class.forName("net.minecraft.server." + version + ".PlayerInteractManager");
            minecraftServer = Class.forName("net.minecraft.server." + version + ".MinecraftServer");
            world = Class.forName("net.minecraft.server." + version + ".World");

            Object cw = craftWorld.cast(Bukkit.getWorlds().get(0));
            Object ws = craftWorld.getMethod("getHandle").invoke(cw);
            Object cs = craftServer.cast(Bukkit.getServer());
            Object manager = null;
            try {
                manager = playerInteractManager.getConstructor(worldServer).newInstance(ws);
            } catch (NoSuchMethodException e) {
                manager = playerInteractManager.getConstructor(world).newInstance(worldServer.getMethod("b").invoke(ws));
            }
            Object eP = entityPlayer.getConstructor(minecraftServer, worldServer
                    , Class.forName("com.mojang.authlib.GameProfile"), playerInteractManager)
                    .newInstance(craftServer.getMethod("getServer").invoke(cs)
                            , ws, Class.forName("com.mojang.authlib.GameProfile").getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), "BubbleXP"), manager);

            fakePlayer = craftPlayer.getConstructors()[0].newInstance(Bukkit.getServer(), eP);

            try {
                Class.forName("org.bukkit.inventory.MainHand");
                offhand = true;
            } catch (ClassNotFoundException e) {
                offhand = false;
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
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

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PacketContainer container_Held = new PacketContainer(PacketType.Play.Server.HELD_ITEM_SLOT);
        container_Held.getIntegers().write(0, 8);

        ItemStack mapItem = new ItemStack(Material.MAP);
        PacketContainer container_Item = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        if (offhand) {
            container_Item.getIntegers().write(0, 0).write(1, 45);
        } else {
            container_Item.getIntegers().write(0, 0).write(1, 44);
        }
        container_Item.getItemModifier().write(0, mapItem);
        try {
            Bubble.manager.sendServerPacket(this.target, container_SETCOMP);
            Bubble.manager.sendServerPacket(this.target, container_SUCCESS);
            Bubble.manager.sendServerPacket(this.target, container_LOGIN);
            Bubble.manager.sendServerPacket(this.target, container_SPAWN_POSITION);
            Bubble.manager.sendServerPacket(this.target, container_POSITION);
            Bubble.manager.sendServerPacket(this.target, container_Held);
            Bubble.manager.sendServerPacket(this.target, container_Item);
            MapView view = Bukkit.getMap(mapItem.getDurability());
            for (MapRenderer renderer : view.getRenderers()) {
                view.removeRenderer(renderer);
            }
            view.setScale(MapView.Scale.FARTHEST);

            VerifyCode verifyCode = new VerifyCode();
            view.addRenderer(new PictureRender(verifyCode.getImage(), this));
            ProtocolListenerAdder.code.put(this.target.getAddress(), verifyCode.getText());

            PacketContainer map = new PacketContainer(PacketType.Play.Server.MAP);
            map.getIntegers().write(0, (int)view.getId()).write(1, 30).write(2, 30)
                    .write(3, 64).write(4, 64);
            map.getBytes().write(0, view.getScale().getValue());
            Collection icons = new ArrayList();
            Object craftMapView = mapObs.cast(view);
            try {
                Object RenderData = mapObs.getMethod("render", craftPlayer).invoke(craftMapView
                            , fakePlayer);
                try {
                    map.getByteArrays().write(0
                            , live((byte[])RenderData.getClass().getField("buffer").get(RenderData),64, 64, 64, 64));
                    for (MapCursor cursor : (Collection<MapCursor>)RenderData
                            .getClass().getField("cursors").get(RenderData)) {
                        if (cursor.isVisible()) {
                            try {
                                icons.add(nmsIcon.getConstructor(byte.class, byte.class, byte.class, byte.class)
                                        .newInstance(cursor.getRawType(), cursor.getX(), cursor.getY(), cursor.getDirection()));
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Object[] obj = (Object[])Array.newInstance(nmsIcon, icons.size());
                    try {
                        map.getModifier().write(2, icons.toArray(obj));
                    } catch (IllegalArgumentException e) {
                        map.getModifier().write(3, icons.toArray(obj));
                    }
                    if (map.getBooleans().size() > 0) {
                        map.getBooleans().write(0, true);
                    }
                    Bubble.manager.sendServerPacket(this.target, map);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        for (String line : Bubble.info) {
            TemporaryPlayer.sendMsgReal(this.target, line);
        }
        try {
            Thread.sleep(Bubble.time * 1000);
            if (this.target != null) {
                TemporaryPlayer.kickPlayer(this.target, Bubble.kickMsg);
                ProtocolListenerAdder.remove(this.target);
            }
        } catch (InterruptedException e) {
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

    /*
    这里的数字试了好几天 淦
     */
    public static byte[] live(byte[] before,int a, int b, int c, int d) {
        byte[] h = new byte[a * b];
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                h[(i + j * a)] = before[(c + i + (d + j) * 128)];
            }
        }
        return h;
    }
}

