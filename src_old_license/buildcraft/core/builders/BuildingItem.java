/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftCore;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.core.ISerializable;
import buildcraft.core.StackAtPosition;
import buildcraft.core.block.BlockDecoration;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtilBC;

import io.netty.buffer.ByteBuf;

public class BuildingItem implements IBuildingItem, ISerializable {

    public static int ITEMS_SPACE = 2;

    public Vec3d origin, destination;
    public LinkedList<StackAtPosition> stacksToDisplay = new LinkedList<>();

    public boolean isDone = false;

    public BuildingSlot slotToBuild;
    public IBuilderContext context;

    private long previousUpdate;
    private float lifetimeDisplay = 0;
    private float maxLifetime = 0;
    private boolean initialized = false;
    private Vec3d velocity;
    private double maxHeight;
    private float lifetime = 0;

    public void initialize() {
        if (!initialized) {
            Vec3d vec = destination.subtract(origin);
            double size = vec.lengthVector();

            maxLifetime = (float) size * 4;

            maxHeight = size / 2;

            // The below computation is a much simpler version of the long commented out code. Hopefully it works :P

            velocity = Utils.multiply(vec, 1 / maxLifetime);

            // the below computation is an approximation of the distance to
            // travel for the object. It really follows a sinus, but we compute
            // the size of a triangle for simplification.

            // Vec3d middle = new Vec3d();
            // middle.x = (destination.x + origin.x) / 2;
            // middle.y = (destination.y + origin.y) / 2;
            // middle.z = (destination.z + origin.z) / 2;
            //
            // Vec3d top = new Vec3d();
            // top.x = middle.x;
            // top.y = middle.y + maxHeight;
            // top.z = middle.z;
            //
            // Vec3d originToTop = new Vec3d();
            // originToTop.x = top.x - origin.x;
            // originToTop.y = top.y - origin.y;
            // originToTop.z = top.z - origin.z;
            //
            // Vec3d destinationToTop = new Vec3d();
            // destinationToTop.x = destination.x - origin.x;
            // destinationToTop.y = destination.y - origin.y;
            // destinationToTop.z = destination.z - origin.z;
            //
            // double d1 = Math.sqrt(originToTop.x * originToTop.x + originToTop.y * originToTop.y + originToTop.z *
            // originToTop.z);
            //
            // double d2 =
            // Math.sqrt(destinationToTop.x * destinationToTop.x + destinationToTop.y * destinationToTop.y +
            // destinationToTop.z * destinationToTop.z);
            //
            // d1 = d1 / size * maxLifetime;
            // d2 = d2 / size * maxLifetime;
            //
            // maxLifetime = (float) d1 + (float) d2;
            //
            // vx = dx / maxLifetime;
            // vy = dy / maxLifetime;
            // vz = dz / maxLifetime;

            if (stacksToDisplay.size() == 0) {
                StackAtPosition sPos = new StackAtPosition();
                sPos.stack = new ItemStack(BuildCraftCore.decoratedBlock);
                stacksToDisplay.add(sPos);
            }

            initialized = true;
        }
    }

    public Vec3d getDisplayPosition(float time) {
        Vec3d pos = origin.add(Utils.multiply(velocity, time));
        return pos.addVector(0, MathHelper.sin(time / maxLifetime * (float) Math.PI) * maxHeight, 0);
    }

    public void update() {
        if (isDone) {
            return;
        }

        initialize();

        lifetime++;

        if (lifetime > maxLifetime + stacksToDisplay.size() * ITEMS_SPACE - 1) {
            isDone = true;
            build();
        }

        lifetimeDisplay = lifetime;
        previousUpdate = new Date().getTime();

        if (slotToBuild != null && lifetime > maxLifetime) {
            slotToBuild.writeCompleted(context, (lifetime - maxLifetime) / (stacksToDisplay.size() * ITEMS_SPACE));
        }
    }

    public void displayUpdate() {
        initialize();

        float tickDuration = 50.0F; // miliseconds
        long currentUpdate = new Date().getTime();
        float timeSpan = currentUpdate - previousUpdate;
        previousUpdate = currentUpdate;

        float displayPortion = timeSpan / tickDuration;

        if (lifetimeDisplay - lifetime <= 1.0) {
            lifetimeDisplay += 1.0 * displayPortion;
        }
    }

