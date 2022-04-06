package com.dynious.refinedrelocation.client.gui.widget.button;

import com.dynious.refinedrelocation.api.tileentity.ISortingInventory;
import com.dynious.refinedrelocation.client.gui.GuiRefinedRelocationContainer;
import com.dynious.refinedrelocation.client.gui.IGuiParent;
import com.dynious.refinedrelocation.helper.GuiHelper;
import com.dynious.refinedrelocation.lib.Strings;
import com.dynious.refinedrelocation.network.packet.gui.MessageGUI;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

public class GuiButtonPriority extends GuiButton {
    private ISortingInventory tile;

    public GuiButtonPriority(IGuiParent parent, int x, int y, ISortingInventory tile) {
        super(parent, x, y, "");
        this.tile = tile;
        setAdventureModeRestriction(true);
    }

    public void setValue(ISortingInventory.Priority priority) {
        String text = "";
        switch (priority) {
            case HIGH:
                text = "+";
                break;
            case NORMAL_HIGH:
                text = "0+";
                break;
            case NORMAL:
                text = "0";
                break;
            case NORMAL_LOW:
                text = "-0";
                break;
            case LOW:
                text = "-";
                break;
        }
        this.label.setText(text);
    }

    @Override
    public void getTooltip(List<String> tooltip, int mouseX, int mouseY) {
        super.getTooltip(tooltip, mouseX, mouseY);
        if (isInsideBounds(mouseX, mouseY)) {
            tooltip.add(StatCollector.translateToLocal(Strings.PRIORITY) + ":");
            tooltip.add("\u00A77" + StatCollector.translateToLocal(tile.getPriority().name().replace('_', '-')));
            tooltip.add("\u00a7e" + StatCollector.translateToLocal(Strings.CLICK_INCREASE));
            tooltip.add("\u00a7e" + StatCollector.translateToLocal(Strings.CLICK_DECREASE));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int type, boolean isShiftKeyDown) {
        if (isInsideBounds(mouseX, mouseY) && (type == 0 || type == 1)) {
            if (!isAdventureModeRestriction() || !GuiRefinedRelocationContainer.isRestrictedAccessWithError()) {
                int amount = type == 0 ? -1 : 1;
                if (tile.getPriority().ordinal() + amount >= 0 && tile.getPriority().ordinal() + amount < ISortingInventory.Priority.values().length) {
                    ISortingInventory.Priority newPriority = ISortingInventory.Priority.values()[tile.getPriority().ordinal() + amount];
                    tile.setPriority(newPriority);
                    GuiHelper.sendByteMessage(MessageGUI.PRIORITY, (byte) newPriority.ordinal());
                    setValue(newPriority);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, type, isShiftKeyDown);
    }

    @Override
    public void update() {
        if (tile != null) {
            setValue(tile.getPriority());
        }
        super.update();
    }
}
