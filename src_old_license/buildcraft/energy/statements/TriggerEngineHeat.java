/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;

public class TriggerEngineHeat extends BCStatement implements ITriggerExternal {

    public EnumEnergyStage stage;

    public TriggerEngineHeat(EnumEnergyStage stage) {
        super("buildcraft:engine.stage." + stage.name().toLowerCase(Locale.ROOT), "buildcraft.engine.stage." + stage.name().toLowerCase(Locale.ROOT));
        setBuildCraftLocation("energy", "triggers/trigger_engineheat_" + stage.name().toLowerCase(Locale.ROOT));
        this.stage = stage;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.engine." + stage.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer container, IStatementParameter[] parameters) {
        if (tile instanceof TileEngineBase) {
            TileEngineBase engine = (TileEngineBase) tile;

            return engine.getEnergyStage() == stage;
        }

        return false;
    }
}
