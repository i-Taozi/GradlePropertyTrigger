/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import java.util.Collection;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.robotics.RobotUtils;

public class RobotsTriggerProvider implements ITriggerProvider {

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> result, IStatementContainer container) {
        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        if (stations.size() > 0) {
            result.add(BuildCraftRobotics.triggerRobotSleep);
            result.add(BuildCraftRobotics.triggerRobotInStation);
            result.add(BuildCraftRobotics.triggerRobotLinked);
            result.add(BuildCraftRobotics.triggerRobotReserved);
        }
    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, EnumFacing side, TileEntity tile) {

    }
}
