/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.recipes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.item.ItemStack;

import buildcraft.api.recipes.IAssemblyRecipeManager;
import buildcraft.api.recipes.IFlexibleRecipe;

public class AssemblyRecipeManager implements IAssemblyRecipeManager {

    public static final AssemblyRecipeManager INSTANCE = new AssemblyRecipeManager();
    private BiMap<String, IFlexibleRecipe<ItemStack>> assemblyRecipes = HashBiMap.create();

    @Override
    public void addRecipe(String id, long powerCost, ItemStack output, Object... input) {
        if (output == null) {
            throw new IllegalArgumentException("Cannot have a null output!");
        }
        addRecipe(id, new FlexibleRecipe<>(id, output, powerCost, 0, input));
    }

    @Override
    public void addRecipe(IFlexibleRecipe<ItemStack> recipe) {
        addRecipe(recipe.getId(), recipe);
    }

    private void addRecipe(String id, IFlexibleRecipe<ItemStack> recipe) {
        if (recipe == null) {
            throw new RuntimeException("Recipe \"" + id + "\" is null!");
        }

        if (assemblyRecipes.containsKey(id)) {
            throw new RuntimeException("Recipe \"" + id + "\" already registered");
        }

        assemblyRecipes.put(recipe.getId(), recipe);
    }

    @Override
    public Collection<IFlexibleRecipe<ItemStack>> getRecipes() {
        return assemblyRecipes.values();
    }

    public IFlexibleRecipe<ItemStack> getRecipe(String id) {
        return assemblyRecipes.get(id);
    }

    @Override
    public void removeRecipe(IFlexibleRecipe<ItemStack> recipe) {
        removeRecipe(recipe.getId());
    }

    @Override
    public void removeRecipe(String id) {
        assemblyRecipes.remove(id);
    }
}
