/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.BiMap;

import net.minecraft.item.ItemStack;

import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;

import buildcraft.core.recipes.IntegrationRecipeBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.ItemGate;

public class GateExpansionRecipe extends IntegrationRecipeBC {
    private static final BiMap<IGateExpansion, ItemStack> recipes = (BiMap<IGateExpansion, ItemStack>) GateExpansions.getRecipesForPostInit();

    public GateExpansionRecipe() {
        super(25000);
    }

    @Override
    public boolean isValidInput(ItemStack input) {
        return input.getItem() instanceof ItemGate;
    }

    @Override
    public boolean isValidExpansion(ItemStack input, ItemStack expansion) {
        if (StackUtil.isMatchingItem(EnumRedstoneChipset.RED.getStack(), expansion, true, true)) {
            return true;
        }
        for (ItemStack s : recipes.values()) {
            if (StackUtil.isMatchingItem(s, expansion, true, true)) {
                IGateExpansion exp = recipes.inverse().get(s);
                if (exp != null) {
                    GateDefinition.GateMaterial material = ItemGate.getMaterial(input);
                    int numTP = material != null ? material.numTriggerParameters : 0;
                    int numAP = material != null ? material.numActionParameters : 0;
                    if (exp.canAddToGate(numTP, numAP)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> generateExampleInput() {
        return Collections.unmodifiableList(ItemGate.getAllGates());
    }

    @Override
    public List<ItemStack> generateExampleOutput() {
        ArrayList<ItemStack> list = new ArrayList<>();
        ArrayList<IGateExpansion> exps = new ArrayList<>();
        int combinations = recipes.size();
        for (IGateExpansion exp : recipes.keySet()) {
            exps.add(exp);
        }
        for (int i = 0; i < (1 << combinations); i++) {
            for (GateDefinition.GateLogic l : GateDefinition.GateLogic.VALUES) {
                for (GateDefinition.GateMaterial m : GateDefinition.GateMaterial.VALUES) {
                    ItemStack s = ItemGate.makeGateItem(m, l);
                    for (int j = 0; j < combinations; j++) {
                        if (((i >> j) & 1) != 0) {
                            ItemGate.addGateExpansion(s, exps.get(j));
                        }
                    }
                    list.add(s);
                }
            }
        }
        return list;
    }

    @Override
    public List<List<ItemStack>> generateExampleExpansions() {
        ArrayList<List<ItemStack>> list = new ArrayList<>();
        ArrayList<ItemStack> list2 = new ArrayList<>();
        list2.addAll(recipes.values());
        list.add(list2);
        return list;
    }

    @Override
    public ItemStack craft(ItemStack input, List<ItemStack> expansions, boolean preview) {
        ItemStack output = input.copy();
        output.stackSize = 1;
        int expansionsAdded = 0;

        for (ItemStack chipset : expansions) {
            if (StackUtil.isMatchingItem(EnumRedstoneChipset.RED.getStack(), chipset, true, true)) {
                ItemGate.setLogic(output, ItemGate.getLogic(output) == GateDefinition.GateLogic.AND ? GateDefinition.GateLogic.OR
                    : GateDefinition.GateLogic.AND);
                expansionsAdded++;
                continue;
            }
            for (ItemStack expansion : recipes.values()) {
                if (StackUtil.isMatchingItem(chipset, expansion, true, true) && !ItemGate.hasGateExpansion(output, recipes.inverse().get(
                        expansion))) {
                    if (!preview) {
                        chipset.stackSize--;
                    }
                    ItemGate.addGateExpansion(output, recipes.inverse().get(expansion));
                    expansionsAdded++;
                    break;
                }
            }
        }

        if (expansionsAdded > 0) {
            return output;
        } else {
            return null;
        }
    }
}
