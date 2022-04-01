/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import buildcraft.BuildCraftCore;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.builders.TileBuilder;
import buildcraft.core.blueprints.RequirementItemStack;
import buildcraft.core.client.CoreIconProvider;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.lib.config.OverridableConfigOption;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.misc.LocaleUtil;

public class GuiBuilder extends GuiAdvancedInterface {

    /** Options for controlling the position of extra icons are laid out */
    private static final OverridableConfigOption OPTION_EXTRA_X_POS = new OverridableConfigOption("gui.builder", "extra.x", "80");
    private static final OverridableConfigOption OPTION_EXTRA_Y_POS = new OverridableConfigOption("gui.builder", "extra.y", "44");

    private static final ResourceLocation REGULAR_TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/builder.png");
    private static final ResourceLocation BLUEPRINT_TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/builder_blueprint.png");
    private TileBuilder builder;
    private GuiButton selectedButton;

    public GuiBuilder(EntityPlayer player, TileBuilder builder) {
        super(new ContainerBuilder(player, builder), builder, BLUEPRINT_TEXTURE);
        this.builder = builder;
        xSize = 256;
        ySize = 225;

        resetNullSlots(6 * 4);

        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 4; ++j) {
                slots.set(i * 4 + j, new SlotBuilderRequirement(this, 179 + j * 18, 18 + i * 18));
            }
        }
    }

    private ContainerBuilder getContainerBuilder() {
        return (ContainerBuilder) getContainer();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        drawCenteredString(LocaleUtil.localize("tile.builderBlock.name"), 178 / 2, 16, 0x404040);
        if (builder.getStackInSlot(0) != null) {
            fontRendererObj.drawString(LocaleUtil.localize("gui.building.resources"), 8, 60, 0x404040);
            fontRendererObj.drawString(LocaleUtil.localize("gui.inventory"), 8, ySize - 97, 0x404040);
            fontRendererObj.drawString(LocaleUtil.localize("gui.needed"), 178, 7, 0x404040);
            fontRendererObj.drawString(LocaleUtil.localize("gui.building.fluids"), 178, 133, 0x404040);
        }

        drawTooltips(par1, par2);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private void drawTooltips(int par1, int par2) {
        int top = guiTop + 145;
        for (int i = 0; i < builder.fluidTanks.length; i++) {
            int left = guiLeft + 179 + 18 * i;
            if (par1 >= left && par2 >= top && par1 < (left + 16) && par2 < (left + 47)) {
                List<String> fluidTip = new ArrayList<>();
                Tank tank = builder.fluidTanks[i];
                if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                    fluidTip.add(tank.getFluid().getLocalizedName());
                    if (!BuildCraftCore.hideFluidNumbers) {
                        fluidTip.add(EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC + tank.getFluid().amount + " mB");
                    }
                } else {
                    fluidTip.add(StatCollector.translateToLocal("gui.fluidtank.empty"));
                }
                drawHoveringText(fluidTip, par1 - guiLeft, par2 - guiTop, fontRendererObj);
                return;
            }
        }

        drawTooltipForSlotAt(par1, par2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        // We cannot do super here due to some crazy shenanigans with a dynamically
        // resized GUI.
        GlStateManager.color(1f, 1f, 1f, 1f);
        boolean isBlueprint = builder.getStackInSlot(0) != null;

        mc.renderEngine.bindTexture(REGULAR_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, ySize);
        mc.renderEngine.bindTexture(BLUEPRINT_TEXTURE);
        if (isBlueprint) {
            drawTexturedModalRect(guiLeft + 169, guiTop, 169, 0, 256 - 169, ySize);
        }

        List<RequirementItemStack> needs = builder.getNeededItems();

        if (needs != null) {
            if (needs.size() > slots.size()) {
                getContainerBuilder().scrollbarWidget.hidden = false;
                getContainerBuilder().scrollbarWidget.setLength((needs.size() - slots.size() + 3) / 4);
            } else {
                getContainerBuilder().scrollbarWidget.hidden = true;
            }

            int offset = getContainerBuilder().scrollbarWidget.getPosition() * 4;
            for (int s = 0; s < slots.size(); s++) {
                int ts = offset + s;
                if (ts >= needs.size()) {
                    ((SlotBuilderRequirement) slots.get(s)).stack = null;
                } else {
                    ((SlotBuilderRequirement) slots.get(s)).stack = needs.get(ts);
                }
            }

            for (GuiButton b : (List<GuiButton>) buttonList) {
                b.visible = true;
            }
        } else {
            getContainerBuilder().scrollbarWidget.hidden = true;
            for (AdvancedSlot slot : slots) {
                ((SlotBuilderRequirement) slot).stack = null;
            }
            for (GuiButton b : (List<GuiButton>) buttonList) {
                b.visible = false;
            }
        }

        if (builder.getControlMode() == Mode.Off) {
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            int extraX = guiLeft + OPTION_EXTRA_X_POS.getAsInt();
            int extraY = guiTop + OPTION_EXTRA_Y_POS.getAsInt();
            drawTexturedModalRect(extraX, extraY, CoreIconProvider.TURNED_OFF.getSprite(), 16, 16);
        }

        drawWidgets(x, y);

        if (isBlueprint) {
            drawBackgroundSlots(x, y);

            for (int i = 0; i < builder.fluidTanks.length; i++) {
                Tank tank = builder.fluidTanks[i];
                if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                    drawFluid(tank.getFluid(), guiLeft + 179 + 18 * i, guiTop + 145, 16, 47, tank.getCapacity());
                }
            }

            mc.renderEngine.bindTexture(BLUEPRINT_TEXTURE);

            for (int i = 0; i < builder.fluidTanks.length; i++) {
                Tank tank = builder.fluidTanks[i];
                if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                    drawTexturedModalRect(guiLeft + 179 + 18 * i, guiTop + 145, 0, 54, 16, 47);
                }
            }
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    @Override
    public void initGui() {
        super.initGui();
        for (int i = 0; i < 4; i++) {
            buttonList.add(new BuilderEraseButton(i, guiLeft + 178 + 18 * i, guiTop + 197, 18, 18));
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int eventType) {
        super.mouseReleased(mouseX, mouseY, eventType);
        if (this.selectedButton != null && eventType == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }
    }

    private class BuilderEraseButton extends GuiButton {
        private boolean clicked;

        public BuilderEraseButton(int id, int x, int y, int width, int height) {
            super(id, x, y, width, height, null);
        }

        @Override
        public boolean mousePressed(Minecraft mc, int x, int y) {
            if (super.mousePressed(mc, x, y)) {
                selectedButton = this;
                clicked = true;
                BuildCraftCore.instance.sendToServer(new PacketCommand(builder, "eraseFluidTank", new CommandWriter() {
                    @Override
                    public void write(ByteBuf data) {
                        data.writeInt(id);
                    }
                }));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void mouseReleased(int x, int y) {
            super.mouseReleased(x, y);
            clicked = false;
        }

        @Override
        public void drawButton(Minecraft mc, int x, int y) {
            if (!visible) {
                return;
            }
            // hovered
            this.hovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;

            mc.renderEngine.bindTexture(BLUEPRINT_TEXTURE);
            drawTexturedModalRect(xPosition, yPosition, 0, (clicked ? 1 : this.hovered ? 2 : 0) * 18, 18, 18);
            mouseDragged(mc, x, y);
        }
    }
}
