package com.dynious.refinedrelocation.tileentity;

import com.dynious.refinedrelocation.api.item.IItemRelocatorModule;
import com.dynious.refinedrelocation.api.relocator.IRelocatorModule;
import com.dynious.refinedrelocation.grid.relocator.RelocatorGridLogic;
import com.dynious.refinedrelocation.grid.relocator.RelocatorModuleRegistry;
import com.dynious.refinedrelocation.grid.relocator.RelocatorMultiModule;
import com.dynious.refinedrelocation.grid.relocator.TravellingItem;
import com.dynious.refinedrelocation.helper.*;
import com.dynious.refinedrelocation.lib.Mods;
import com.dynious.refinedrelocation.lib.Settings;
import com.dynious.refinedrelocation.compat.FMPHelper;
import com.dynious.refinedrelocation.network.NetworkHandler;
import com.dynious.refinedrelocation.network.packet.MessageItemList;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class TileRelocator extends TileEntity implements IRelocator, ISidedInventory
{
    public boolean shouldUpdate = true;
    public boolean isBeingPowered = false;
    private boolean isFirstTick = true;
    private boolean neighborChanged = false;
    private TileEntity[] inventories = new TileEntity[ForgeDirection.VALID_DIRECTIONS.length];
    private IRelocator[] relocators = new IRelocator[ForgeDirection.VALID_DIRECTIONS.length];
    private IRelocatorModule[] modules = new IRelocatorModule[ForgeDirection.VALID_DIRECTIONS.length];
    private List<ItemStack>[] stuffedItems;
    /*
    Only used client side
     */
    private boolean[] isConnected = new boolean[6];
    private byte renderType = 0;
    private boolean[] isStuffed = new boolean[6];
    /*
    Cached Paths and stack sizes
     */
    private TravellingItem cachedTravellingItem;
    private int maxStackSize = 64;
    private List<TravellingItem> items = new ArrayList<TravellingItem>();
    private List<TravellingItem> itemsToAdd = new ArrayList<TravellingItem>();
    /*
    Client side cached items
     */
    private TByteObjectMap<StackAndCounter> cachedItems = new TByteObjectHashMap<StackAndCounter>();
    private byte ticker = 0;
    private byte lastID = (byte) (new Random().nextInt(512) - 256);

    @SuppressWarnings("unchecked")
    public TileRelocator()
    {
        stuffedItems = (List<ItemStack>[]) new ArrayList[ForgeDirection.VALID_DIRECTIONS.length];
        for (int i = 0; i < stuffedItems.length; i++)
        {
            stuffedItems[i] = new ArrayList<ItemStack>();
        }
    }

    public static void markUpdate(World world, int x, int y, int z)
    {
        if (world.isRemote) return;

        if (Mods.IS_FMP_LOADED)
        {
            FMPHelper.updateBlock(world, x, y, z);
        }
        else
        {
            world.markBlockForUpdate(x, y, z);
        }
    }

    public static void markUpdateAndNotify(World world, int x, int y, int z)
    {
        markUpdate(world, x, y, z);
        world.notifyBlocksOfNeighborChange(x, y, z, world.getBlock(x, y, z));
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        for (int i = 0; i < modules.length; i++)
        {
            if (modules[i] != null)
            {
                modules[i].onUpdate(this, i);
            }
        }

        if (worldObj.isRemote)
        {
            clientSideUpdate();
        }
        else
        {
            serverSideUpdate();
        }
    }

    private void serverSideUpdate()
    {
        if (isFirstTick)
        {
            discoverNeighbours();
            isFirstTick = false;
            // If a neighbor updated before this tile at startup, don't affect modules
            neighborChanged = false;
        }

        if (neighborChanged)
        {
            neighborChangeUpdate();
            neighborChanged = false;
        }

        if (shouldUpdate)
        {
            markUpdate(worldObj, xCoord, yCoord, zCoord);
            shouldUpdate = false;
        }

        if (!itemsToAdd.isEmpty())
        {
            for (TravellingItem item : itemsToAdd)
            {
                if (item.sync)
                {
                    lastID++;
                    item.id = lastID;
                }
                else
                {
                    for (TravellingItem i : items)
                    {
                        if (i.id == item.id)
                        {
                            item.lastId = item.id;
                            lastID++;
                            item.id = lastID;
                            break;
                        }
                    }
                }
            }
            items.addAll(itemsToAdd);
            NetworkHandler.INSTANCE.sendToAllAround(new MessageItemList(this, itemsToAdd), new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 16));
            for (TravellingItem item : itemsToAdd)
            {
                item.sync = false;
                item.lastId = null;
            }
            itemsToAdd.clear();
        }

        for (Iterator<TravellingItem> iterator = items.iterator(); iterator.hasNext(); )
        {
            TravellingItem item = iterator.next();
            item.counter++;
            if (item.counter > TravellingItem.timePerRelocator)
            {
                iterator.remove();
                outputToSide(item, item.onOutput());
            }
            else if (item.counter == TravellingItem.timePerRelocator / 2)
            {
                if (!connectsToSide(item.getOutputSide()))
                {
                    iterator.remove();
                    retryOutput(item, -1);
                }
            }
        }

        ticker++;
        if (ticker >= Settings.RELOCATOR_MIN_TICKS_BETWEEN_EXTRACTION)
        {
            ticker = 0;
            for (byte side = 0; side < stuffedItems.length; side++)
            {
                if (stuffedItems[side].isEmpty())
                    continue;

                ArrayList<ItemStack> stacksUnableToAdd = new ArrayList<ItemStack>();
                for (ListIterator<ItemStack> iterator = stuffedItems[side].listIterator(); iterator.hasNext(); )
                {
                    ItemStack stack = iterator.next();
                    if (!stacksUnableToAdd.contains(stack) && (modules[side] == null || modules[side].passesFilter(this, side, stack, false, true)))
                    {
                        IRelocator relocator = getConnectedRelocators()[side];
                        if (relocator != null)
                        {
                            IRelocatorModule module = relocator.getRelocatorModule(ForgeDirection.OPPOSITES[side]);
                            if (module == null || module.passesFilter(relocator, ForgeDirection.OPPOSITES[side], stack, true, true))
                            {
                                if (module != null && module.isItemDestination())
                                {
                                    stack = module.receiveItemStack(relocator, ForgeDirection.OPPOSITES[side], stack.copy(), true, false);
                                }
                                else
                                {
                                    TravellingItem item = RelocatorGridLogic.findOutput(stack, relocator, ForgeDirection.OPPOSITES[side]);
                                    if (item != null)
                                    {
                                        stack.stackSize -= item.getStackSize();
                                        relocator.receiveTravellingItem(item);
                                    }
                                }
                            }
                        }
                        else
                        {
                            if (getRelocatorModule(side) != null)
                            {
                                stack = getRelocatorModule(side).outputToSide(this, side, getConnectedInventories()[side], stack.copy(), false);
                            }
                            else
                            {
                                stack = IOHelper.insert(getConnectedInventories()[side], stack.copy(), ForgeDirection.getOrientation(side).getOpposite(), false);
                            }
                        }
                        if (stack == null || stack.stackSize == 0)
                        {
                            iterator.remove();
                        }
                        else
                        {
                            iterator.set(stack);
                            stacksUnableToAdd.add(stack.copy());
                        }

                    }
                }

                if (stuffedItems[side].isEmpty())
                    shouldUpdate = true;
            }
        }

        cachedTravellingItem = null;
        maxStackSize = 64;
    }

    private void clientSideUpdate()
    {
        for (Iterator<TravellingItem> iterator = items.iterator(); iterator.hasNext(); )
        {
            TravellingItem item = iterator.next();
            item.counter++;
            if (item.counter > (TravellingItem.timePerRelocator + 1))
            {
                iterator.remove();
            }
            else if (item.counter == TravellingItem.timePerRelocator / 2)
            {
                cachedItems.put(item.id, new StackAndCounter(item.getItemStack()));
                if (!connectsToSide(item.getOutputSide()))
                {
                    iterator.remove();
                }
            }
        }
        for (Iterator<StackAndCounter> iterator = cachedItems.valueCollection().iterator(); iterator.hasNext(); )
        {
            StackAndCounter stackAndCounter = iterator.next();
            stackAndCounter.counter--;
            if (stackAndCounter.counter <= 0)
                iterator.remove();
        }
    }

    public void updateRedstone()
    {
        boolean newPowerState = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) || BlockHelper.isDirectlyPowered(worldObj, xCoord, yCoord, zCoord);
        if ((newPowerState && !isBeingPowered) || (!newPowerState && isBeingPowered))
        {
            isBeingPowered = newPowerState;
            for (IRelocatorModule module : modules)
            {
                if (module != null)
                {
                    module.onRedstonePowerChange(isBeingPowered);
                }
            }
        }
    }

    public boolean getRedstoneState()
    {
        return isBeingPowered;
    }

    public boolean shouldConnectToRedstone()
    {
        for (IRelocatorModule module : modules)
        {
            if (module != null && module.connectsToRedstone())
                return true;
        }
        return false;
    }

    public int isProvidingStrongPower(int side)
    {
        for (IRelocatorModule module : modules)
        {
            if (module != null)
            {
                return module.strongRedstonePower(side);
            }
        }
        return 0;
    }

    public boolean leftClick(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
    {
        if (worldObj.isRemote) return true;

        int side = hit.subHit;
        if (side < 6) // Hit side, not middle
        {
            if (isStuffedOnSide(side))
            {
                for (int i = 0; i < Math.min(stuffedItems[side].size(), 5); i++)
                {
                    IOHelper.spawnItemInWorld(worldObj, stuffedItems[side].get(i), xCoord, yCoord, zCoord);
                }
                markUpdate(worldObj, xCoord, yCoord, zCoord);
                return false;
            }
        }
        return true;
    }

    public boolean onActivated(EntityPlayer player, MovingObjectPosition hit, ItemStack stack)
    {
        if (hit.subHit < 6)
        {
            return sideHit(player, hit.subHit, stack);
        }
        else
        {
            //Middle was hit
        }
        return false;
    }

    public boolean sideHit(EntityPlayer player, int side, ItemStack stack)
    {
        if (stack != null && stack.getItem() instanceof IItemRelocatorModule && modules[side] == null)
        {
            IRelocatorModule module = ((IItemRelocatorModule) stack.getItem()).getRelocatorModule(stack);
            if (module != null)
            {
                modules[side] = module;
                module.init(this, side);
                if (!player.capabilities.isCreativeMode)
                    stack.stackSize--;
                markUpdateAndNotify(worldObj, xCoord, yCoord, zCoord);
                return true;
            }
        }
        else if (modules[side] != null)
        {
            if (player.isSneaking())
            {
                List<ItemStack> list = modules[side].getDrops(this, side);
                if (list != null && !player.capabilities.isCreativeMode)
                {
                    for (ItemStack stack1 : list)
                    {
                        IOHelper.spawnItemInWorld(worldObj, stack1, xCoord, yCoord, zCoord);
                    }
                }
                modules[side] = null;
                markUpdateAndNotify(worldObj, xCoord, yCoord, zCoord);
                return true;
            }
            else
            {
                return modules[side].onActivated(this, player, side, stack);
            }
        }
        return false;
    }

    public void emptySide(int side)
    {
        if (modules[side] != null)
        {
            for (ItemStack stack : modules[side].getDrops(this, side))
            {
                IOHelper.spawnItemInWorld(worldObj, stack, xCoord, yCoord, zCoord);
            }
            modules[side] = null;
        }
        if (stuffedItems[side] != null)
        {
            for (ItemStack stack : stuffedItems[side])
            {
                IOHelper.spawnItemInWorld(worldObj, stack, xCoord, yCoord, zCoord);
            }
            stuffedItems[side].clear();
        }
        for (Iterator<TravellingItem> iterator = items.iterator(); iterator.hasNext(); )
        {
            TravellingItem item = iterator.next();
            if ((item.getInputSide() == side && item.counter < TravellingItem.timePerRelocator / 2)
                    || (item.getOutputSide() == side && item.counter > TravellingItem.timePerRelocator / 2))
            {
                IOHelper.spawnItemInWorld(worldObj, item.getItemStack(), xCoord, yCoord, zCoord);
                iterator.remove();
            }
        }
        for (Iterator<TravellingItem> iterator = itemsToAdd.iterator(); iterator.hasNext(); )
        {
            TravellingItem item = iterator.next();
            if (item.getInputSide() == side)
            {
                IOHelper.spawnItemInWorld(worldObj, item.getItemStack(), xCoord, yCoord, zCoord);
                iterator.remove();
            }
        }
    }

    public List<ItemStack> getDrops()
    {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (TravellingItem travellingItem : getItems(true))
        {
            items.add(travellingItem.getItemStack());
        }
        for (int i = 0; i < modules.length; i++)
        {
            IRelocatorModule module = modules[i];
            if (module != null)
            {
                items.addAll(module.getDrops(this, i));
            }
        }
        for (List<ItemStack> stuffedItem : stuffedItems)
        {
            items.addAll(stuffedItem);
        }
        return items;
    }

    public void discoverNeighbours()
    {
        inventories = new TileEntity[ForgeDirection.VALID_DIRECTIONS.length];
        relocators = new IRelocator[ForgeDirection.VALID_DIRECTIONS.length];

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
        {
            if (!((IRelocator) worldObj.getTileEntity(xCoord, yCoord, zCoord)).canConnectOnSide(direction.ordinal()))
                continue;

            TileEntity tile = DirectionHelper.getTileAtSide(this, direction);
            if (tile != null)
            {
                if (tile instanceof IRelocator)
                {
                    if (((IRelocator) tile).canConnectOnSide(direction.getOpposite().ordinal()))
                    {
                        relocators[direction.ordinal()] = (IRelocator) tile;
                    }
                }
                else if (IOHelper.canInterfaceWith(tile, direction.getOpposite()))
                {
                    inventories[direction.ordinal()] = tile;
                }
            }
        }
    }

    public void onBlocksChanged()
    {
        neighborChanged = true;
    }

    private void neighborChangeUpdate()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            return;

        discoverNeighbours();

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
        {
            if (relocators[direction.ordinal()] == null && inventories[direction.ordinal()] == null)
            {
                emptySide(direction.ordinal());
            }
        }

        updateRedstone();
        markUpdate(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public GuiScreen getGUI(int side, EntityPlayer player)
    {
        IRelocatorModule module = modules[side];
        if (module instanceof RelocatorMultiModule)
        {
            return ((RelocatorMultiModule) module).getCurrentModule().getGUI(this, side, player);
        }
        else
        {
            return module.getGUI(this, side, player);
        }
    }

    @Override
    public Container getContainer(int side, EntityPlayer player)
    {
        IRelocatorModule module = modules[side];
        if (module instanceof RelocatorMultiModule)
        {
            return ((RelocatorMultiModule) module).getCurrentModule().getContainer(this, side, player);
        }
        else
        {
            return module.getContainer(this, side, player);
        }
    }

    @Override
    public boolean canConnectOnSide(int side)
    {
        return true;
    }

    @Override
    public IRelocator[] getConnectedRelocators()
    {
        return relocators;
    }

    @Override
    public boolean passesFilter(ItemStack itemStack, int side, boolean input, boolean simulate)
    {
        return stuffedItems[side].isEmpty() && (modules[side] == null || modules[side].passesFilter(this, side, itemStack, input, simulate));
    }

    @Override
    public TileEntity[] getConnectedInventories()
    {
        return inventories;
    }

    @Override
    public List<TravellingItem> getItems(boolean includeItemsToAdd)
    {
        if (includeItemsToAdd)
        {
            List<TravellingItem> list = new ArrayList<TravellingItem>(items);
            list.addAll(itemsToAdd);
            return list;
        }
        return items;
    }

    @Override
    public TileEntity getTileEntity()
    {
        return this;
    }

    @Override
    public void receiveTravellingItem(TravellingItem item)
    {
        if (worldObj.isRemote)
        {
            if (!item.getPath().isEmpty())
                items.add(item);
        }
        else
        {
            if (item.getPath().isEmpty())
            {
                retryOutput(item, item.input);
            }
            else
            {
                itemsToAdd.add(item);
            }
        }
    }

    public void outputToSide(TravellingItem item, byte side)
    {
        //Can we output?
        if (modules[side] == null || modules[side].passesFilter(this, side, item.getItemStack(), false, false))
        {
            //Is our output side our destination?
            if (modules[side] != null && modules[side].isItemDestination())
            {
                ItemStack stack = modules[side].receiveItemStack(this, side, item.getItemStack(), false, false);
                if (stack != null && stack.stackSize > 0)
                {
                    item.getItemStack().stackSize = stack.stackSize;
                    retryOutput(item, side);
                }
                return;
            }
            IRelocator relocator = getConnectedRelocators()[side];
            if (relocator != null)
            {
                int side2 = ForgeDirection.OPPOSITES[side];
                IRelocatorModule module = relocator.getRelocatorModule(side2);

                //Can we input?
                if (module == null || module.passesFilter(relocator, side2, item.getItemStack(), true, false))
                {
                    //Is our input side our destination?
                    if (module != null && module.isItemDestination())
                    {
                        ItemStack stack = module.receiveItemStack(this, side, item.getItemStack(), true, false);
                        if (stack != null && stack.stackSize > 0)
                        {
                            item.getItemStack().stackSize = stack.stackSize;
                            retryOutput(item, side);
                        }
                        return;
                    }
                    relocator.receiveTravellingItem(item);
                    return;
                }
            }
            else if (getConnectedInventories()[side] != null)
            {
                ItemStack stack;
                if (getRelocatorModule(side) != null)
                {
                    stack = getRelocatorModule(side).outputToSide(this, side, getConnectedInventories()[side], item.getItemStack().copy(), false);
                }
                else
                {
                    stack = IOHelper.insert(getConnectedInventories()[side], item.getItemStack().copy(), ForgeDirection.getOrientation(side).getOpposite(), false);
                }
                if (stack != null && stack.stackSize > 0)
                {
                    item.getItemStack().stackSize = stack.stackSize;
                    retryOutput(item, side);
                }
                return;
            }
        }
        retryOutput(item, side);
    }

    public void retryOutput(TravellingItem item, int side)
    {
        ItemStack stack = item.getItemStack();
        TravellingItem travellingItem = RelocatorGridLogic.findOutput(item.getItemStack(), this, side);
        if (travellingItem != null)
        {
            receiveTravellingItem(travellingItem);
            stack.stackSize -= item.getStackSize();
            if (stack.stackSize <= 0)
            {
                stack = null;
            }
        }
        if (stack != null)
        {
            if (side != -1)
            {
                for (ItemStack stack1 : stuffedItems[side])
                {
                    if (stack1.stackSize != stack1.getMaxStackSize() && ItemStackHelper.areItemStacksEqual(stack1, stack))
                    {
                        int amount = Math.min(stack.stackSize, stack1.getMaxStackSize() - stack1.stackSize);
                        stack1.stackSize += amount;
                        stack.stackSize -= amount;
                        if (stack.stackSize == 0)
                        {
                            stack = null;
                            break;
                        }
                    }
                }
                if (stack != null)
                {
                    if (stuffedItems[side].isEmpty())
                        shouldUpdate = true;

                    stuffedItems[side].add(stack);
                }
            }
            else
            {
                IOHelper.spawnItemInWorld(worldObj, stack, xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public ItemStack insert(ItemStack itemStack, int side, boolean simulate)
    {
        if (itemStack == null || itemStack.stackSize == 0)
            return null;
        if (passesFilter(itemStack, side, true, true))
        {
            TravellingItem item = RelocatorGridLogic.findOutput(itemStack.copy(), this, side);
            if (item != null)
            {
                if (!simulate)
                {
                    receiveTravellingItem(item);
                }
                itemStack.stackSize -= item.getStackSize();
                if (itemStack.stackSize <= 0)
                {
                    return null;
                }
            }
        }
        return itemStack;
    }

    @Override
    public boolean connectsToSide(int side)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
            return relocators[side] != null || inventories[side] != null;
        else
            return isConnected[side];
    }

    @Override
    public boolean isStuffedOnSide(int side)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
            return !stuffedItems[side].isEmpty();
        else
            return isStuffed[side];
    }

    @Override
    public IRelocatorModule getRelocatorModule(int side)
    {
        return modules[side];
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        isBeingPowered = compound.getBoolean("redstone");

        if (compound.hasKey("Items"))
        {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                TravellingItem readTravellingStack = TravellingItem.createFromNBT(nbttagcompound1);
                if (readTravellingStack != null)
                    items.add(readTravellingStack);
            }
        }
        if (compound.hasKey("ItemsToAdd"))
        {
            NBTTagList nbttaglist = compound.getTagList("ItemsToAdd", 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                TravellingItem readTravellingStack = TravellingItem.createFromNBT(nbttagcompound1);
                if (readTravellingStack != null)
                    itemsToAdd.add(readTravellingStack);
            }
        }

        if (compound.hasKey("StuffedItems"))
        {
            NBTTagList nbttaglist = compound.getTagList("StuffedItems", 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                byte side = nbttagcompound1.getByte("Side");
                ItemStack readStack = ItemStack.loadItemStackFromNBT(nbttagcompound1);
                if (readStack != null)
                    stuffedItems[side].add(readStack);
            }
        }

        readModules(compound, false);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setBoolean("redstone", isBeingPowered);

        if (!items.isEmpty())
        {
            NBTTagList nbttaglist = new NBTTagList();
            for (TravellingItem item : this.items)
            {
                if (item != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    item.writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }
            compound.setTag("Items", nbttaglist);
        }

        if (!itemsToAdd.isEmpty())
        {
            NBTTagList nbttaglist = new NBTTagList();
            for (TravellingItem item : this.itemsToAdd)
            {
                if (item != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    item.writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }
            compound.setTag("ItemsToAdd", nbttaglist);
        }

        NBTTagList nbttaglist = new NBTTagList();
        for (byte i = 0; i < stuffedItems.length; i++)
        {
            List<ItemStack> stuffedItemList = stuffedItems[i];
            if (!stuffedItemList.isEmpty())
            {
                for (ItemStack item : stuffedItemList)
                {
                    if (item != null)
                    {
                        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                        nbttagcompound1.setByte("Side", i);
                        item.writeToNBT(nbttagcompound1);
                        nbttaglist.appendTag(nbttagcompound1);
                    }
                }
            }
        }
        compound.setTag("StuffedItems", nbttaglist);

        saveModules(compound, false);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setBoolean("redstone", isBeingPowered);

        for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++)
        {
            if (connectsToSide(i))
                tag.setBoolean("c" + i, true);
        }
        saveModules(tag, true);

        for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++)
        {
            if (!stuffedItems[i].isEmpty())
                tag.setBoolean("s" + i, true);
        }

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        NBTTagCompound tag = pkt.func_148857_g();

        isBeingPowered = tag.getBoolean("redstone");

        for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++)
        {
            isConnected[i] = tag.hasKey("c" + i);
        }
        calculateRenderType();
        modules = new IRelocatorModule[ForgeDirection.VALID_DIRECTIONS.length];
        readModules(tag, true);

        for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++)
        {
            isStuffed[i] = tag.hasKey("s" + i);
        }

        removeFloatingItems();
    }

    public void saveModules(NBTTagCompound compound, boolean client)
    {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < modules.length; i++)
        {
            if (modules[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setString("clazzIdentifier", RelocatorModuleRegistry.getIdentifier(modules[i].getClass()));
                nbttagcompound1.setByte("place", (byte) i);
                if (client)
                    modules[i].writeClientData(this, i, nbttagcompound1);
                else
                    modules[i].writeToNBT(this, i, nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        compound.setTag("modules", nbttaglist);
    }

    public void readModules(NBTTagCompound compound, boolean client)
    {
        NBTTagList nbttaglist = compound.getTagList("modules", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte place = nbttagcompound1.getByte("place");
            IRelocatorModule module = RelocatorModuleRegistry.getModule(nbttagcompound1.getString("clazzIdentifier"));
            if (module != null)
            {
                module.init(this, place);
                modules[place] = module;
                if (client)
                    modules[place].readClientData(this, i, nbttagcompound1);
                else
                    modules[place].readFromNBT(this, i, nbttagcompound1);
            }
        }
    }

    /*
    ISidedInventory implementation
     */

    public void calculateRenderType()
    {
        if (isConnected[0] && isConnected[1] && !isConnected[2] && !isConnected[3] && !isConnected[4] && !isConnected[5])
            renderType = 1;
        else if (!isConnected[0] && !isConnected[1] && isConnected[2] && isConnected[3] && !isConnected[4] && !isConnected[5])
            renderType = 2;
        else if (!isConnected[0] && !isConnected[1] && !isConnected[2] && !isConnected[3] && isConnected[4] && isConnected[5])
            renderType = 3;
        else
            renderType = 0;
    }

    public byte getRenderType()
    {
        return renderType;
    }

    @Override
    public ItemStack getItemStackWithId(byte id)
    {
        if (worldObj.isRemote)
        {
            StackAndCounter item = cachedItems.get(id);
            return item == null ? null : item.stack;
        }
        else
        {
            for (TravellingItem item : items)
            {
                if (item.id == id)
                    return item.getItemStack();
            }
            return null;
        }
    }

    public void removeFloatingItems()
    {
        for (Iterator<TravellingItem> iterator = items.iterator(); iterator.hasNext(); )
        {
            TravellingItem item = iterator.next();
            if ((item.counter - 1 < TravellingItem.timePerRelocator / 2 && !connectsToSide(item.getInputSide() < 0 ? 0 : item.getInputSide()))
                    || (item.counter - 1 > TravellingItem.timePerRelocator / 2 && !connectsToSide(item.getOutputSide())))
            {
                iterator.remove();
            }
        }
        for (int i = 0; i < isStuffed.length; i++)
        {
            if (!connectsToSide(i))
                isStuffed[i] = false;
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[]{side};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int side)
    {
        if (itemStack == null || itemStack.stackSize == 0)
            return false;

        if (connectsToSide(side) && passesFilter(itemStack, side, true, true))
        {
            cachedTravellingItem = RelocatorGridLogic.findOutput(itemStack.copy(), this, side);
            if (cachedTravellingItem != null)
            {
                maxStackSize = cachedTravellingItem.getStackSize();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int side)
    {
        return false;
    }

    @Override
    public int getSizeInventory()
    {
        return 6;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int side, ItemStack itemstack)
    {
        if (itemstack == null || itemstack.stackSize == 0)
            return;

        ItemStack itemStackCopy = itemstack.copy();

        if (cachedTravellingItem != null && side == cachedTravellingItem.input && cachedTravellingItem.isItemSameAs(itemStackCopy))
        {
            if (cachedTravellingItem.getStackSize() > itemStackCopy.stackSize)
            {
                cachedTravellingItem.getItemStack().stackSize = itemStackCopy.stackSize;
            }
            itemStackCopy.stackSize -= cachedTravellingItem.getStackSize();
            receiveTravellingItem(cachedTravellingItem);
        }
        if (itemStackCopy.stackSize > 0)
        {
            TravellingItem travellingItem = RelocatorGridLogic.findOutput(itemStackCopy.copy(), this, side);
            if (travellingItem != null)
            {
                itemStackCopy.stackSize -= travellingItem.getStackSize();
                if (itemStackCopy.stackSize > 0)
                {
                    travellingItem.getItemStack().stackSize += itemStackCopy.stackSize;
                }
                receiveTravellingItem(travellingItem);
            }
            else
            {
                TileEntity tile = DirectionHelper.getTileAtSide(this, ForgeDirection.getOrientation(side));
                LogHelper.warning(String.format("Tileentity at %s:%s:%s inserted ItemStack wrongly into Relocator!!", tile.xCoord, tile.yCoord, tile.zCoord));
                IOHelper.spawnItemInWorld(worldObj, itemStackCopy, xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public String getInventoryName()
    {
        return "tile.relocator.name";
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return maxStackSize;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
    }


    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack)
    {
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return super.getRenderBoundingBox().expand(0.5, 0.5, 0.5);
    }

    /*
    IItemDuct implementation
     */

    @Override
    @Optional.Method(modid = Mods.COFH_TRANSPORT_API_ID)
    public ItemStack insertItem(ForgeDirection forgeDirection, ItemStack itemStack)
    {
        return insert(itemStack, forgeDirection.ordinal(), false);
    }

    private static class StackAndCounter
    {
        public ItemStack stack;
        // Cache time in ticks
        public byte counter = 20;

        public StackAndCounter(ItemStack stack)
        {
            this.stack = stack;
        }
    }
}
