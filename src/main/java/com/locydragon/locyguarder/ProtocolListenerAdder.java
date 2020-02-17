package com.locydragon.locyguarder;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.locydragon.locyguarder.async.AsyncPacketSender;
import com.locydragon.locyguarder.util.TemporaryPlayer;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProtocolListenerAdder {
    static PacketType[] types;
    public static Executor executor = Executors.newCachedThreadPool();
    public static Vector<InetSocketAddress> addresses = new Vector<>();

    public static ConcurrentHashMap<InetSocketAddress,String> code = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<InetSocketAddress,String> name = new ConcurrentHashMap<>();

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
                            name.put(e.getPlayer().getAddress(), e.getPacket().
                                    getGameProfiles().read(0).getName().trim());
                            addresses.add(e.getPlayer().getAddress());
                            AsyncPacketSender sender
                                    = new AsyncPacketSender(e.getPlayer(), e.getPacket().getGameProfiles().read(0));
                            executor.execute(sender);
                            e.setCancelled(true);
                            return;
                        } else if (e.getPacketType() == PacketType.Play.Client.CHAT
                                && containsAddress(e.getPlayer().getAddress())) {
                            String obj = code.get(e.getPlayer().getAddress());
                            StringBuffer sb = new StringBuffer();
                            sb.append(obj.charAt(2)).append(obj.charAt(3));
                            if (e.getPacket().getStrings().read(0).equalsIgnoreCase(sb.toString())) {
                                TemporaryPlayer.kickPlayer(e.getPlayer(), Bubble.success);
                                Bubble.addSafePlayer(name.get(e.getPlayer().getAddress()));
                                remove(e.getPlayer());
                                return;
                            } else {
                                TemporaryPlayer.kickPlayer(e.getPlayer(), Bubble.failed);
                                remove(e.getPlayer());
                                return;
                            }
                        }
                        if (containsAddress(e.getPlayer().getAddress())) {
                            e.setCancelled(true);
                        }
                    }

                    @Override
                    public void onPacketSending(PacketEvent e) {
                        {
                        }
                    }
        });
    }
}
