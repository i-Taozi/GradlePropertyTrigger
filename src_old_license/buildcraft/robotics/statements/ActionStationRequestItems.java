/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.statements.IStatementParameter;

import buildcraft.core.statements.StatementParameterItemStackExact;
import buildcraft.lib.misc.LocaleUtil;

public class ActionStationRequestItems extends ActionStationInputItems {

    public ActionStationRequestItems() {
        super("buildcraft:station.request_items");
        setBuildCraftLocation("robotics", "triggers/action_station_request_items");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.station.request_items");
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStackExact();
    }
}
