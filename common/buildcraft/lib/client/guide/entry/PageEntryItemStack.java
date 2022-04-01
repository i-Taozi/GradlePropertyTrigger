package buildcraft.lib.client.guide.entry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.GuidePageRegistry;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemPermutations;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.registry.RegistryConfig;

public class PageEntryItemStack extends PageValueType<ItemStackValueFilter> {

    public static final PageEntryItemStack INSTANCE = new PageEntryItemStack();
    private static final JsonTypeTags TAGS = new JsonTypeTags("buildcraft.guide.contents.item_stacks");

    @Override
    public Class<ItemStackValueFilter> getEntryClass() {
        return ItemStackValueFilter.class;
    }

    @Override
    protected boolean isValid(ItemStackValueFilter typed) {
        return !typed.stack.baseStack.isEmpty();
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer, Profiler prof) {

        // For now, we can always re-enable this as a last resort fix.
        boolean limitDomains = ForgeRegistries.ITEMS.getKeys().size() > BCLibConfig.guideItemSearchLimit;

        Set<String> domains = new HashSet<>();
        // Always show all of minecraft's items
        domains.add("minecraft");
        // And all of BuildCraft's items as we haven't created guide entries for them yet
        for (BCModules mod : BCModules.values()) {
            domains.add(mod.getModId());
        }
        domains.addAll(GuidePageRegistry.INSTANCE.getSourceDomains());

        if (limitDomains) {
            BCLog.logger.warn(
                "[lib.guide] Limiting the domians of items to only: " + domains + " because there are "
                    + ForgeRegistries.ITEMS.getKeys().size() + " items!"
            );
        }

        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation regName = item.getRegistryName();
            if (regName == null || (limitDomains && !domains.contains(regName.getResourceDomain()))) {
                continue;
            }
            if (!GuideManager.INSTANCE.objectsAdded.add(item)) {
                continue;
            }
            NonNullList<ItemStack> stacks = NonNullList.create();
            prof.startSection("search");
            item.getSubItems(CreativeTabs.SEARCH, stacks);
            prof.endStartSection("itr_search");
            if (stacks.size() > 200) {
                // Likely a "super-item" which is constructed from a different registry
                // and so it has thousands of useless permutations
                // Instead lets replace it with a custom tooltip
                consumer.addChild(TAGS, PageLinkItemPermutations.create(false, stacks, prof));
                prof.endSection();
                BCLog.logger.info(
                    "[lib.guide] Squished " + regName + " and all of it's " + stacks.size()
                        + " variants down into one page entry."
                );
                continue;
            }
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);

                try {
                    consumer.addChild(TAGS, PageLinkItemStack.create(false, stack, prof));
                } catch (RuntimeException e) {
                    throw new Error(
                        "Failed to create a page link for " + item.getRegistryName() + " " + item.getClass() + " ("
                            + stack.serializeNBT() + ")", e
                    );
                }
            }
            prof.endSection();
        }
    }

    @Override
    public OptionallyDisabled<PageLink> createLink(String to, Profiler prof) {
        OptionallyDisabled<ItemStack> stackq = MarkdownPageLoader.parseItemStack(to);
        if (stackq.isPresent()) {
            return new OptionallyDisabled<>(PageLinkItemStack.create(true, stackq.get(), prof));
        } else {
            return new OptionallyDisabled<>(stackq.getDisabledReason());
        }
    }

    @Override
    public OptionallyDisabled<PageEntry<ItemStackValueFilter>> deserialize(ResourceLocation name, JsonObject json,
        JsonDeserializationContext ctx) {
        JsonElement jStack = json.get("stack");
        if (jStack == null) {
            throw new JsonSyntaxException(
                "Expected either a string or an object for 'stack', but got nothing for " + json
            );
        }
        final ItemStack stack;
        final boolean matchMeta, matchNbt;
        if (jStack.isJsonPrimitive()) {
            String str = JsonUtils.getString(jStack, "stack");
            if (str.startsWith("{") && str.endsWith("}")) {
                stack = MarkdownPageLoader.loadComplexItemStack(str.substring(1, str.length() - 1));
                stack.setCount(1);
                matchMeta = true;
                matchNbt = stack.hasTagCompound();
            } else {
                if (str.startsWith("(") && str.endsWith(")")) {
                    str = str.substring(1, str.length() - 1);
                }
                ResourceLocation loc = new ResourceLocation(str);
                Item item = ForgeRegistries.ITEMS.getValue(loc);
                if (item == null) {
                    if (RegistryConfig.hasItemBeenDisabled(loc)) {
                        return new OptionallyDisabled<>("The item '" + loc + "' has been disabled.");
                    }
                    throw new JsonSyntaxException("Invalid item: " + str);
                }
                stack = new ItemStack(item);
                matchMeta = false;
                matchNbt = false;
            }
        } else {
            // TODO!
            throw new AbstractMethodError("// TODO: Implement this!");
        }
        if (stack.isEmpty()) {
            throw new JsonSyntaxException("Unknown item " + jStack);
        }
        ItemStackValueFilter filter = new ItemStackValueFilter(new ItemStackKey(stack), matchMeta, matchNbt);
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, filter));
    }

    @Override
    public String getTitle(ItemStackValueFilter value) {
        return value.stack.baseStack.getDisplayName();
    }

    @Override
    public List<String> getTooltip(ItemStackValueFilter value) {
        return GuiUtil.getFormattedTooltip(value.stack.baseStack);
    }

    @Override
    public boolean matches(ItemStackValueFilter entry, Object obj) {
        if (obj instanceof ItemStackValueFilter) {
            obj = ((ItemStackValueFilter) obj).stack.baseStack;
        }
        if (obj instanceof ItemStackKey) {
            obj = ((ItemStackKey) obj).baseStack;
        }
        if (obj instanceof ItemStack) {
            ItemStack base = entry.stack.baseStack;
            ItemStack test = (ItemStack) obj;
            if (base.isEmpty() || test.isEmpty()) {
                return false;
            }
            if (base.getItem() != test.getItem()) {
                return false;
            }
            if (entry.matchMeta) {
                if (base.getMetadata() != test.getMetadata()) {
                    return false;
                }
            }
            if (entry.matchNbt) {
                if (!ItemStack.areItemStackTagsEqual(base, test)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable(ItemStackValueFilter value) {
        return new GuiStack(value.stack.baseStack);
    }

    @Override
    public Object getBasicValue(ItemStackValueFilter value) {
        return value.stack.baseStack.getItem();
    }

    @Override
    public void addPageEntries(ItemStackValueFilter value, GuiGuide gui, List<GuidePart> parts) {
        XmlPageLoader.appendAllCrafting(value.stack.baseStack, parts, gui, new Profiler());
    }
}
