/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.FMLOutboundHandler.OutboundTarget;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.IBuildCraftMod;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.network.base.PacketHandler;
import buildcraft.core.lib.utils.Utils;

@Deprecated
public class BuildCraftMod implements IBuildCraftMod {
    private static final Executor packetSender;

    public EnumMap<Side, FMLEmbeddedChannel> channels;
    protected Map<String, Property> options = Maps.newHashMap();

    abstract class SendRequest implements Runnable {
        final Packet packet;
        final BuildCraftMod source;

        SendRequest(Packet packet) {
            this.packet = packet;
            this.source = BuildCraftMod.this;
            if (packet.tempWorld == null) {
                NullPointerException npe = new NullPointerException("The packet's world was null! Cannot send this!");
                BCLog.logger.fatal("// Blame AlexIIL", npe);
                throw npe;
            }
            if (packet.dimensionId == PacketHandler.INVALID_DIM_ID) {
                IllegalArgumentException iae = new IllegalArgumentException("The packet had an invalid dimension ID! Cannot send this!");
                BCLog.logger.fatal("// Blame AlexIIL", iae);
                throw iae;
            }
        }

        @Override
        public final void run() {
            try {
                FMLEmbeddedChannel channel = source.channels.get(Side.SERVER);
                editAttributes(channel);
                channel.writeOutbound(packet);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        abstract void editAttributes(FMLEmbeddedChannel channel);
    }

    class PlayerSendRequest extends SendRequest {
        EntityPlayer player;

        PlayerSendRequest(Packet packet, EntityPlayer player) {
            super(packet);
            this.player = player;
        }

        @Override
        void editAttributes(FMLEmbeddedChannel channel) {
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.PLAYER);
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        }
    }

    class EntitySendRequest extends LocationSendRequest {
        Entity entity;

        EntitySendRequest(Packet packet, Entity entity, int maxDistance) {
            super(packet, entity.worldObj.provider.getDimension(), Utils.getVec(entity), maxDistance);
            this.entity = entity;
        }
    }

    class WorldSendRequest extends SendRequest {
        final int dimensionId;

        WorldSendRequest(Packet packet, int dimensionId) {
            super(packet);
            this.dimensionId = dimensionId;
        }

        @Override
        void editAttributes(FMLEmbeddedChannel channel) {
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.DIMENSION);
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        }
    }

    class LocationSendRequest extends SendRequest {
        final int dimensionId;
        final int maxDistance;
        final Vec3d pos;

        LocationSendRequest(Packet packet, int dimensionId, Vec3d pos, int distance) {
            super(packet);
            this.dimensionId = dimensionId;
            this.pos = pos;
            this.maxDistance = distance * distance;
        }

        @Override
        void editAttributes(FMLEmbeddedChannel channel) {
            TargetPoint point = new TargetPoint(dimensionId, pos.xCoord, pos.yCoord, pos.zCoord, maxDistance);
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        }
    }

    class AllSendRequest extends SendRequest {
        AllSendRequest(Packet packet) {
            super(packet);
        }

        @Override
        void editAttributes(FMLEmbeddedChannel channel) {
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALL);
        }
    }

    static {
        // Swap them when debugging
        packetSender = Executors.newSingleThreadExecutor();
        // packetSender = ImmediateExecutor.INSTANCE;
    }

    private static void addSendRequest(SendRequest request) {
        packetSender.execute(request);
    }

    public void sendToPlayers(Packet packet, World world, BlockPos pos, int maxDistance) {
        addSendRequest(new LocationSendRequest(packet, world.provider.getDimension(), Utils.convertMiddle(pos), maxDistance));
    }

    public void sendToPlayersNear(Packet packet, TileEntity tile, int maxDistance) {
        sendToPlayers(packet, tile.getWorld(), tile.getPos(), maxDistance);
    }

    public void sendToPlayersNear(Packet packet, TileEntity tile) {
        sendToPlayersNear(packet, tile, DefaultProps.NETWORK_UPDATE_RANGE);
    }

    public void sendToWorld(Packet packet, World world) {
        addSendRequest(new WorldSendRequest(packet, world.provider.getDimension()));
    }

    public void sendToEntity(Packet packet, Entity entity) {
        addSendRequest(new EntitySendRequest(packet, entity, DefaultProps.NETWORK_UPDATE_RANGE));
    }

    public void sendToPlayer(EntityPlayer player, Packet packet) {
        addSendRequest(new PlayerSendRequest(packet, player));
    }

    public void sendToAll(Packet packet) {
        addSendRequest(new AllSendRequest(packet));
    }

    public void sendToServer(Packet packet) {
        if (packet.tempWorld == null) {
            NullPointerException npe = new NullPointerException("The packet's world was null! Cannot send this! (Client)");
            BCLog.logger.fatal("// Blame AlexIIL", npe);
            throw npe;
        }
        try {
            channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
            channels.get(Side.CLIENT).writeOutbound(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Property getOption(String name) {
        if (options.containsKey(name)) {
            return options.get(name);
        }
        return null;
    }

    /** WARNING: INTERNAL USE ONLY! */
    public void putOption(String name, Property value) {
        options.put(name, value);
    }
}
