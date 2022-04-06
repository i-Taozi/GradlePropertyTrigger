package com.dynious.refinedrelocation.api.filter;

import com.dynious.refinedrelocation.api.tileentity.IFilterTileGUI;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

/**
 * @deprecated This interface will be renamed to IMultiFilter during the update to Minecraft 1.8. We already provide a dummy interface named like that which currently just extends this, so consider using that instead.
 */
@Deprecated
public interface IFilterGUI extends IFilter
{
	List<String> getWAILAInformation(NBTTagCompound compound);

	void filterChanged();

	void writeToNBT(NBTTagCompound compound);

	void readFromNBT(NBTTagCompound compound);

	boolean isDirty();

	void markDirty(boolean dirty);

	int getFilterCount();

	IMultiFilterChild getFilterAtIndex(int index);

    IFilterTileGUI getFilterTile();

	void setFilterType(int filterIndex, String filterType);

    void sendStringToPlayer(IMultiFilterChild receiver, EntityPlayerMP player, int index, String value);

    void sendStringToServer(IMultiFilterChild receiver, int index, String value);

    void sendBooleanToPlayer(IMultiFilterChild receiver, EntityPlayerMP player, int index, boolean value);

    void sendBooleanToServer(IMultiFilterChild receiver, int index, boolean value);

    void sendBooleanArrayToPlayer(IMultiFilterChild receiver, EntityPlayerMP player, int index, boolean[] value);

    void sendBooleanArrayToServer(IMultiFilterChild receiver, int index, boolean[] value);
}
