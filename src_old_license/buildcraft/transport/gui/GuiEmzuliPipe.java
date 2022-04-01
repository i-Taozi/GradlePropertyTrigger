/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.pipes.PipeItemsEmzuli;

public class GuiEmzuliPipe extends GuiBuildCraft {

    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/pipe_emzuli.png");
    IInventory filterInventory;
    PipeItemsEmzuli pipe;

    public GuiEmzuliPipe(EntityPlayer player, PipeItemsEmzuli pipe) {
        super(new ContainerEmzuliPipe(player, pipe), pipe.getFilters(), TEXTURE);

        this.pipe = pipe;
        filterInventory = pipe.getFilters();

        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        String title = LocaleUtil.localize("gui.pipes.emzuli.title");
        fontRendererObj.drawString(title, (xSize - fontRendererObj.getStringWidth(title)) / 2, 6, 0x404040);
        fontRendererObj.drawString(LocaleUtil.localize("gui.inventory"), 8, ySize - 93, 0x404040);
    }
}
