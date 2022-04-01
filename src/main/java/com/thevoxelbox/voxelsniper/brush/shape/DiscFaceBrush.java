/*
 * This file is part of VoxelSniper, licensed under the MIT License (MIT).
 *
 * Copyright (c) The VoxelBox <http://thevoxelbox.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.thevoxelbox.voxelsniper.brush.shape;

import com.flowpowered.math.GenericMath;
import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.Undo;
import com.thevoxelbox.voxelsniper.brush.Brush;
import com.thevoxelbox.voxelsniper.brush.PerformBrush;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * A disc aligned to the the target block face.
 */
@Brush.BrushInfo(
    name = "Disc Face",
    aliases = {"df", "discface"},
    permission = "voxelsniper.brush.discface",
    category = Brush.BrushCategory.SHAPE
)
public class DiscFaceBrush extends PerformBrush {

    public DiscFaceBrush() {
    }

    private void disc(final SnipeData v, Location<World> targetBlock, Direction axis) {
        double brushSize = v.getBrushSize();
        double brushSizeSquared = brushSize * brushSize;

        int tx = targetBlock.getBlockX();
        int ty = targetBlock.getBlockY();
        int tz = targetBlock.getBlockZ();

        this.undo = new Undo(GenericMath.floor(Math.PI * (brushSize + 1) * (brushSize + 1)));
        int size = GenericMath.floor(brushSize) + 1;
        // @Cleanup Should wrap this within a block worker so that it works
        // better with the cause tracker
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                if (x * x + z * z < brushSizeSquared) {
                    if (axis == Direction.UP) {
                        perform(v, x + tx, ty, z + tz);
                    } else if (axis == Direction.NORTH) {
                        perform(v, x + tx, z + ty, tz);
                    } else if (axis == Direction.EAST) {
                        perform(v, tx, x + ty, z + tz);
                    }
                }
            }
        }

        v.owner().storeUndo(this.undo);
        this.undo = null;
    }

    private void pre(final SnipeData v, Location<World> target) {
        if (this.lastBlock.getBlockY() != this.targetBlock.getBlockY()) {
            disc(v, target, Direction.UP);
        } else if (this.lastBlock.getBlockX() != this.targetBlock.getBlockX()) {
            disc(v, target, Direction.EAST);
        } else if (this.lastBlock.getBlockZ() != this.targetBlock.getBlockZ()) {
            disc(v, target, Direction.NORTH);
        }
    }

    @Override
    protected final void arrow(final SnipeData v) {
        this.pre(v, this.targetBlock);
    }

    @Override
    protected final void powder(final SnipeData v) {
        this.pre(v, this.lastBlock);
    }

    @Override
    public final void info(final Message vm) {
        vm.brushName(this.info.name());
        vm.size();
    }
}
