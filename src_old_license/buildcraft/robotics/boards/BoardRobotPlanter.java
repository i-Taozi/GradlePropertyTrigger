/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;

import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.lib.inventory.filter.AggregateFilter;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotPlant;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotPlanter extends RedstoneBoardRobot {

    private BlockPos blockFound;
    private IStackFilter filter = new IStackFilter() {

        @Override
        public boolean matches(ItemStack stack) {
            return CropManager.isSeed(stack);
        }
    };

    public BoardRobotPlanter(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("planter");
    }

    @Override
    public void update() {
        if (robot.getHeldItem() == null) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new AggregateFilter(filter, ActionRobotFilter.getGateFilter(robot
                    .getLinkedStation()))));
        } else {
            final ItemStack itemStack = robot.getHeldItem();
            IBlockFilter blockFilter = new IBlockFilter() {
                @Override
                public boolean matches(World world, BlockPos pos) {
                    return !BuildCraftAPI.getWorldProperty("replaceable").get(world, pos) && isPlantable(itemStack, world, pos) && !robot
                            .getRegistry().isTaken(new ResourceIdBlock(pos));
                }
            };
            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, true, blockFilter, 1));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock) {
            if (ai.success()) {
                blockFound = ((AIRobotSearchAndGotoBlock) ai).getBlockFound();
                startDelegateAI(new AIRobotPlant(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotPlant) {
            releaseBlockFound();
        } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    private boolean isPlantable(ItemStack seed, World world, BlockPos pos) {
        synchronized (world) {
            return CropManager.canSustainPlant(world, seed, pos);
        }
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.setTag("blockFound", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("blockFound")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.getTag("blockFound"));
        }
    }
}
