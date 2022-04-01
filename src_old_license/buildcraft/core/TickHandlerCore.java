/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.List;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.network.base.PacketHandler;

public enum TickHandlerCore {
    INSTANCE;
    private static final List<PacketHandler> packetHandlers = Lists.newCopyOnWriteArrayList();

    public static void addPacketHandler(PacketHandler handler) {
        if (handler != null) {
            packetHandlers.add(handler);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            return;
        }

        World world = Minecraft.getMinecraft().theWorld;
        if (world != null) {
            for (PacketHandler packetHandler : packetHandlers) {
                packetHandler.tick(world);
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(WorldTickEvent event) {
        if (event.phase == Phase.START) {
            return;
        }

        for (PacketHandler packetHandler : packetHandlers) {
            packetHandler.tick(event.world);
        }
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload unload) {
        BCLog.logger.info("World Unload event");
        for (PacketHandler packetHandler : packetHandlers) {
            packetHandler.unload(unload.getWorld());
        }
    }
}
