/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.world;

import net.minecraft.util.math.Vec3d;

public class WorldInfo {
    public final String schematic;
    public final WorldLabel[] labels;
    public final Vec3d cameraPos;
    public final Vec3d cameraFacing;

    public WorldInfo(String schematic, WorldLabel[] labels, Vec3d cameraPos, Vec3d cameraFacing) {
        this.schematic = schematic;
        this.labels = labels;
        this.cameraPos = cameraPos;
        this.cameraFacing = cameraFacing;
    }

    public byte[] getSchematic() {
        return new byte[0];
    }
}
