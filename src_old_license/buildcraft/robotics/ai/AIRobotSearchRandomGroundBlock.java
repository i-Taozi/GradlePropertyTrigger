/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.MathHelper;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.IBlockFilter;

public class AIRobotSearchRandomGroundBlock extends AIRobot {

    private static final int MAX_ATTEMPTS = 4096;

    public BlockPos blockFound;

    private int range;
    private IBlockFilter filter;
    private IZone zone;
    private int attempts = 0;

    public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot, int iRange, IBlockFilter iFilter, IZone iZone) {
        this(iRobot);

        range = iRange;
        filter = iFilter;
        zone = iZone;
    }

    @Override
    public void update() {
        if (filter == null) {
            terminate();
        }

        attempts++;

        if (attempts > MAX_ATTEMPTS) {
            terminate();
        }

        int x, z;

        if (zone == null) {
            double r = robot.worldObj.rand.nextFloat() * range;
            float a = robot.worldObj.rand.nextFloat() * 2.0F * (float) Math.PI;

            x = (int) (MathHelper.cos(a) * r + Math.floor(robot.posX));
            z = (int) (MathHelper.sin(a) * r + Math.floor(robot.posZ));
        } else {
            BlockPos b = zone.getRandomBlockPos(robot.worldObj.rand);
            x = b.getX();
            z = b.getZ();
        }

        BlockPos pos = new BlockPos(x, robot.worldObj.getHeight(), z);
        for (; pos.getY() >= 0; pos = pos.down()) {
            if (filter.matches(robot.worldObj, pos)) {
                blockFound = new BlockPos(pos);
                terminate();
                return;
            } else if (!robot.worldObj.isAirBlock(pos)) {
                return;
            }
        }
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    @Override
    public int getEnergyCost() {
        return 2;
    }
}
