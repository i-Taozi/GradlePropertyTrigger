/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network.base;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.core.TickHandlerCore;

/* Implementation note: while this does implement Sharable this isn't technically sharable because it has a packet map.
 * However its fine provided that you only pass a single instance of this to forge, as this handles the double sides by
 * itself. */
@Sharable
public final class PacketHandler extends SimpleChannelInboundHandler<Packet> {
    /** A massive negative number. Close to {@link Integer#MIN_VALUE} to reduce the likelyhood of this being a valid dim
     * ID in another mod. */
    public static final int INVALID_DIM_ID = Integer.MIN_VALUE + 10;

    private static volatile int livePacketCount = 0;
    private static int lastSomethingPackets = 0;
    private static long lastDispMs = System.currentTimeMillis();
    private final Map<Side, Map<Integer, Queue<Packet>>> packetMap;

    public PacketHandler() {
        Map<Side, Map<Integer, Queue<Packet>>> map = Maps.newHashMap();
        for (Side side : Side.values()) {
            Map<Integer, Queue<Packet>> mp = new ConcurrentHashMap<>();
            map.put(side, mp);
        }
        packetMap = ImmutableMap.copyOf(map);
        TickHandlerCore.addPacketHandler(this);
    }

    public static int packetQueueSize() {
        long now = System.currentTimeMillis();
        if (now - lastDispMs > 800) {
            lastDispMs = now;
            lastSomethingPackets = livePacketCount;
            livePacketCount = 0;
        }
        return lastSomethingPackets;
    }

    public void tick(World world) {
        Packet packet = null;
        Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
        int dimId = world.provider.getDimension();
        Queue<Packet> queue = getQueue(side, dimId);
        // BCLog.logger.info("Ticking world " + dimId + " at " + side + " with " + queue.size() + " packets");
        while ((packet = queue.poll()) != null) {
            // Pass the packets player back to it (It was not accessible to subtypes)
            packet.applyData(world, packet.player);
        }
    }

    public void unload(World world) {
        Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
        int dimId = world.provider.getDimension();
        Queue<Packet> queue = getQueue(side, dimId);
        queue.clear();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        Side side = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
        if (packet.dimensionId == INVALID_DIM_ID) BCLog.logger.warn("Found a packet with an invalid dimension id! [read]");
        if (!packet.hasDoneByteStuff) throw new IllegalArgumentException("Tried to use a packet that hasn't read yet! THIS IS VERY BAD!");
        if (side != null) {
            getQueue(side, packet.dimensionId).add(packet);
            livePacketCount++;
        } else {
            BCLog.logger.error("Found a message without a side! THIS IS VERY BAD, MAJOR ERRORS COULD OCCOUR!");
        }
    }

    private Queue<Packet> getQueue(Side side, int dimId) {
        Map<Integer, Queue<Packet>> map = packetMap.get(side);
        if (!map.containsKey(dimId)) {
            map.put(dimId, Queues.<Packet> newConcurrentLinkedQueue());
        }
        return map.get(dimId);
    }
}
