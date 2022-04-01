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

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.Undo;
import com.thevoxelbox.voxelsniper.brush.Brush;
import com.thevoxelbox.voxelsniper.util.BlockBuffer;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Brush.BrushInfo(
    name = "Shell Voxel",
    aliases = {"shv", "shellvoxel"},
    permission = "voxelsniper.brush.shellvoxel",
    category = Brush.BrushCategory.SHAPE
)
public class ShellVoxelBrush extends Brush {

    public ShellVoxelBrush() {
    }
    
    // @Usability: add parameters for checking for any solid block rather than replace material

    private void vShell(final SnipeData v, Location<World> targetBlock) {
        int brushSize = (int) Math.round(v.getBrushSize());

        int tx = targetBlock.getBlockX();
        int ty = targetBlock.getBlockY();
        int tz = targetBlock.getBlockZ();

        BlockBuffer buffer = new BlockBuffer(new Vector3i(-brushSize, -brushSize, -brushSize), new Vector3i(brushSize, brushSize, brushSize));

        for (int x = -brushSize; x <= brushSize; x++) {
            int x0 = tx + x;
            for (int y = -brushSize; y <= brushSize; y++) {
                int y0 = ty + y;
                for (int z = -brushSize; z <= brushSize; z++) {
                    int z0 = tz + z;
                    if (y0 <= 0 || y0 >= WORLD_HEIGHT) {
                        continue;
                    }
                    if (this.world.getBlock(x0, y0, z0) != v.getReplaceState()) {
                        continue;
                    }
                    int blocks = 0;
                    if (this.world.getBlock(x0 + 1, y0, z0) == v.getReplaceState()) {
                        blocks++;
                    }
                    if (this.world.getBlock(x0 - 1, y0, z0) == v.getReplaceState()) {
                        blocks++;
                    }
                    if (this.world.getBlock(x0, y0 + 1, z0) == v.getReplaceState()) {
                        blocks++;
                    }
                    if (this.world.getBlock(x0, y0 - 1, z0) == v.getReplaceState()) {
                        blocks++;
                    }
                    if (this.world.getBlock(x0, y0, z0 + 1) == v.getReplaceState()) {
                        blocks++;
                    }
                    if (this.world.getBlock(x0, y0, z0 - 1) == v.getReplaceState()) {
                        blocks++;
                    }
                    if (blocks == 6) {
                        buffer.set(x, y, z, v.getVoxelState());
                    }
                }
            }
        }

        this.undo = new Undo(buffer.getBlockCount());
        // apply the buffer to the world
        for (int x = -brushSize; x <= brushSize; x++) {
            int x0 = x + tx;
            for (int y = -brushSize; y <= brushSize; y++) {
                int y0 = y + ty;
                for (int z = -brushSize; z <= brushSize; z++) {
                    int z0 = z + tz;
                    if (buffer.contains(x, y, z)) {
                        setBlockState(x0, y0, z0, buffer.get(x, y, z));
                    }
                }
            }
        }
        v.owner().storeUndo(this.undo);
        this.undo = null;
    }

    @Override
    protected final void arrow(final SnipeData v) {
        vShell(v, this.targetBlock);
    }

    @Override
    protected final void powder(final SnipeData v) {
        vShell(v, this.targetBlock);
    }

    @Override
    public final void info(final Message vm) {
        vm.brushName(this.info.name());
        vm.size();
        vm.voxel();
        vm.replace();
    }
}
