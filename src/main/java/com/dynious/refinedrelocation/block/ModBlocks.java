package com.dynious.refinedrelocation.block;

import com.dynious.refinedrelocation.api.ModObjects;
import com.dynious.refinedrelocation.item.*;
import com.dynious.refinedrelocation.lib.Mods;
import com.dynious.refinedrelocation.lib.Names;
import com.dynious.refinedrelocation.lib.Settings;
import com.dynious.refinedrelocation.compat.AE2Helper;
import com.dynious.refinedrelocation.compat.EE3Helper;
import com.dynious.refinedrelocation.compat.IronChestHelper;
import com.dynious.refinedrelocation.compat.JabbaHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModBlocks
{
    public static BlockExtender blockExtender;
    public static BlockBuffer buffer;
    public static BlockSortingChest sortingChest;
    public static BlockSortingIronChest sortingIronChest;
    public static BlockSortingConnector sortingConnector;
    public static BlockFilteringHopper filteringHopper;
    public static BlockSortingBarrel sortingBarrel;
    public static BlockRelocationPortal relocationPortal;
    public static BlockPlayerRelocatorBase playerRelocatorBase;
    public static BlockPowerLimiter powerLimiter;
    public static BlockSortingAlchemicalChest sortingAlchemicalChest;
    public static BlockRelocator relocator;

    public static void init()
    {
        blockExtender = new BlockExtender();
        buffer = new BlockBuffer();
        sortingChest = new BlockSortingChest();
        sortingConnector = new BlockSortingConnector();
        filteringHopper = new BlockFilteringHopper();
        relocationPortal = new BlockRelocationPortal();
        playerRelocatorBase = new BlockPlayerRelocatorBase();
        powerLimiter = new BlockPowerLimiter();

        ModObjects.blockExtender = new ItemStack(blockExtender);
        ModObjects.advancedBlockExtender = new ItemStack(blockExtender, 1, 1);
        ModObjects.filteredBlockExtender = new ItemStack(blockExtender, 1, 2);
        ModObjects.advancedFilteredBlockExtender = new ItemStack(blockExtender, 1, 3);
        ModObjects.wirelessBlockExtender = new ItemStack(blockExtender, 1, 4);
        ModObjects.buffer = new ItemStack(buffer);
        ModObjects.advancedBuffer = new ItemStack(buffer, 1, 1);
        ModObjects.filteredBuffer = new ItemStack(buffer, 1, 2);
        ModObjects.sortingChest = new ItemStack(sortingChest);
        ModObjects.sortingConnector = new ItemStack(sortingConnector);
        ModObjects.sortingInterface = new ItemStack(sortingConnector, 1, 1);
        ModObjects.sortingInputPane = new ItemStack(sortingConnector, 1, 2);
        ModObjects.sortingImporter = ModObjects.sortingInputPane;
        ModObjects.filteringHopper = new ItemStack(filteringHopper);
        ModObjects.relocationPortal = new ItemStack(relocationPortal);
        ModObjects.playerRelocatorBase = new ItemStack(playerRelocatorBase);
        ModObjects.relocationController = ModObjects.playerRelocatorBase;
        ModObjects.powerLimiter = new ItemStack(powerLimiter);

        GameRegistry.registerBlock(blockExtender, ItemBlockExtender.class, Names.blockExtender);
        GameRegistry.registerBlock(buffer, ItemBlockBuffer.class, Names.buffer);
        GameRegistry.registerBlock(sortingChest, Names.sortingChest);
        GameRegistry.registerBlock(sortingConnector, ItemBlockSortingConnector.class, Names.sortingConnector);
        GameRegistry.registerBlock(filteringHopper, Names.filteringHopper);
        GameRegistry.registerBlock(relocationPortal, Names.relocationPortal);
        GameRegistry.registerBlock(playerRelocatorBase, ItemBlockPlayerRelocatorBase.class, Names.playerRelocatorBase);
        GameRegistry.registerBlock(powerLimiter, ItemBlockPowerLimiter.class, Names.powerLimiter);

        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 4, 0), "igi", "geg", "ioi", 'i', Items.iron_ingot, 'o', Blocks.obsidian, 'g', Blocks.glass_pane, 'e', Items.ender_pearl);
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 1), "r r", " b ", "r r", 'r', Blocks.redstone_block, 'b', new ItemStack(blockExtender, 1, 0));
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 2), "g g", " b ", "g g", 'g', Items.gold_ingot, 'b', new ItemStack(blockExtender, 1, 0));
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 3), "g g", " b ", "g g", 'g', Items.gold_ingot, 'b', new ItemStack(blockExtender, 1, 1));
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 3), "r r", " b ", "r r", 'r', Blocks.redstone_block, 'b', new ItemStack(blockExtender, 1, 2));

        if (!Settings.DISABLE_WIRELESS_BLOCK_EXTENDER)
        {
            GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 4), "d d", " b ", "d d", 'd', Items.diamond, 'b', new ItemStack(blockExtender, 1, 3));
        }

        GameRegistry.addShapedRecipe(new ItemStack(buffer, 4, 0), "igi", "geg", "igi", 'i', Items.iron_ingot, 'g', Blocks.glass_pane, 'e', Items.ender_pearl);
        GameRegistry.addShapedRecipe(new ItemStack(buffer, 1, 1), "r r", " b ", "r r", 'r', Blocks.redstone_block, 'b', new ItemStack(buffer, 1, 0));
        GameRegistry.addShapedRecipe(new ItemStack(buffer, 1, 2), "g g", " b ", "g g", 'g', Items.gold_ingot, 'b', new ItemStack(buffer, 1, 0));

        GameRegistry.addShapedRecipe(new ItemStack(sortingChest, 1, 0), "g g", " b ", "p p", 'g', Items.gold_ingot, 'b', new ItemStack(Blocks.chest), 'p', Blocks.planks);
        if (!Settings.DISABLE_SORTING_TO_NORMAL)
            GameRegistry.addShapelessRecipe(new ItemStack(Blocks.chest), new ItemStack(sortingChest, 1, 0));

        GameRegistry.addShapedRecipe(new ItemStack(sortingConnector, 4, 0), "gsg", "sis", "gsg", 'g', Items.gold_nugget, 's', Blocks.stone, 'i', Items.iron_ingot);
        GameRegistry.addShapedRecipe(new ItemStack(sortingConnector, 1, 1), "g g", " i ", "g g", 'g', Items.gold_ingot, 'i', new ItemStack(sortingConnector, 4, 0));
        GameRegistry.addShapedRecipe(new ItemStack(sortingConnector, 1, 2), "rgr", "sis", "rgr", 'g', Items.gold_ingot, 's', Items.redstone, 'r', Items.iron_ingot, 'i', new ItemStack(sortingConnector, 4, 0));

        GameRegistry.addShapedRecipe(new ItemStack(filteringHopper), "g g", " h ", "g g", 'g', Items.gold_ingot, 'h', new ItemStack(Blocks.hopper));
        GameRegistry.addShapedRecipe(new ItemStack(powerLimiter), "iri", "rbr", "iri", 'i', Items.iron_ingot, 'r', Items.redstone, 'b', Blocks.redstone_block);


        if (!Settings.DISABLE_PLAYER_RELOCATOR)
        {
            GameRegistry.addShapedRecipe(new ItemStack(playerRelocatorBase), "ded", "ece", "ded", 'd', Items.diamond, 'e', Items.ender_eye, 'c', Items.compass);
        }

        if (Mods.IS_IRON_CHEST_LOADED)
        {
            IronChestHelper.addIronChestBlocks();
            IronChestHelper.addIronChestRecipes();
        }

        if (Mods.IS_JABBA_LOADED)
        {
            JabbaHelper.addBarrelBlock();
            JabbaHelper.addBarrelRecipes();
        }

        if (Mods.IS_EE3_LOADED)
        {
            EE3Helper.addEE3Blocks();
            EE3Helper.addEE3Recipes();
        }

        if (!Mods.IS_FMP_LOADED)
        {
            relocator = new BlockRelocator();
            ModObjects.relocator = new ItemStack(relocator);
            GameRegistry.registerBlock(relocator, ItemBlockRelocator.class, Names.relocator);
            GameRegistry.addShapedRecipe(new ItemStack(relocator, 4, 0), "igi", "g g", "igi", 'i', Items.iron_ingot, 'g', Blocks.glass_pane);
        }

        if (Mods.IS_AE2_LOADED)
        {
            AE2Helper.addAERecipes();
        }
    }
}
