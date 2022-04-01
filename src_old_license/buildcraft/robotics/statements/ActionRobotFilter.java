/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;

import buildcraft.core.lib.inventory.filters.StatementParameterStackFilter;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.inventory.filter.ArrayFluidFilter;
import buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraft.lib.inventory.filter.PassThroughFluidFilter;
import buildcraft.lib.inventory.filter.PassThroughStackFilter;
import buildcraft.lib.misc.LocaleUtil;

public class ActionRobotFilter extends BCStatement implements IActionInternal {

    public ActionRobotFilter() {
        super("buildcraft:robot.work_filter");
        setBuildCraftLocation("robotics", "triggers/action_robot_filter");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.robot.filter");
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    public static Collection<ItemStack> getGateFilterStacks(DockingStation station) {
        ArrayList<ItemStack> result = new ArrayList<>();

        for (StatementSlot slot : station.getActiveActions()) {
            if (slot.statement instanceof ActionRobotFilter) {
                for (IStatementParameter p : slot.parameters) {
                    if (p != null && p instanceof StatementParameterItemStack) {
                        StatementParameterItemStack param = (StatementParameterItemStack) p;
                        ItemStack stack = param.getItemStack();

                        if (stack != null) {
                            result.add(stack);
                        }
                    }
                }
            }
        }

        return result;
    }

    public static IStackFilter getGateFilter(DockingStation station) {
        Collection<ItemStack> stacks = getGateFilterStacks(station);

        if (stacks.size() == 0) {
            return new PassThroughStackFilter();
        } else {
            return new ArrayStackOrListFilter(stacks.toArray(new ItemStack[stacks.size()]));
        }
    }

    public static IFluidFilter getGateFluidFilter(DockingStation station) {
        Collection<ItemStack> stacks = getGateFilterStacks(station);

        if (stacks.size() == 0) {
            return new PassThroughFluidFilter();
        } else {
            return new ArrayFluidFilter(stacks.toArray(new ItemStack[stacks.size()]));
        }
    }

    public static boolean canInteractWithItem(DockingStation station, IStackFilter filter, Class<?> actionClass) {
        boolean actionFound = false;

        for (StatementSlot s : station.getActiveActions()) {
            if (actionClass.isAssignableFrom(s.statement.getClass())) {
                StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

                if (!param.hasFilter() || param.matches(filter)) {
                    actionFound = true;
                    break;
                }
            }
        }

        return actionFound;
    }

    public static boolean canInteractWithFluid(DockingStation station, IFluidFilter filter, Class<?> actionClass) {
        boolean actionFound = false;

        for (StatementSlot s : station.getActiveActions()) {
            if (actionClass.isAssignableFrom(s.statement.getClass())) {
                StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

                if (!param.hasFilter()) {
                    actionFound = true;
                    break;
                } else {
                    for (ItemStack stack : param.getStacks()) {
                        if (stack != null) {
                            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);

                            if (fluid != null && filter.matches(fluid)) {
                                actionFound = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return actionFound;

    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }
}
