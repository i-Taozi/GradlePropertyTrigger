package com.dynious.refinedrelocation.client.gui.widget;

import com.dynious.refinedrelocation.client.gui.IGuiParent;
import com.dynious.refinedrelocation.client.gui.widget.button.GuiButton;
import com.dynious.refinedrelocation.lib.Strings;
import com.dynious.refinedrelocation.tileentity.TileBlockExtender;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiDisguise extends GuiWidgetBase {
    protected GuiButton button;
    protected TileBlockExtender tile;

    public GuiDisguise(IGuiParent parent, int x, int y, int w, int h, TileBlockExtender tile) {
        super(parent, x, y, w, h);
        this.tile = tile;
        button = new GuiButton(this, x, y, w, h, "button_masked", null);
    }

    @Override
    public void getTooltip(List<String> tooltip, int mouseX, int mouseY) {
        super.getTooltip(tooltip, mouseX, mouseY);
        if (isInsideBounds(mouseX, mouseY)) {
            if (tile.getDisguise() != null) {
                Block disguisedAs = tile.getDisguise();
                int meta = tile.blockDisguisedMetadata;
                ItemStack item = new ItemStack(disguisedAs, 0, meta);
                tooltip.add(StatCollector.translateToLocalFormatted(Strings.DISGUISED, item.getDisplayName()));
                for (String s : StatCollector.translateToLocal(Strings.DISGUISED_INFO).split("\\\\n")) {
                    tooltip.add("\u00A77" + s);
                }
            } else {
                tooltip.add(StatCollector.translateToLocal(Strings.UNDISGUISED));
                for (String s : StatCollector.translateToLocal(Strings.UNDISGUISED_INFO).split("\\\\n")) {
                    tooltip.add("\u00A77" + s);
                }
            }
        }
    }

    @Override
    public void drawBackground(int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        if (tile.getDisguise() != null) {
            Block disguisedAs = tile.getDisguise();
            int meta = tile.blockDisguisedMetadata;

            IIcon icon = disguisedAs.getIcon(tile.getConnectedDirection().ordinal(), meta);
            if (icon != null) {
                this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                this.drawTexturedModelRectFromIcon(x, y, icon, w, h);
            }
        }
        if (tile.getDisguise() != null && isInsideBounds(mouseX, mouseY))
            return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        super.drawBackground(mouseX, mouseY);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
