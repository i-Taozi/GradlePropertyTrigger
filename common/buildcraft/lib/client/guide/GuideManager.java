/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Stopwatch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Language;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.statements.IStatement;

import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.entry.IEntryLinkConsumer;
import buildcraft.lib.client.guide.entry.ItemStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.entry.PageValueType;
import buildcraft.lib.client.guide.loader.IPageLoader;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePageStandInRecipes;
import buildcraft.lib.client.guide.parts.contents.ContentsNode;
import buildcraft.lib.client.guide.parts.contents.ContentsNodeGui;
import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import buildcraft.lib.client.guide.parts.contents.IContentsNode;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import buildcraft.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraft.lib.client.guide.parts.recipe.GuideCraftingRecipes;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.guide.GuideContentsData;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.ProfilerUtil;
import buildcraft.lib.misc.data.ProfilerBC;
import buildcraft.lib.misc.data.ProfilerBC.IProfilerSection;
import buildcraft.lib.misc.search.ISuffixArray;
import buildcraft.lib.misc.search.SimpleSuffixArray;
import buildcraft.lib.misc.search.VanillaSuffixArray;

public enum GuideManager implements IResourceManagerReloadListener {
    INSTANCE;

    public static final String DEFAULT_LANG = "en_us";
    public static final Map<String, IPageLoader> PAGE_LOADERS = new HashMap<>();
    public static final GuideContentsData BOOK_ALL_DATA = new GuideContentsData(null);
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.guide.loader");

    private final List<PageEntry<?>> entries = new ArrayList<>();

    /** The keys are the partial paths, not the full ones!
     * <p>
     * For example a partial path might be "buildcraftcore:wrench.md" and the */
    private final Map<ResourceLocation, GuidePageFactory> pages = new HashMap<>();
    private final Map<ItemStack, GuidePageFactory> generatedPages = new HashMap<>();

    /** Internal use only! Use {@link #addChild(ResourceLocation, JsonTypeTags, PageLink)} instead! */
    public ISuffixArray<PageLink> quickSearcher;
    /** Every {@link PageLink} that has been added to {@link #quickSearcher}. */
    private final Set<PageLink> pageLinksAdded = new HashSet<>();
    private final Map<GuideBook, Map<TypeOrder, ContentsNode>> contents = new HashMap<>();

    /** Every object added to the guide. Generally this means {@link Item}'s and {@link IStatement}'s. */
    public final Set<Object> objectsAdded = new HashSet<>();

    private boolean isInReload = false;

    static {
        PAGE_LOADERS.put("md", MarkdownPageLoader.INSTANCE);
    }

