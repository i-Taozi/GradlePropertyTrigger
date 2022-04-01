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
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.robotics.RobotUtils;

public class RobotsActionProvider implements IActionProvider {

    @Override
    public void addInternalActions(Collection<IActionInternal> result, IStatementContainer container) {
        TileEntity tile = container.getTile();

        if (!(tile instanceof IPipeTile)) {
            return;
        }

        IPipeTile pipeTile = (IPipeTile) tile;

        List<DockingStation> stations = RobotUtils.getStations(pipeTile);

        if (stations.size() == 0) {
            return;
        }

        result.add(BuildCraftRobotics.actionRobotGotoStation);
        result.add(BuildCraftRobotics.actionRobotWorkInArea);
        result.add(BuildCraftRobotics.actionRobotLoadUnloadArea);
        result.add(BuildCraftRobotics.actionRobotWakeUp);
        result.add(BuildCraftRobotics.actionRobotFilter);
        result.add(BuildCraftRobotics.actionRobotFilterTool);
        result.add(BuildCraftRobotics.actionStationForbidRobot);
        result.add(BuildCraftRobotics.actionStationForceRobot);

        if (pipeTile.getPipeType() == PipeType.ITEM) {
            result.add(BuildCraftRobotics.actionStationRequestItems);
            result.add(BuildCraftRobotics.actionStationAcceptItems);
        }

        if (pipeTile.getPipeType() == PipeType.FLUID) {
            result.add(BuildCraftRobotics.actionStationAcceptFluids);
        }

        for (DockingStation station : stations) {
            if (station.getItemInput() != null) {
                result.add(BuildCraftRobotics.actionStationProvideItems);
            }

            if (station.getFluidInput() != null) {
                result.add(BuildCraftRobotics.actionStationProvideFluids);
            }

            if (station.getRequestProvider() != null) {
                result.add(BuildCraftRobotics.actionStationMachineRequestItems);
            }
        }
    }

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, EnumFacing side, TileEntity tile) {

    }
}
