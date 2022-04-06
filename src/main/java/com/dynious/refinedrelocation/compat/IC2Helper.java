package com.dynious.refinedrelocation.compat;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class IC2Helper
{
    public static void addToEnergyNet(TileEntity tile)
    {
        if (!tile.getWorldObj().isRemote && tile instanceof IEnergyTile)
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
    }

    public static void removeFromEnergyNet(TileEntity tile)
    {
        if (!tile.getWorldObj().isRemote && tile instanceof IEnergyTile)
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile));
    }
}