    public void onRegistryReload(EventBuildCraftReload.FinishLoad event) {
        if (isInReload) {
            // We reload the book registry while reloading this registry, so we don't need to reload it twice.
            // hang on... isn't this a bit hacky?
            // I feel like we shouldn't allow reloading everything by default?
            return;
        }
        if (event.manager.isLoadingAll()) {
            return;
        }
        if (event.reloadingRegistries.contains(GuideBookRegistry.INSTANCE)) {
            reload();
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        reload(resourceManager);
    }

    public void reload() {
        reload(Minecraft.getMinecraft().getResourceManager());
    }

    private void reload(IResourceManager resourceManager) {
        if (isInReload) {
            throw new IllegalStateException("Cannot reload while we are reloading!");
        }
        try {
            isInReload = true;
            reload0(resourceManager);
        } finally {
            isInReload = false;
        }
    }

    private void reload0(IResourceManager resourceManager) {
        Profiler prof = new Profiler();
        prof.profilingEnabled = DEBUG;
        prof.startSection("root");
        prof.startSection("reload");
        Stopwatch watch = Stopwatch.createStarted();

        GuideGroupManager.get("lols", "hi");
        prof.startSection("book_registry");
        GuideBookRegistry.INSTANCE.reload();
        prof.endStartSection("page_registry");
        GuidePageRegistry.INSTANCE.reload();
        prof.endStartSection("setup");
        entries.clear();
        // Don't add permanent as we need the resource domain
        GuidePageRegistry manager = GuidePageRegistry.INSTANCE;
        Map<GuideBook, Set<String>> domains = new HashMap<>();
        domains.put(null, new HashSet<>());
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            domains.put(book, new HashSet<>());
        }

        prof.endStartSection("index_crafting");
        GuideCraftingRecipes.INSTANCE.generateIndices();
        prof.endStartSection("add_pages");

        for (PageEntry<?> entry : manager.getAllEntries()) {
            domains.get(null).add(entry.typeTags.domain);
            GuideBook book = GuideBookRegistry.INSTANCE.getBook(entry.book.toString());
            Set<String> domainSet = domains.get(book);
            if (domainSet != null && book != null) {
                domainSet.add(entry.typeTags.domain);
            }
            entries.add(entry);
        }

        prof.endStartSection("generate_books");
        BOOK_ALL_DATA.generate(domains.get(null));
        for (Entry<GuideBook, Set<String>> entry : domains.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            entry.getKey().data.generate(entry.getValue());
        }
        pages.clear();

        prof.endStartSection("load_lang");
        Language currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
        String langCode;
        if (currentLanguage == null) {
            BCLog.logger.warn("Current language was null!");
            langCode = DEFAULT_LANG;
        } else {
            langCode = currentLanguage.getLanguageCode();
        }

        // load the default ones
        loadLangInternal(resourceManager, DEFAULT_LANG, prof);
        // replace any existing with the new ones.

        if (!DEFAULT_LANG.equals(langCode)) {
            loadLangInternal(resourceManager, langCode, prof);
        }

        prof.endStartSection("contents_page");
        generateContentsPage(prof);
        prof.endSection();

        watch.stop();
        long time = watch.elapsed(TimeUnit.MICROSECONDS);
        int p = entries.size();
        int a = pages.size();
        int e = p - a;
        prof.endSection();
        prof.endSection();
        if (prof.profilingEnabled) {
            BCLog.logger.info("[lib.guide] " + pageLinksAdded.size() + " search terms");
            BCLog.logger.info(
                "[lib.guide] Loaded " + p + " possible and " + a + " actual guide pages (" + e + " not found) in "
                    + time / 1000 + "ms."
            );
            BCLog.logger.info("[lib.guide] Performance information for guide loading:");
            ProfilerUtil.logProfilerResults(prof, "root", time * 1000);
            BCLog.logger.info("[lib.guide] End of guide loading performance information. (" + time / 1000 + "ms)");
        }
    }

