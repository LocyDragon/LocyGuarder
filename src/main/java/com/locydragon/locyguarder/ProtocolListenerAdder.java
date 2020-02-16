package com.locydragon.locyguarder;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.locydragon.locyguarder.async.AsyncPacketSender;
import com.locydragon.locyguarder.util.TemporaryPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProtocolListenerAdder {
    static PacketType[] types;
    public static Executor executor = Executors.newCachedThreadPool();
    public static ConcurrentHashMap<String,InetSocketAddress> monitorPlayers = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Player> unLoginPlayers = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, PacketContainer> loginPacket = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<InetSocketAddress,String> code = new ConcurrentHashMap<>();

    public static boolean contains(String name) {
        for (String obj : unLoginPlayers.keySet()) {
            System.out.println(obj);
            if (obj.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean login(String name) {
        if (!contains(name)) {
            return false;
        }
        monitorPlayers.remove(name);
        Player target = unLoginPlayers.get(name);
        unLoginPlayers.remove(name);
        PacketContainer packet = loginPacket.get(name);
        loginPacket.remove(name);
        try {
            Bubble.manager.recieveClientPacket(target, packet);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void setUpPackets() {
        List<PacketType> typeList = new ArrayList<>();
        PacketType.values().forEach(x -> typeList.add(x));
        types = typeList.toArray(new PacketType[typeList.size()]);
    }

    public static boolean containsAddress(InetSocketAddress address) {
        for (InetSocketAddress add : monitorPlayers.values()) {
            if (add.equals(address)) {
                return true;
            }
        }
        return false;
    }

    public static void addListener() {
        Bubble.manager
                .addPacketListener(new PacketAdapter(PacketAdapter.params()
                .plugin(Bubble.instance)
                .clientSide()
                .serverSide()
                .listenerPriority(ListenerPriority.MONITOR)
                .gamePhase(GamePhase.BOTH)
                .optionAsync()
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
                            monitorPlayers.put(e.getPacket().getGameProfiles().read(0).getName().trim()
                                    , e.getPlayer().getAddress());
                            unLoginPlayers.put(e.getPacket().getGameProfiles().read(0).getName().trim()
                                    , e.getPlayer());
                            loginPacket.put(e.getPacket().getGameProfiles()
                                    .read(0).getName().trim(), e.getPacket());
                            AsyncPacketSender sender
                                    = new AsyncPacketSender(e.getPlayer(), e.getPacket().getGameProfiles().read(0));
                            executor.execute(sender);
                            e.setCancelled(true);
                            return;
                        }
                        if (containsAddress(e.getPlayer().getAddress())) {
                            e.setCancelled(true);
                        }
                    }

                    @Override
                    public void onPacketSending(PacketEvent e) {
                        if (containsAddress(e.getPlayer().getAddress())) {
                            e.setReadOnly(false);
                            if (e.getPacketType() == PacketType.Play.Client.CHAT) {
                                String obj = code.get(e.getPlayer().getAddress());
                                StringBuffer sb = new StringBuffer();
                                sb.append(obj.charAt(2)).append(obj.charAt(3));
                                if (e.getPacket().getStrings().read(0).equalsIgnoreCase(sb.toString())) {
                                    TemporaryPlayer.kickPlayer(e.getPlayer(), Bubble.success);
                                } else {
                                    TemporaryPlayer.kickPlayer(e.getPlayer(), Bubble.failed);
                                }
                            }
                            e.setCancelled(true);
                        }
                    }
        });
    }
}
