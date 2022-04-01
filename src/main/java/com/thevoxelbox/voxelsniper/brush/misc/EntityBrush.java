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
package com.thevoxelbox.voxelsniper.brush.misc;

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.brush.Brush;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

/**
 * Spawns entities
 */
@Brush.BrushInfo(
    name = "Entity",
    aliases = {"en", "entity"},
    permission = "voxelsniper.brush.entity",
    category = Brush.BrushCategory.MISC
)
public class EntityBrush extends Brush {

    private EntityType entityType = EntityTypes.ZOMBIE;

    public EntityBrush() {
    }

    private void spawn(final SnipeData v) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
            for (int x = 0; x < v.getBrushSize(); x++) {
                Entity e = this.world.createEntity(this.entityType, this.lastBlock.getBlockPosition());
                this.world.spawnEntity(e);
            }
        }
    }

    @Override
    protected final void arrow(final SnipeData v) {
        this.spawn(v);
    }

    @Override
    protected final void powder(final SnipeData v) {
        this.spawn(v);
    }

    @Override
    public final void info(final Message vm) {
        vm.custom(TextColors.LIGHT_PURPLE, "Entity brush" + " (", TextColors.DARK_PURPLE, this.entityType.getName(), TextColors.LIGHT_PURPLE, ")");
        vm.size();
    }

    @Override
    public final void parameters(final String[] par, final SnipeData v) {
        if (par.length == 0 || par[0].equalsIgnoreCase("info")) {
            v.sendMessage(TextColors.AQUA, "Available entity types:");
            StringBuilder types = new StringBuilder();
            for (EntityType type : Sponge.getRegistry().getAllOf(EntityType.class)) {
                types.append(", ").append(type.getId().replace("minecraft:", ""));
            }
            v.sendMessage(types.toString().substring(2));
        } else {
            Optional<EntityType> selection = Sponge.getRegistry().getType(EntityType.class, par[0]);
            if (!selection.isPresent()) {
                v.sendMessage(TextColors.RED, "This is not a valid entity!");
            } else {
                this.entityType = selection.get();
                v.sendMessage(TextColors.GREEN, "Entity type set to " + this.entityType.getName());
            }
        }
    }
}
