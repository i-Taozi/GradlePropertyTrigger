/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.statements;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.builders.TileFiller;
import buildcraft.core.builders.patterns.FillerPattern;

public class BuildersActionProvider implements IActionProvider {
    private final HashMap<String, ActionFiller> actionMap = new HashMap<>();

    @Override
    public void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container) {

    }

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, EnumFacing side, TileEntity tile) {
        if (tile instanceof TileFiller) {
            for (IFillerPattern p : FillerManager.registry.getPatterns()) {
                if (p instanceof FillerPattern) {
                    FillerPattern pattern = (FillerPattern) p;
                    if (!actionMap.containsKey(p.getUniqueTag())) {
                        actionMap.put(p.getUniqueTag(), ActionFiller.getForPattern(pattern));
                    }
                    actions.add(actionMap.get(p.getUniqueTag()));
                }
            }
        }
    }
}
