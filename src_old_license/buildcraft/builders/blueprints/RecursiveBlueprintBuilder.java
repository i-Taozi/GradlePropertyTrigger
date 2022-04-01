/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.blueprints;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.Template;
import buildcraft.lib.misc.data.Box;

public class RecursiveBlueprintBuilder {

    private boolean returnedThis = false;
    private BlueprintBase blueprint;
    private RecursiveBlueprintBuilder current;
    private int nextSubBlueprint = 0;
    private ArrayList<NBTTagCompound> subBlueprints = new ArrayList<>();
    private BlockPos pos;
    private EnumFacing dir;
    private World world;
    private Box box = new Box();

    public RecursiveBlueprintBuilder(BlueprintBase iBlueprint, World iWorld, BlockPos pos, EnumFacing iDir) {
        blueprint = iBlueprint;
        subBlueprints = iBlueprint.subBlueprintsNBT;
        world = iWorld;
        this.pos = pos;
        dir = iDir;
    }

    public BptBuilderBase nextBuilder() {
        if (!returnedThis) {
            blueprint = blueprint.adjustToWorld(world, pos, dir);

            returnedThis = true;

            BptBuilderBase builder;

            if (blueprint instanceof Blueprint) {
                builder = new BptBuilderBlueprint((Blueprint) blueprint, world, pos);
            } else if (blueprint instanceof Template) {
                builder = new BptBuilderTemplate(blueprint, world, pos);
            } else {
                return null;
            }

            box.initialize(builder);

            return builder;
        }

        // Free memory associated with this blueprint
        blueprint = null;

        if (current != null) {
            BptBuilderBase builder = current.nextBuilder();

            if (builder != null) {
                return builder;
            }
        }

        if (nextSubBlueprint >= subBlueprints.size()) {
            return null;
        }

        NBTTagCompound nbt = subBlueprints.get(nextSubBlueprint);
        BlueprintBase bpt = BlueprintBase.loadBluePrint(nbt.getCompoundTag("bpt"));

        int nx = box.min().getX() + nbt.getInteger("x");
        int ny = box.min().getY() + nbt.getInteger("y");
        int nz = box.min().getZ() + nbt.getInteger("z");

        EnumFacing nbtDir = EnumFacing.values()[nbt.getByte("dir")];

        current = new RecursiveBlueprintBuilder(bpt, world, new BlockPos(nx, ny, nz), nbtDir);
        nextSubBlueprint++;

        return current.nextBuilder();
    }
}
