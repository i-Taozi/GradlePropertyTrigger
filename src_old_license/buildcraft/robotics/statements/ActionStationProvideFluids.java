/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;

public class ActionStationProvideFluids extends BCStatement implements IActionInternal {

    public ActionStationProvideFluids() {
        super("buildcraft:station.provide_fluids");
        setBuildCraftLocation("robotics", "triggers/action_station_provide_fluids");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.station.povide_fluids");
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
