package com.dynious.refinedrelocation.grid.relocator;

import com.dynious.refinedrelocation.api.APIUtils;
import com.dynious.refinedrelocation.api.item.IItemRelocatorModule;
import com.dynious.refinedrelocation.api.relocator.IItemRelocator;
import com.dynious.refinedrelocation.api.relocator.IRelocatorModule;
import com.dynious.refinedrelocation.api.relocator.RelocatorModuleBase;
import com.dynious.refinedrelocation.client.gui.GuiModuleMultiModule;
import com.dynious.refinedrelocation.container.ContainerMultiModule;
import com.dynious.refinedrelocation.helper.StringHelper;
import com.dynious.refinedrelocation.item.ModItems;
import com.dynious.refinedrelocation.lib.Names;
import com.dynious.refinedrelocation.lib.Resources;
import com.dynious.refinedrelocation.lib.Strings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RelocatorMultiModule extends RelocatorModuleBase {
    private static IIcon icon;
    private List<IRelocatorModule> modules = new ArrayList<>();
    private int currentModule = -1; // -1 is the multi module

    public RelocatorMultiModule() {
        super(new ItemStack(ModItems.relocatorModule, 1, 10));
    }

    public IRelocatorModule getCurrentModule() {
        return currentModule == -1 ? this : modules.get(currentModule);
    }

    public void setCurrentModule(int newModule) {
        currentModule = newModule;
    }

    public List<List<String>> getModuleInformation(NBTTagCompound compound) {
        List<List<String>> moduleInformation = new ArrayList<>();

        NBTTagList list = compound.getTagList("multiModules", 10);
        for (int i = 0; i < list.tagCount() && i <= 3; i++) {
            NBTTagCompound moduleCompound = list.getCompoundTagAt(i);
            IRelocatorModule module = RelocatorModuleRegistry.getModule(moduleCompound.getString("clazzIdentifier"));
            if (module != null) {
                List<String> wailaInfo = module.getWailaInformation(moduleCompound);
                if (!wailaInfo.isEmpty()) {
                    for (int j = 0; j < wailaInfo.size(); j++) {
                        wailaInfo.set(j, StringUtils.repeat(" ", 3) + wailaInfo.get(j));
                    }
                    wailaInfo.add(0, module.getDisplayName());
                    moduleInformation.add(wailaInfo);
                }
            }
        }

        if (list.tagCount() >= 4) {
            List<String> ellipse = new ArrayList<>();
            ellipse.add(StringHelper.getLocalizedString(Strings.ELLIPSE));
            moduleInformation.add(ellipse);
        }

        return moduleInformation;
    }

    @Override
    public String getDisplayName() {
        return StatCollector.translateToLocal("item." + Names.relocatorModule + 10 + ".name");
    }

    public boolean addModule(IRelocatorModule module) {
        for (IRelocatorModule module1 : modules) {
            if (module1.getClass() == module.getClass() || module instanceof RelocatorMultiModule) {
                return false;
            }
        }
        modules.add(module);
        return true;
    }

    public IRelocatorModule removeModule(int index) {
        return modules.remove(index);
    }

    @Override
    public void init(IItemRelocator relocator, int side) {
        for (IRelocatorModule module : modules) {
            if (module != null) {
                module.init(relocator, side);
            }
        }
    }

    @Override
    public boolean onActivated(IItemRelocator relocator, EntityPlayer player, int side, ItemStack stack) {
        if (stack != null && stack.getItem() instanceof IItemRelocatorModule) {
            IRelocatorModule module = ((IItemRelocatorModule) stack.getItem()).getRelocatorModule(stack);
            if (module != null && addModule(module)) {
                module.init(relocator, side);
                if (!player.capabilities.isCreativeMode) {
                    stack.stackSize--;
                }
                return true;
            }
        }
        APIUtils.openRelocatorModuleGUI(relocator, player, side);
        return true;
    }

    @Override
    public void onUpdate(IItemRelocator relocator, int side) {
        for (IRelocatorModule module : modules) {
            if (module != null)
                module.onUpdate(relocator, side);
        }
    }

    @Override
    public ItemStack outputToSide(IItemRelocator relocator, int side, TileEntity inventory, ItemStack stack, boolean simulate) {
        ItemStack returned = stack;
        for (IRelocatorModule module : modules) {
            if (module != null)
                returned = module.outputToSide(relocator, side, inventory, stack, simulate);
            if (returned == null)
                return null;
        }
        return returned;
    }

    @Override
    public void onRedstonePowerChange(boolean isPowered) {
        for (IRelocatorModule module : modules) {
            if (module != null)
                module.onRedstonePowerChange(isPowered);
        }
    }

    @Override
    public int strongRedstonePower(int side) {
        int power = 0;
        for (IRelocatorModule module : modules) {
            if (module != null) {
                int p = module.strongRedstonePower(side);
                if (p > power) {
                    power = p;
                }
            }
        }
        return power;
    }

    @Override
    public boolean connectsToRedstone() {
        for (IRelocatorModule module : modules) {
            if (module != null && module.connectsToRedstone()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getGUI(IItemRelocator relocator, int side, EntityPlayer player) {
        if (currentModule == -1) {
            return new GuiModuleMultiModule(this, relocator, side);
        } else {
            return getCurrentModule().getGUI(relocator, side, player);
        }
    }

    @Override
    public Container getContainer(IItemRelocator relocator, int side, EntityPlayer player) {
        if (currentModule == -1) {
            return new ContainerMultiModule(this, relocator, side);
        } else {
            return getCurrentModule().getContainer(relocator, side, player);
        }
    }

    @Override
    public boolean passesFilter(IItemRelocator relocator, int side, ItemStack stack, boolean input, boolean simulate) {
        for (IRelocatorModule module : modules) {
            if (module != null && !module.passesFilter(relocator, side, stack, input, simulate)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void readFromNBT(IItemRelocator relocator, int side, NBTTagCompound compound) {
        NBTTagList list = compound.getTagList("multiModules", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound1 = list.getCompoundTagAt(i);
            IRelocatorModule module = RelocatorModuleRegistry.getModule(compound1.getString("clazzIdentifier"));
            if (module != null) {
                modules.add(module);
                module.init(relocator, side);
                module.readFromNBT(relocator, side, compound1);
            }
        }
    }

    @Override
    public void writeToNBT(IItemRelocator relocator, int side, NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (IRelocatorModule module : modules) {
            if (module != null) {
                NBTTagCompound compound1 = new NBTTagCompound();
                compound1.setString("clazzIdentifier", RelocatorModuleRegistry.getIdentifier(module.getClass()));
                module.writeToNBT(relocator, side, compound1);
                list.appendTag(compound1);
            }
        }
        compound.setTag("multiModules", list);
    }

    @Override
    public void readClientData(IItemRelocator relocator, int side, NBTTagCompound compound) {
        NBTTagList list = compound.getTagList("multiModules", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound1 = list.getCompoundTagAt(i);
            IRelocatorModule module = RelocatorModuleRegistry.getModule(compound1.getString("clazzIdentifier"));
            if (module != null) {
                modules.add(module);
                module.init(relocator, side);
                module.readClientData(relocator, side, compound1);
            }
        }
    }

    @Override
    public void writeClientData(IItemRelocator relocator, int side, NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (IRelocatorModule module : modules) {
            if (module != null) {
                NBTTagCompound compound1 = new NBTTagCompound();
                compound1.setString("clazzIdentifier", RelocatorModuleRegistry.getIdentifier(module.getClass()));
                module.writeClientData(relocator, side, compound1);
                list.appendTag(compound1);
            }
        }
        compound.setTag("multiModules", list);
    }

    @Override
    public List<ItemStack> getDrops(IItemRelocator relocator, int side) {
        List<ItemStack> drops = new ArrayList<>();
        for (IRelocatorModule module : modules) {
            if (module != null) {
                drops.addAll(module.getDrops(relocator, side));
            }
        }
        drops.add(new ItemStack(ModItems.relocatorModule, 1, 10));
        return drops;
    }

    @Override
    public IIcon getIcon(IItemRelocator relocator, int side) {
        return icon;
    }

    @Override
    public void registerIcons(IIconRegister register) {
        icon = register.registerIcon(Resources.MOD_ID + ":" + "relocatorModuleMulti");
    }

    public List<IRelocatorModule> getModules() {
        return modules;
    }

    public int getModuleCount() {
        return modules.size();
    }
}