    private void build() {
        if (slotToBuild != null) {
            BlockPos dest = Utils.convertFloor(destination);

            IBlockState state = context.world().getBlockState(dest);

            /* if (BlockUtil.isToughBlock(context.world(), destX, destY, destZ)) { BlockUtil.breakBlock(context.world(),
             * destX, destY, destZ, BuildCraftBuilders.fillerLifespanTough); } else {
             * BlockUtil.breakBlock(context.world(), destX, destY, destZ, BuildCraftBuilders.fillerLifespanNormal); } */

            if (slotToBuild.writeToWorld(context)) {
                context.world().playAuxSFXAtEntity(null, 2001, dest, Block.getStateId(state));
            } else {
                for (ItemStack s : slotToBuild.stackConsumed) {
                    if (s != null && !(s.getItem() instanceof ItemBlock && Block.getBlockFromItem(s.getItem()) instanceof BlockDecoration)) {
                        InvUtils.dropItems(context.world(), s, dest);
                    }
                }
            }
        }
    }

    public LinkedList<StackAtPosition> getStacks() {
        int d = 0;

        for (StackAtPosition s : stacksToDisplay) {
            float stackLife = lifetimeDisplay - d;

            if (stackLife <= maxLifetime && stackLife > 0) {
                s.pos = getDisplayPosition(stackLife);
                s.display = true;
            } else {
                s.display = false;
            }

            d += ITEMS_SPACE;
        }

        return stacksToDisplay;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("origin", NBTUtilBC.writeVec3d(origin));
        nbt.setTag("destination", NBTUtilBC.writeVec3d(destination));
        nbt.setFloat("lifetime", lifetime);

        NBTTagList items = new NBTTagList();

        for (StackAtPosition s : stacksToDisplay) {
            NBTTagCompound cpt = new NBTTagCompound();
            s.stack.writeToNBT(cpt);
            items.appendTag(cpt);
        }

        nbt.setTag("items", items);

        MappingRegistry registry = new MappingRegistry();

        NBTTagCompound slotNBT = new NBTTagCompound();
        NBTTagCompound registryNBT = new NBTTagCompound();

        slotToBuild.writeToNBT(slotNBT, registry);
        registry.write(registryNBT);

        nbt.setTag("registry", registryNBT);

        if (slotToBuild instanceof BuildingSlotBlock) {
            nbt.setByte("slotKind", (byte) 0);
        } else {
            nbt.setByte("slotKind", (byte) 1);
        }

        nbt.setTag("slotToBuild", slotNBT);
    }

    public void readFromNBT(NBTTagCompound nbt) throws MappingNotFoundException {
        origin = NBTUtilBC.readVec3d(nbt, "origin");
        destination = NBTUtilBC.readVec3d(nbt, "destination");
        lifetime = nbt.getFloat("lifetime");

        NBTTagList items = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < items.tagCount(); ++i) {
            StackAtPosition sPos = new StackAtPosition();
            sPos.stack = ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i));
            stacksToDisplay.add(sPos);
        }

        MappingRegistry registry = new MappingRegistry();
        registry.read(nbt.getCompoundTag("registry"));

        if (nbt.getByte("slotKind") == 0) {
            slotToBuild = new BuildingSlotBlock();
        } else {
            slotToBuild = new BuildingSlotEntity();
        }

        slotToBuild.readFromNBT(nbt.getCompoundTag("slotToBuild"), registry);
    }

    public void setStacksToDisplay(List<ItemStack> stacks) {
        if (stacks != null) {
            for (ItemStack s : stacks) {
                for (int i = 0; i < s.stackSize; ++i) {
                    StackAtPosition sPos = new StackAtPosition();
                    sPos.stack = s.copy();
                    sPos.stack.stackSize = 1;
                    stacksToDisplay.add(sPos);
                }
            }
        }
    }

    @Override
    public void readData(ByteBuf stream) {
        origin = NetworkUtils.readVec3d(stream);
        destination = NetworkUtils.readVec3d(stream);
        lifetime = stream.readFloat();
        stacksToDisplay.clear();
        int size = stream.readUnsignedShort();
        for (int i = 0; i < size; i++) {
            StackAtPosition e = new StackAtPosition();
            e.readData(stream);
            stacksToDisplay.add(e);
        }
    }

    @Override
    public void writeData(ByteBuf stream) {
        NetworkUtils.writeVec3d(stream, origin);
        NetworkUtils.writeVec3d(stream, destination);
        stream.writeFloat(lifetime);
        stream.writeShort(stacksToDisplay.size());
        for (StackAtPosition s : stacksToDisplay) {
            s.writeData(stream);
        }
    }

    @Override
    public int hashCode() {
        return (131 * origin.hashCode()) + destination.hashCode();
    }
}