    private void loadLangInternal(IResourceManager resourceManager, String lang, Profiler prof) {
        ProfilerBC p = new ProfilerBC(prof);
        main_iteration: for (Entry<ResourceLocation, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE
            .getReloadableEntryMap().entrySet()) {
            ResourceLocation entryKey = mapEntry.getKey();
            String domain = entryKey.getResourceDomain();
            String path = "compat/buildcraft/guide/" + lang + "/" + entryKey.getResourcePath();

            for (Entry<String, IPageLoader> entry : PAGE_LOADERS.entrySet()) {
                ResourceLocation fLoc = new ResourceLocation(domain, path + "." + entry.getKey());

                try (IProfilerSection s = p.start("get_resource"); InputStream stream = resourceManager.getResource(
                    fLoc
                ).getInputStream(); IProfilerSection l = p.start("load")) {
                    GuidePageFactory factory = entry.getValue().loadPage(stream, entryKey, mapEntry.getValue(), prof);
                    // put the original page in so that the different lang variants override it
                    pages.put(entryKey, factory);
                    if (GuideManager.DEBUG) {
                        BCLog.logger.info("[lib.guide.loader] Loaded page '" + entryKey + "'.");
                    }
                    continue main_iteration;
                } catch (FileNotFoundException f) {
                    // Ignore it, we'll log this later
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

            if (pages.containsKey(entryKey)) {
                // We are overriding a different language so it's ok if we miss something.
                continue;
            }

            String endings;
            if (PAGE_LOADERS.size() == 1) {
                endings = PAGE_LOADERS.keySet().iterator().next();
            } else {
                endings = PAGE_LOADERS.keySet().toString();
            }
            BCLog.logger.warn(
                "[lib.guide.loader] Unable to load guide page '" + entryKey + "' (full path = '" + domain + ":" + path
                    + "." + endings + "') because we couldn't find any of the valid paths in any resource pack!"
            );
        }
    }

    private void generateContentsPage(Profiler prof) {
        prof.startSection("clear");
        objectsAdded.clear();
        contents.clear();
        prof.endStartSection("setup");
        genTypeMap(null);
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            genTypeMap(book);
        }
        quickSearcher = false ? new VanillaSuffixArray<>() : new SimpleSuffixArray<>();
        pageLinksAdded.clear();
        prof.endStartSection("add_pages");

        for (Entry<ResourceLocation, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            ResourceLocation partialLocation = mapEntry.getKey();
            GuidePageFactory entryFactory = GuideManager.INSTANCE.getFactoryFor(partialLocation);

            PageEntry<?> entry = mapEntry.getValue();
            String translatedTitle = entry.title;
            ISimpleDrawable icon = entry.createDrawable();
            PageLine line = new PageLine(icon, icon, 2, translatedTitle, true);

            if (entryFactory != null) {
                objectsAdded.add(entry.getBasicValue());
                PageLinkNormal pageLink = new PageLinkNormal(line, true, entry.getTooltip(), entryFactory);
                prof.startSection("add_child");
                addChild(entry.book, entry.typeTags, pageLink);
                prof.endSection();
            }
        }

        prof.endStartSection("add_default");
        ContentsNode othersRoot = new ContentsNode(LocaleUtil.localize("buildcraft.guide.contents.all_group"), 0);
        for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : contents.entrySet()) {
            @Nullable
            GuideBook book = bookEntry.getKey();
            if (book != null && !book.appendAllEntries) {
                continue;
            }
            for (ContentsNode root : bookEntry.getValue().values()) {
                root.addChild(othersRoot);
            }
        }
        final IEntryLinkConsumer adder = (tags, page) -> {
            assert tags.domain == null;
            assert tags.subType == null;
            prof.startSection("add_child");
            if (pageLinksAdded.add(page)) {
                quickSearcher.add(page, page.getSearchName());
            }
            String title = LocaleUtil.localize(tags.type);
            IContentsNode subNode = othersRoot.getChild(title);
            if (subNode instanceof ContentsNode) {
                subNode.addChild(page);
            } else if (subNode == null) {
                ContentsNode subContents = new ContentsNode(title, 1);
                othersRoot.addChild(subContents);
                subContents.addChild(page);
            } else {
                throw new IllegalStateException("Unknown node type " + subNode.getClass());
            }
            prof.endSection();
        };
        for (PageValueType<?> type : GuidePageRegistry.INSTANCE.types.values()) {
            prof.startSection(type.getClass().getName().replace('.', '/'));
            type.iterateAllDefault(adder, prof);
            prof.endSection();
        }

        prof.endStartSection("generate_quick_search");
        quickSearcher.generate(prof);

        prof.endStartSection("sort");
        for (Map<TypeOrder, ContentsNode> map : contents.values()) {
            for (ContentsNode node : map.values()) {
                node.sort();
            }
        }
        prof.endSection();
    }

    private void genTypeMap(GuideBook book) {
        Map<TypeOrder, ContentsNode> map = new HashMap<>();
        contents.put(book, map);
        for (TypeOrder order : GuiGuide.SORTING_TYPES) {
            map.put(order, new ContentsNode("root", -1));
        }
    }

