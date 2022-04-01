/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.PowerMode;
import buildcraft.core.statements.BCStatement;

public class ActionPowerLimiter extends BCStatement implements IActionInternal {

    public final PowerMode limit;

    public ActionPowerLimiter(PowerMode limit) {
        super("buildcraft:power.limiter." + limit.name().toLowerCase(Locale.ROOT), "buildcraft.power.limiter." + limit.name().toLowerCase(
                Locale.ROOT));
        setBuildCraftLocation("transport", "triggers/trigger_limiter_" + limit.name().toLowerCase(Locale.ROOT));

        this.limit = limit;
    }

    @Override
    public String getDescription() {
        return limit.maxPower + " RF/t Limit";
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }
}
