/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import buildcraft.lib.gui.ISimpleDrawable;

/** Stores information about a single line of text. This may be displayed as more than a single line though. */
public class PageLine implements Comparable<PageLine> {
    /** Can be any of the boxes, any icon with dimensions different to these will render incorrectly. */
    public ISimpleDrawable startIcon;
    public ISimpleDrawable startIconHovered;
    public final int indent;
    /** This will be wrapped automatically when it is rendered. */
    public final String text;
    public final boolean link;

    @Nullable
    public final Supplier<List<String>> tooltipSupplier;

    public PageLine(int indent, String text, boolean isLink) {
        this(null, null, indent, text, isLink);
    }

    public PageLine(ISimpleDrawable startIcon, ISimpleDrawable startIconHovered, int indent, String text,
        boolean isLink) {
        this(startIcon, startIconHovered, indent, text, isLink, null);
    }

    public PageLine(ISimpleDrawable startIcon, ISimpleDrawable startIconHovered, int indent, String text, boolean link,
        @Nullable Supplier<List<String>> tooltipSupplier) {
        if (text == null) throw new NullPointerException("text");
        this.startIcon = startIcon;
        this.startIconHovered = startIconHovered;
        this.indent = indent;
        this.text = text;
        this.link = link;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public String toString() {
        return "PageLine [indent = " + indent + ", text=" + text + "]";
    }

    @Override
    public int compareTo(PageLine o) {
        return text.toLowerCase().compareTo(o.text.toLowerCase());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + indent;
        result = prime * result + (link ? 1231 : 1237);
        result = prime * result + ((startIcon == null) ? 0 : startIcon.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PageLine other = (PageLine) obj;
        if (indent != other.indent) return false;
        if (link != other.link) return false;
        if (startIcon == null) {
            if (other.startIcon != null) return false;
        } else if (!startIcon.equals(other.startIcon)) return false;
        if (text == null) {
            if (other.text != null) return false;
        } else if (!text.equals(other.text)) return false;
        return true;
    }

    @Nullable
    public List<String> getTooltip() {
        return tooltipSupplier == null ? null : tooltipSupplier.get();
    }
}
