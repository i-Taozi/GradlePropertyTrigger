/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;

public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    private static final boolean USE_INDEX = true;

    private Map<Item, Set<IRecipe>> inputIndexMap, outputIndexMap;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack target) {
        final Iterable<IRecipe> recipes;
        if (USE_INDEX) {
            generateInputIndex();
            recipes = inputIndexMap.get(target.getItem());
            if (recipes == null) {
                return ImmutableList.of();
            }
        } else {
            recipes = ForgeRegistries.RECIPES;
        }

        List<GuidePartFactory> list = new ArrayList<>();
        for (IRecipe recipe : recipes) {
            if (checkRecipeUses(recipe, target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    public void generateIndices() {
        if (USE_INDEX) {
            generateInputIndex();
            generateOutputIndex();
        }
    }

    private void generateInputIndex() {
        if (inputIndexMap == null) {
            inputIndexMap = new IdentityHashMap<>();
            for (IRecipe recipe : ForgeRegistries.RECIPES) {
                generateInputIndex0(recipe);
            }
        }
    }

    private void generateInputIndex0(IRecipe recipe) {
        for (Ingredient ing : recipe.getIngredients()) {
            generateIngredientIndex(recipe, ing, inputIndexMap);
        }
    }

    private static void generateIngredientIndex(IRecipe recipe, Ingredient ing, Map<Item, Set<IRecipe>> indexMap) {
        for (ItemStack stack : ing.getMatchingStacks()) {
            appendIndex(stack, recipe, indexMap);
        }
    }

    private static void appendIndex(ItemStack stack, IRecipe recipe, Map<Item, Set<IRecipe>> indexMap) {
        Set<IRecipe> list = indexMap.get(stack.getItem());
        if (list == null) {
            list = new LinkedHashSet<>();
            indexMap.put(stack.getItem(), list);
        }
        list.add(recipe);
    }

    private static boolean checkRecipeUses(IRecipe recipe, @Nonnull ItemStack target) {
        NonNullList<Ingredient> ingrediants = recipe.getIngredients();
        if (ingrediants.isEmpty()) {
            if (recipe instanceof IRecipeViewable) {
                // TODO!
            }
        }
        for (Ingredient ing : ingrediants) {
            if (ing.test(target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(@Nonnull ItemStack target, @Nullable Object in) {
        if (in instanceof ItemStack) {
            return StackUtil.doesEitherStackMatch((ItemStack) in, target);
        } else if (in instanceof List) {
            for (Object obj : (List<?>) in) {
                if (obj instanceof ItemStack) {
                    if (StackUtil.doesEitherStackMatch((ItemStack) obj, target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack target) {
        final Iterable<IRecipe> recipes;
        if (USE_INDEX) {
            generateOutputIndex();
            recipes = outputIndexMap.get(target.getItem());
            if (recipes == null) {
                return ImmutableList.of();
            }

        } else {
            recipes = ForgeRegistries.RECIPES;
        }

        List<GuidePartFactory> list = new ArrayList<>();
        for (IRecipe recipe : recipes) {
            if (checkRecipeOutputs(recipe, target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    private void generateOutputIndex() {
        if (outputIndexMap == null) {
            outputIndexMap = new IdentityHashMap<>();
            for (IRecipe recipe : ForgeRegistries.RECIPES) {
                generateOutputIndex0(recipe);
            }
        }
    }

    private void generateOutputIndex0(IRecipe recipe) {
        if (recipe instanceof IRecipeViewable) {
            ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
            for (ItemStackKey stack : changing.getOptions()) {
                appendIndex(stack.baseStack, recipe, outputIndexMap);
            }
        } else {
            ItemStack output = recipe.getRecipeOutput();
            if (!output.isEmpty()) {
                appendIndex(output, recipe, outputIndexMap);
            }
        }
        for (Ingredient ing : recipe.getIngredients()) {
            generateIngredientIndex(recipe, ing, outputIndexMap);
        }
    }

    private static boolean checkRecipeOutputs(IRecipe recipe, ItemStack target) {
        if (recipe instanceof IRecipeViewable) {
            ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
            if (changing.matches(target)) {
                return true;
            }
        } else {
            ItemStack out = StackUtil.asNonNull(recipe.getRecipeOutput());
            if (OreDictionary.itemMatches(target, out, false) || OreDictionary.itemMatches(out, target, false)) {
                return true;
            }
        }
        return false;
    }
}
