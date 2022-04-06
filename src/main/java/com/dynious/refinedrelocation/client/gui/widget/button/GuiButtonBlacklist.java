package com.dynious.refinedrelocation.client.gui.widget.button;

import com.dynious.refinedrelocation.client.gui.GuiFiltered;
import com.dynious.refinedrelocation.lib.Strings;
import com.dynious.refinedrelocation.network.NetworkHandler;
import com.dynious.refinedrelocation.network.packet.filter.MessageSetFilterBlacklist;
import net.minecraft.util.StatCollector;

import java.util.List;

public class GuiButtonBlacklist extends GuiButtonToggle {
    private final GuiFiltered parent;

    public GuiButtonBlacklist(GuiFiltered parent, int x, int y) {
        super(parent, x, y, 16, 16, "button_whitelist_small", "button_blacklist_small", null, null);
        this.parent = parent;
        update();
        setAdventureModeRestriction(true);
    }

    @Override
    protected void onStateChangedByUser(boolean newState) {
        int selectedFilterIndex = parent.getSelectedFilterIndex();
        if (selectedFilterIndex != -1) {
            NetworkHandler.INSTANCE.sendToServer(new MessageSetFilterBlacklist(selectedFilterIndex, newState));
        }
    }

    @Override
    public void getTooltip(List<String> tooltip, int mouseX, int mouseY) {
        super.getTooltip(tooltip, mouseX, mouseY);
        if (isInsideBounds(mouseX, mouseY)) {
            tooltip.add(StatCollector.translateToLocal(getState() ? Strings.BLACKLIST : Strings.WHITELIST));
            String[] tooltipLines = StatCollector.translateToLocal(getState() ? Strings.BLACKLIST_DESC : Strings.WHITELIST_DESC).split("\\\\n");
            for (String tooltipLine : tooltipLines) {
                tooltip.add("\u00a77" + tooltipLine);
            }
            tooltip.add("\u00a7b" + StatCollector.translateToLocal(Strings.FILTER_ORDER));
            tooltip.add("\u00a7e" + StatCollector.translateToLocal(Strings.CLICK_TO_TOGGLE));
        }
    }

    @Override
    public void update() {
        int selectedFilterIndex = parent.getSelectedFilterIndex();
        if (selectedFilterIndex >= 0 && selectedFilterIndex < parent.getFilter().getFilterCount()) {
            setState(parent.getFilter().getFilterAtIndex(parent.getSelectedFilterIndex()).isBlacklist());
        }
        super.update();
    }

    @Override
    public boolean isInsideBounds(int x, int y) {
        return parent.hasFilterSelected() && super.isInsideBounds(x, y);
    }

    @Override
    public void drawBackground(int mouseX, int mouseY) {
        if (parent.hasFilterSelected()) {
            super.drawBackground(mouseX, mouseY);
        }
    }
}