    private void addChild(ResourceLocation bookType, JsonTypeTags tags, PageLink page) {
        if (pageLinksAdded.add(page)) {
            quickSearcher.add(page, page.getSearchName());
        }

        for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : contents.entrySet()) {
            @Nullable
            GuideBook book = bookEntry.getKey();

            if (book != null && !book.name.equals(bookType)) {
                continue;
            }
            Map<TypeOrder, ContentsNode> map = bookEntry.getValue();
            for (Entry<TypeOrder, ContentsNode> entry : map.entrySet()) {
                TypeOrder order = entry.getKey();
                String[] ordered = tags.getOrdered(order);
                ContentsNode[] nodePath = new ContentsNode[ordered.length];
                ContentsNode node = entry.getValue();
                for (int i = 0; i < ordered.length; i++) {
                    String title = LocaleUtil.localize(ordered[i]);
                    IContentsNode subNode = node.getChild(title);
                    if (subNode instanceof ContentsNode) {
                        node = (ContentsNode) subNode;
                        nodePath[i] = node;
                    } else if (subNode == null) {
                        ContentsNode subContents = new ContentsNode(title, i);
                        node.addChild(subContents);
                        node = subContents;
                        nodePath[i] = node;
                    } else {
                        throw new IllegalStateException("Unknown node type " + subNode.getClass());
                    }
                }
                if (nodePath.length == 0) {
                    node.addChild(page);
                } else {
                    nodePath[nodePath.length - 1].addChild(page);
                }
            }
        }
    }

    @Nullable
    public GuidePageFactory getFactoryFor(ResourceLocation partialLocation) {
        return pages.get(partialLocation);
    }

    @Nullable
    public GuidePageFactory getFactoryFor(Object value) {
        if (value instanceof ItemStackValueFilter) {
            value = ((ItemStackValueFilter) value).stack.baseStack;
        } else if (value instanceof ItemStackKey) {
            value = ((ItemStackKey) value).baseStack;
        }
        if (value instanceof ItemStack) {
            return getPageFor((ItemStack) value);
        }
        return getFactoryFor(getEntryFor(value));
    }

    public static ResourceLocation getEntryFor(Object obj) {
        for (Entry<ResourceLocation, PageEntry<?>> entry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            if (entry.getValue().matches(obj)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Nonnull
    public GuidePageFactory getPageFor(@Nonnull ItemStack stack) {
        // TODO: Make this generation much more flexible!
        // (Basically return a stand-in page that contains groups, recipes, and all known info)
        // Or should that be implicit for *all* pages? Yes?
        // (Specifically all that extend [GuidePage] as that won't include the contents page)
        // This implies merging GuidePage up into GuidePageEntry and deleting GuidePageStandInRecipes
        // we will also need to ensure we don't generate groups or recipes multiple times.
        // Although we do need to generate the info for it first and cache it?
        // Also the "Recipes" chapter title needs a JEI integration button!
        ResourceLocation entry = getEntryFor(stack);
        if (entry != null) {
            GuidePageFactory factory = getFactoryFor(entry);
            if (factory != null) {
                return factory;
            }
        }
        // Create a dummy page for the stack
        return generatedPages.computeIfAbsent(stack, GuidePageStandInRecipes::createFactory);
    }

    public ContentsNodeGui getGuiContents(GuiGuide gui, GuidePageContents guidePageContents, TypeOrder sortingOrder) {
        Map<TypeOrder, ContentsNode> map = contents.get(gui.book);
        if (map == null) {
            throw new IllegalStateException("Unknown book " + gui.book);
        }
        ContentsNode node = map.get(sortingOrder);
        if (node == null) {
            throw new IllegalStateException("Unknown sorting order " + sortingOrder);
        }
        node.resetVisibility();

        return new ContentsNodeGui(gui, node);
    }
}
