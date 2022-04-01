/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.client.guide.ref.GuideGroupSet;
import buildcraft.lib.client.guide.ref.GuideGroupSet.GroupDirection;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StringUtilBC;

public class GuidePage extends GuidePageBase {
    public final ImmutableList<GuidePart> parts;
    public final String title;
    public final GuideChapter chapterContents;
    public final PageValue<?> entry;

    public GuidePage(GuiGuide gui, List<GuidePart> parts, PageValue<?> entry) {
        super(gui);
        this.title = StringUtilBC.formatStringForWhite(entry.title);
        this.chapterContents = new GuideChapterContents(gui);
        this.entry = entry;
        List<GuidePart> from = parts;
        // Defensive copy as we modify it
        parts = new ArrayList<>();
        // First: Prepend the list with a chapter title
        parts.add(new GuideChapterWithin(gui, title));

        // Re-add everything that we missed before
        parts.addAll(from);

        // Now add relevant groups
        List<GuidePartGroup> linksToOther = new ArrayList<>();
        List<GuidePartGroup> linksToThis = new ArrayList<>();
        PageValue<?> value = entry.copyToValue();
        for (GuideGroupSet set : GuideGroupManager.sets.values()) {
            if (set.sources.contains(value)) {
                linksToOther.add(new GuidePartGroup(gui, set, GroupDirection.SRC_TO_ENTRY));
            } else if (set.entries.contains(value)) {
                linksToThis.add(new GuidePartGroup(gui, set, GroupDirection.ENTRY_TO_SRC));
            }
        }

        // Ensure we don't get duplicates if they have been manually specified earlier
        linksToOther.removeAll(parts);
        linksToThis.removeAll(parts);

        if (!linksToOther.isEmpty()) {
            parts.add(new GuideChapterWithin(gui, LocaleUtil.localize("buildcraft.guide.meta.group.linking_to")));
            for (GuidePartGroup g : linksToOther) {
                parts.add(g);
                parts.add(new GuidePartNewPage(gui));
            }
        }

        if (!linksToThis.isEmpty()) {
            parts.add(new GuideChapterWithin(gui, LocaleUtil.localize("buildcraft.guide.meta.group.linked_from")));
            for (GuidePartGroup g : linksToThis) {
                parts.add(g);
                parts.add(new GuidePartNewPage(gui));
            }
        }

        addTypeSpecific(gui, parts, entry);

        this.parts = ImmutableList.copyOf(parts);
        setupChapters();
    }

    private static <T> void addTypeSpecific(GuiGuide gui, List<GuidePart> parts, PageValue<T> entry) {
        entry.type.addPageEntries(entry.value, gui, parts);
    }

    @Override
    public List<GuideChapter> getChapters() {
        List<GuideChapter> list = new ArrayList<>();
        list.add(chapterContents);
        for (GuidePart part : parts) {
            if (part instanceof GuideChapter) {
                list.add((GuideChapter) part);
            }
        }
        return list;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        for (GuidePart part : parts) {
            part.setFontRenderer(fontRenderer);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuidePart part : parts) {
            part.updateScreen();
        }
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        super.renderPage(x, y, width, height, index);
        PagePosition part = new PagePosition(0, 0);
        for (GuidePart guidePart : parts) {
            part = guidePart.renderIntoArea(x, y, width, height, part, index);
            if (numPages != -1 && part.page > index) {
                break;
            }
        }
        if (numPages == -1) {
            numPages = part.newPage().page;
        }
    }

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton,
        int index, boolean isEditing) {
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);

        PagePosition part = new PagePosition(0, 0);
        for (GuidePart guidePart : parts) {
            part = guidePart.handleMouseClick(x, y, width, height, part, index, mouseX, mouseY);

            if (numPages != -1 && part.page > index) {
                break;
            }
        }
    }
}
