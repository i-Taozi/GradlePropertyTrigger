/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.transport.statements.TriggerPipeContents;

public class PipeTriggerProvider implements ITriggerProvider {

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> result, IStatementContainer container) {

        Pipe<?> pipe = null;
        TileEntity tile = container.getTile();

        if (tile instanceof TileGenericPipe) {
            pipe = ((TileGenericPipe) tile).pipe;
        }

        if (pipe == null) {
            return;
        }

        switch (((TileGenericPipe) tile).getPipeType()) {
            case ITEM:
                result.add(TriggerPipeContents.PipeContents.empty.trigger);
                result.add(TriggerPipeContents.PipeContents.containsItems.trigger);
                break;
            case FLUID:
                result.add(TriggerPipeContents.PipeContents.empty.trigger);
                result.add(TriggerPipeContents.PipeContents.containsFluids.trigger);
                break;
            case POWER:
                result.add(TriggerPipeContents.PipeContents.empty.trigger);
                result.add(TriggerPipeContents.PipeContents.containsEnergy.trigger);
                result.add(TriggerPipeContents.PipeContents.tooMuchEnergy.trigger);
                result.add(TriggerPipeContents.PipeContents.requestsEnergy.trigger);
                break;
            case STRUCTURE:
                break;
        }

        if (container instanceof Gate) {
            ((Gate) container).addTriggers(result);
        }
    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, EnumFacing side, TileEntity tile) {

    }
}
