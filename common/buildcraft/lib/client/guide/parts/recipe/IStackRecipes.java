/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

/** Defines a stack recipe lookup - implementations should register with {@link RecipeLookupHelper} to be used by the
 * guide for usages and recipes. */
public interface IStackRecipes {
    List<GuidePartFactory> getUsages(@Nonnull ItemStack stack);

    List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack);
}
