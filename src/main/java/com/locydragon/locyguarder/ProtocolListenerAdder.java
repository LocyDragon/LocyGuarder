package com.locydragon.locyguarder;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.locydragon.locyguarder.async.AsyncPacketSender;
import com.locydragon.locyguarder.util.TemporaryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProtocolListenerAdder {
    public static List<PacketType> packets = new ArrayList<>();

    static PacketType[] types;
    public static Executor executor = Executors.newCachedThreadPool();
    public static Vector<InetSocketAddress> addresses = new Vector<>();

    public static ConcurrentHashMap<InetSocketAddress,String> code = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<InetSocketAddress,String> name = new ConcurrentHashMap<>();

    static {
        packets.add(PacketType.Login.Server.SET_COMPRESSION);
        packets.add(PacketType.Login.Server.SUCCESS);
        packets.add(PacketType.Play.Server.SPAWN_POSITION);
        packets.add(PacketType.Play.Server.POSITION);
        packets.add(PacketType.Play.Server.LOGIN);
        packets.add(PacketType.Play.Server.HELD_ITEM_SLOT);
        packets.add(PacketType.Play.Server.SET_SLOT);
        packets.add(PacketType.Play.Server.MAP);
        packets.add(PacketType.Play.Server.KICK_DISCONNECT);
        packets.add(PacketType.Play.Server.CHAT);
    }

    public static void setUpPackets() {
        List<PacketType> typeList = new ArrayList<>();
        PacketType.values().forEach(x -> typeList.add(x));
        types = typeList.toArray(new PacketType[typeList.size()]);
    }

    public static void remove(Player target) {
        addresses.remove(target.getAddress());
        code.remove(target.getAddress());
        name.remove(target.getAddress());
    }

    public static boolean containsAddress(InetSocketAddress address) {
       return addresses.contains(address);
    }

    public static void addListener() {
        Bubble.managerPL
                .addPacketListener(new PacketAdapter(PacketAdapter.params()
                .plugin(Bubble.instance)
                .clientSide()
                .serverSide()
                .optionAsync()
                .listenerPriority(ListenerPriority.MONITOR)
                .gamePhase(GamePhase.BOTH)
                .options(ListenerOptions.SKIP_PLUGIN_VERIFIER)
                .types(types)){


                    @Override
                    public void onPacketReceiving(PacketEvent e) {
                        PacketType type = e.getPacketType();
                        e.setReadOnly(false);
                        if (type == PacketType.Login.Client.START) {
                            if (Bubble.safePlayers.contains(e.getPacket().
                                    getGameProfiles().read(0).getName().trim())) {
                                return;
                            }
                            name.put(e.getPlayer().getAddress(), e.getPacket().
                                    getGameProfiles().read(0).getName().trim());
                            addresses.add(e.getPlayer().getAddress());
                            AsyncPacketSender sender
                                    = new AsyncPacketSender(e.getPlayer(), e.getPacket().getGameProfiles().read(0), null);
                            executor.execute(sender);
                            e.setCancelled(true);
                            return;
                        } else if (e.getPacketType() == PacketType.Play.Client.CHAT
                                && containsAddress(e.getPlayer().getAddress())) {
                            String obj = code.get(e.getPlayer().getAddress());
                            StringBuffer sb = new StringBuffer();
                            sb.append(obj.charAt(2)).append(obj.charAt(3));
                            if (e.getPacket().getStrings().read(0).equalsIgnoreCase(sb.toString())) {
                                e.setReadOnly(false);
                                e.setCancelled(true);
                                executor.execute(() -> {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                    TemporaryPlayer.kickPlayer(e.getPlayer(), Bubble.success);
                                });
                                Bubble.addSafePlayer(name.get(e.getPlayer().getAddress()));
                                remove(e.getPlayer());
                                return;
                            } else {
                                e.setReadOnly(false);
                                e.setCancelled(true);
                                executor.execute(() -> {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                    TemporaryPlayer.kickPlayer(e.getPlayer(), Bubble.failed);
                                });
                                remove(e.getPlayer());
                                return;
                            }
                        }
                        if (containsAddress(e.getPlayer().getAddress())) {
                            e.setReadOnly(false);
                            e.setCancelled(true);
                        }
                    }

                    @Override
                    public void onPacketSending(PacketEvent e) {
                        if (!(containsAddress(e.getPlayer().getAddress()))) {
                            return;
                        }
                        if (e.getPacketType() == PacketType.Login.Server.SET_COMPRESSION
                                && e.getPacket().getIntegers().read(0) != -256) {
                            e.setReadOnly(false);
                            e.setCancelled(true);
                            return;
                        }
                        if (e.getPacketType() == PacketType.Play.Server.SPAWN_POSITION
                        && !(e.getPacket().getBlockPositionModifier().read(0).getX() == 0)) {
                            e.setReadOnly(false);
                            e.setCancelled(true);
                            return;
                        }
                        if (e.getPacketType() == PacketType.Play.Server.POSITION
                        && !(e.getPacket().getDoubles().read(0) == 0.0)) {
                            e.setReadOnly(false);
                            e.setCancelled(true);
                            return;
                        }
                        if (e.getPacketType() == PacketType.Play.Server.LOGIN
                        && !((int)e.getPacket().getModifier().read(0) == 0)) {
                            e.setReadOnly(false);
                            e.setCancelled(true);
                            return;
                        }
                        if (!packets.contains(e.getPacketType())) {
                            e.setReadOnly(false);
                            e.setCancelled(true);
                            return;
                        }
                    }
        });
    }

    public static boolean contains(PacketType e) {
        for (PacketType t : packets) {
            if (e == t || e.equals(t)) {
                return true;
            }
        }
        return false;
    }
}
