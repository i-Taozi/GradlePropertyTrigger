package com.dynious.refinedrelocation.network;

import com.dynious.refinedrelocation.RefinedRelocation;
import com.dynious.refinedrelocation.api.tileentity.IFilterTileGUI;
import com.dynious.refinedrelocation.api.tileentity.IMultiFilterTile;
import com.dynious.refinedrelocation.client.gui.*;
import com.dynious.refinedrelocation.client.gui.GuiBlockExtender;
import com.dynious.refinedrelocation.container.*;
import com.dynious.refinedrelocation.lib.GuiIds;
import com.dynious.refinedrelocation.tileentity.*;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class GuiHandler implements IGuiHandler {
    public GuiHandler() {
        NetworkRegistry.INSTANCE.registerGuiHandler(RefinedRelocation.instance, this);
    }

    @Override
    public Object getServerGuiElement(int guiId, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null) {
            switch (guiId) {
                case GuiIds.FILTERED:
                    if (tile instanceof IFilterTileGUI) {
                        return new ContainerFiltered((IFilterTileGUI) tile);
                    }
                    break;
                case GuiIds.BLOCK_EXTENDER:
                    if (tile instanceof TileBlockExtender) {
                        return new ContainerBlockExtender((TileBlockExtender) tile);
                    }
                case GuiIds.ADVANCED_BLOCK_EXTENDER:
                case GuiIds.ADVANCED_BUFFER:
                    if (tile instanceof IAdvancedTile) {
                        return new ContainerAdvanced((IAdvancedTile) tile);
                    }
                    break;
                case GuiIds.ADVANCED_FILTERED_BLOCK_EXTENDER:
                    if (tile instanceof IAdvancedFilteredTile) {
                        return new ContainerAdvancedFiltered((IAdvancedFilteredTile) tile);
                    }
                    break;
                case GuiIds.SORTING_CHEST:
                    if (tile instanceof TileSortingChest) {
                        return new ContainerSortingChest(entityPlayer, (TileSortingChest) tile);
                    }
                    break;
                case GuiIds.FILTERING_HOPPER:
                    if (tile instanceof TileFilteringHopper) {
                        return new ContainerFilteringHopper(entityPlayer.inventory, (IMultiFilterTile) tile);
                    }
                    break;
                case GuiIds.SORTING_INPUT_PANE:
                    if (tile instanceof TileSortingInputPane) {
                        return new ContainerSortingInputPane(entityPlayer, (TileSortingInputPane) tile);
                    }
                    break;
                case GuiIds.POWER_LIMITER:
                    if (tile instanceof TilePowerLimiter) {
                        return new ContainerPowerLimiter((TilePowerLimiter) tile);
                    }
                    break;
            }
            if (guiId >= GuiIds.RELOCATOR_FILTER_BASE && guiId < (GuiIds.RELOCATOR_FILTER_BASE + ForgeDirection.VALID_DIRECTIONS.length)) {
                if (tile instanceof IRelocator) {
                    return ((IRelocator) tile).getContainer(guiId - GuiIds.RELOCATOR_FILTER_BASE, entityPlayer);
                }
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiId, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null) {
            switch (guiId) {
                case GuiIds.ADVANCED_BLOCK_EXTENDER:
                    if (tile instanceof TileAdvancedBlockExtender) {
                        return new GuiAdvancedBlockExtender(entityPlayer.inventory, (TileAdvancedBlockExtender) tile);
                    }
                    break;
                case GuiIds.BLOCK_EXTENDER:
                    if (tile instanceof TileBlockExtender) {
                        return new GuiBlockExtender(entityPlayer.inventory, (TileBlockExtender) tile);
                    }
                case GuiIds.FILTERED:
                    if (tile instanceof IFilterTileGUI) {
                        return new GuiFiltered((IFilterTileGUI) tile, new ContainerFiltered((IFilterTileGUI) tile));
                    }
                    break;
                case GuiIds.ADVANCED_FILTERED_BLOCK_EXTENDER:
                    if (tile instanceof TileAdvancedFilteredBlockExtender) {
                        return new GuiFiltered((IMultiFilterTile) tile, new ContainerAdvancedFiltered((IAdvancedFilteredTile) tile));
                    }
                    break;
                case GuiIds.ADVANCED_BUFFER:
                    if (tile instanceof TileAdvancedBuffer) {
                        return new GuiAdvancedBuffer(entityPlayer.inventory, (TileAdvancedBuffer) tile);
                    }
                    break;
                case GuiIds.SORTING_CHEST:
                    if (tile instanceof TileSortingChest) {
                        return new GuiSortingChest(entityPlayer, (TileSortingChest) tile);
                    }
                    break;
                case GuiIds.FILTERING_HOPPER:
                    if (tile instanceof TileFilteringHopper) {
                        return new GuiFilteringHopper(entityPlayer.inventory, (TileFilteringHopper) tile);
                    }
                    break;
                case GuiIds.SORTING_INPUT_PANE:
                    if (tile instanceof TileSortingInputPane) {
                        return new GuiSortingInputPane(entityPlayer, (TileSortingInputPane) tile);
                    }
                    break;
                case GuiIds.POWER_LIMITER:
                    if (tile instanceof TilePowerLimiter) {
                        return new GuiPowerLimiter((TilePowerLimiter) tile);
                    }
                    break;
            }
            if (guiId >= GuiIds.RELOCATOR_FILTER_BASE && guiId < (GuiIds.RELOCATOR_FILTER_BASE + ForgeDirection.VALID_DIRECTIONS.length)) {
                if (tile instanceof IRelocator) {
                    return ((IRelocator) tile).getGUI(guiId - GuiIds.RELOCATOR_FILTER_BASE, entityPlayer);
                }
            }
        }
        return null;
    }

}
