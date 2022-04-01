package buildcraft.lib.client.guide.entry;

import java.util.List;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.parts.contents.PageLinkStatement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;

public class PageEntryStatement extends PageValueType<IStatement> {

    public static final PageEntryStatement INSTANCE = new PageEntryStatement();

    private static final JsonTypeTags TRIGGER_TAGS = new JsonTypeTags("buildcraft.guide.contents.triggers");
    private static final JsonTypeTags ACTION_TAGS = new JsonTypeTags("buildcraft.guide.contents.actions");

    @Override
    public Class<IStatement> getEntryClass() {
        return IStatement.class;
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer, Profiler prof) {
        for (IStatement statement : new TreeMap<>(StatementManager.statements).values()) {
            if (!GuideManager.INSTANCE.objectsAdded.add(statement)) {
                continue;
            }

            final JsonTypeTags parent;

            if (statement instanceof ITrigger) {
                parent = TRIGGER_TAGS;
            } else if (statement instanceof IAction) {
                parent = ACTION_TAGS;
            } else {
                continue;
            }

            consumer.addChild(parent, new PageLinkStatement(false, statement));
        }
    }

    @Override
    public OptionallyDisabled<PageEntry<IStatement>> deserialize(ResourceLocation name, JsonObject json,
        JsonDeserializationContext ctx) {
        String stmntName = JsonUtils.getString(json, "statement");
        IStatement stmnt = StatementManager.statements.get(stmntName);
        if (stmnt == null) {
            throw new JsonSyntaxException("Unknown statement '" + stmntName + "'");
        }
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, stmnt));
    }

    @Override
    public List<String> getTooltip(IStatement value) {
        return value.getTooltip();
    }

    @Override
    public String getTitle(IStatement value) {
        List<String> tooltip = value.getTooltip();
        if (tooltip.isEmpty()) {
            return value.getClass().toString();
        } else {
            return tooltip.get(0);
        }
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable(IStatement value) {
        return (x, y) -> GuiElementStatementSource.drawGuiSlot(value, x, y);
    }
}
