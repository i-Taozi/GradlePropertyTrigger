/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;

import buildcraft.transport.PipeIconProvider;

public class PipeItemsDiorite extends PipeItemsStone {
    public PipeItemsDiorite(Item item) {
        super(item);
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.TYPE.PipeItemsDiorite.ordinal();
    }
}
