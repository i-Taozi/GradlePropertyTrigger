/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blueprints.*;
import buildcraft.api.core.JavaTools;
import buildcraft.api.library.LibraryAPI;
import buildcraft.api.statements.StatementManager;
import buildcraft.builders.*;
import buildcraft.builders.blueprints.RealBlueprintDeployer;
import buildcraft.builders.schematics.*;
import buildcraft.builders.statements.BuildersActionProvider;
import buildcraft.core.*;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.schematics.*;
import buildcraft.core.builders.schematics.SchematicBlockCreative;
import buildcraft.core.network.EntityIds;
import buildcraft.lib.config.RestartRequirement;

// @Mod(name = "BuildCraft Builders", version = DefaultProps.VERSION, useMetadata = false, modid =
// "BuildCraft|Builders",
// dependencies = DefaultProps.DEPENDENCY_CORE)
@Deprecated
public class BuildCraftBuilders extends BuildCraftMod {

//    @Mod.Instance("BuildCraft|Builders")
    public static BuildCraftBuilders instance;

    public static BlockConstructionMarker constructionMarkerBlock;
    public static BlockFiller fillerBlock;
    public static BlockBuilder builderBlock;
    public static BlockArchitect architectBlock;
    public static BlockBlueprintLibrary libraryBlock;
    public static BlockQuarry quarryBlock;
    public static BlockFrame frameBlock;
    public static ItemBlueprintTemplate templateItem;
    public static ItemBlueprintStandard blueprintItem;

    public static Achievement architectAchievement;
    public static Achievement libraryAchievement;
    public static Achievement blueprintAchievement;
    public static Achievement builderAchievement;
    public static Achievement templateAchievement;
    public static Achievement chunkDestroyerAchievement;

    public static BlueprintServerDatabase serverDB;
    public static LibraryDatabase clientDB;

    public static boolean debugPrintSchematicList = false;
    public static boolean dropBrokenBlocks = false;

    public static boolean quarryLoadsChunks = true;
    public static boolean quarryOneTimeUse = false;

    private String oldBlueprintServerDir, blueprintClientDir;

    public class QuarryChunkloadCallback implements ForgeChunkManager.OrderedLoadingCallback {
        @Override
        public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
            for (ForgeChunkManager.Ticket ticket : tickets) {
                int quarryX = ticket.getModData().getInteger("quarryX");
                int quarryY = ticket.getModData().getInteger("quarryY");
                int quarryZ = ticket.getModData().getInteger("quarryZ");
                BlockPos pos = new BlockPos(quarryX, quarryY, quarryZ);

                Block block = world.getBlockState(pos).getBlock();
                if (block == quarryBlock) {
                    TileQuarry tq = (TileQuarry) world.getTileEntity(pos);
                    tq.forceChunkLoading(ticket);
                }
            }
        }

        @Override
        public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world, int maxTicketCount) {
            List<ForgeChunkManager.Ticket> validTickets = Lists.newArrayList();
            for (ForgeChunkManager.Ticket ticket : tickets) {
                int quarryX = ticket.getModData().getInteger("quarryX");
                int quarryY = ticket.getModData().getInteger("quarryY");
                int quarryZ = ticket.getModData().getInteger("quarryZ");
                BlockPos pos = new BlockPos(quarryX, quarryY, quarryZ);

                Block block = world.getBlockState(pos).getBlock();
                if (block == quarryBlock) {
                    validTickets.add(ticket);
                }
            }
            return validTickets;
        }
    }

    @Mod.EventHandler
    public void loadConfiguration(FMLPreInitializationEvent evt) {
        BuildCraftCore.mainConfigManager.register("blueprints.serverDatabaseDirectory", "\"$MINECRAFT" + File.separator + "config" + File.separator + "buildcraft" + File.separator + "blueprints" + File.separator + "server\"",
                "DEPRECATED - USED ONLY FOR COMPATIBILITY", RestartRequirement.GAME);
        BuildCraftCore.mainConfigManager.register("blueprints.clientDatabaseDirectory", "\"$MINECRAFT" + File.separator + "blueprints\"", "Location for the client blueprint database (used by the Electronic Library).",
                RestartRequirement.NONE);

        BuildCraftCore.mainConfigManager.register("general.markerRange", 64, "Set the maximum marker range.", RestartRequirement.NONE);
        BuildCraftCore.mainConfigManager.register("general.quarry.oneTimeUse", false, "Should the quarry only be usable once after placing?", RestartRequirement.NONE);
        BuildCraftCore.mainConfigManager.register("general.quarry.doChunkLoading", true, "Should the quarry keep the chunks it is working on loaded?", RestartRequirement.NONE);

        BuildCraftCore.mainConfigManager.register("builders.dropBrokenBlocks", false, "Should the builder and filler drop the cleared blocks?", RestartRequirement.NONE);

        BuildCraftCore.mainConfigManager.get("blueprints.serverDatabaseDirectory").setShowInGui(false);
        BuildCraftCore.mainConfigManager.get("general.markerRange").setMinValue(8).setMaxValue(64);

        serverDB = new BlueprintServerDatabase();
        clientDB = new LibraryDatabase();

        reloadConfig(RestartRequirement.GAME);

        Property printSchematicList = BuildCraftCore.mainConfiguration.get("debug", "printBlueprintSchematicList", false);
        debugPrintSchematicList = printSchematicList.getBoolean();
    }

    public void reloadConfig(RestartRequirement restartType) {
        if (restartType == RestartRequirement.GAME) {
            reloadConfig(RestartRequirement.WORLD);
        } else if (restartType == RestartRequirement.WORLD) {
            oldBlueprintServerDir = BuildCraftCore.mainConfigManager.get("blueprints.serverDatabaseDirectory").getString();
            oldBlueprintServerDir = JavaTools.stripSurroundingQuotes(replacePathVariables(oldBlueprintServerDir));

            reloadConfig(RestartRequirement.NONE);
        } else {
            quarryOneTimeUse = BuildCraftCore.mainConfigManager.get("general.quarry.oneTimeUse").getBoolean();
            quarryLoadsChunks = BuildCraftCore.mainConfigManager.get("general.quarry.doChunkLoading").getBoolean();

            blueprintClientDir = BuildCraftCore.mainConfigManager.get("blueprints.clientDatabaseDirectory").getString();
            blueprintClientDir = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintClientDir));
            clientDB.init(new String[] { blueprintClientDir, getDownloadsDir() }, blueprintClientDir);

            DefaultProps.MARKER_RANGE = BuildCraftCore.mainConfigManager.get("general.markerRange").getInt();

            dropBrokenBlocks = BuildCraftCore.mainConfigManager.get("builders.dropBrokenBlocks").getBoolean();

            if (BuildCraftCore.mainConfiguration.hasChanged()) {
                BuildCraftCore.mainConfiguration.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
        if ("BuildCraftCore".equals(event.getModID())) {
            reloadConfig(event.isWorldRunning() ? RestartRequirement.NONE : RestartRequirement.WORLD);
        }
    }

    private static String getDownloadsDir() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("nix") || os.contains("lin") || os.contains("mac")) {
            // Linux, Mac or other UNIX
            // According XDG specification every user-specified folder can be localized
            // or even moved to any destination, so we obtain real path with xdg-user-dir
            try {
                Process process = Runtime.getRuntime().exec(new String[] { "xdg-user-dir", "DOWNLOAD" });
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
                process.waitFor();
                String line = reader.readLine().trim();
                reader.close();

                if (line.length() > 0) {
                    return line;
                }
            } catch (Exception ignored) {
                // Very bad, we have a error while obtaining xdg dir :(
                // Just ignore, uses default dir
            }
        }
        // Windows or unknown system
        return "$HOME" + File.separator + "Downloads";
    }

    private String replacePathVariables(String path) {
        String result = path.replace("$DOWNLOADS", getDownloadsDir());
        result = result.replace("$HOME", System.getProperty("user.home"));

        if (Launch.minecraftHome == null) {
            result = result.replace("$MINECRAFT", new File(".").getAbsolutePath());
        } else {
            result = result.replace("$MINECRAFT", Launch.minecraftHome.getAbsolutePath());
        }

        if ("/".equals(File.separator)) {
            result = result.replaceAll("\\\\", "/");
        } else {
            result = result.replaceAll("/", "\\\\");
        }

        return result;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        templateItem = new ItemBlueprintTemplate();
        templateItem.setUnlocalizedName("templateItem");
        BCRegistry.INSTANCE.registerItem(templateItem, false);

        blueprintItem = new ItemBlueprintStandard();
        blueprintItem.setUnlocalizedName("blueprintItem");
        BCRegistry.INSTANCE.registerItem(blueprintItem, false);

        quarryBlock = (BlockQuarry) CompatHooks.INSTANCE.getBlock(BlockQuarry.class);
        BCRegistry.INSTANCE.registerBlock(quarryBlock.setUnlocalizedName("quarryBlock"), false);

        fillerBlock = (BlockFiller) CompatHooks.INSTANCE.getBlock(BlockFiller.class);
        BCRegistry.INSTANCE.registerBlock(fillerBlock.setUnlocalizedName("fillerBlock"), false);

        frameBlock = new BlockFrame();
        BCRegistry.INSTANCE.registerBlock(frameBlock.setUnlocalizedName("frameBlock"), true);

        builderBlock = (BlockBuilder) CompatHooks.INSTANCE.getBlock(BlockBuilder.class);
        BCRegistry.INSTANCE.registerBlock(builderBlock.setUnlocalizedName("builderBlock"), false);

        architectBlock = (BlockArchitect) CompatHooks.INSTANCE.getBlock(BlockArchitect.class);
        BCRegistry.INSTANCE.registerBlock(architectBlock.setUnlocalizedName("architectBlock"), false);

        libraryBlock = (BlockBlueprintLibrary) CompatHooks.INSTANCE.getBlock(BlockBlueprintLibrary.class);
        BCRegistry.INSTANCE.registerBlock(libraryBlock.setUnlocalizedName("libraryBlock"), false);

        BCRegistry.INSTANCE.registerTileEntity(TileQuarry.class, "buildcraft.builders.Quarry", "Machine");
        BCRegistry.INSTANCE.registerTileEntity(TileMarker.class, "buildcraft.builders.Marker", "Marker");
        BCRegistry.INSTANCE.registerTileEntity(TileFiller.class, "buildcraft.builders.Filler", "Filler");
        BCRegistry.INSTANCE.registerTileEntity(TileBuilder.class, "buildcraft.builders.Builder", "net.minecraft.src.builders.TileBuilder");
        BCRegistry.INSTANCE.registerTileEntity(TileArchitect.class, "buildcraft.builders.Architect", "net.minecraft.src.builders.TileTemplate");
        BCRegistry.INSTANCE.registerTileEntity(TilePathMarker.class, "buildcraft.builders.PathMarker", "net.minecraft.src.builders.TilePathMarker");
        BCRegistry.INSTANCE.registerTileEntity(TileBlueprintLibrary.class, "buildcraft.builders.BlueprintLibrary", "net.minecraft.src.builders.TileBlueprintLibrary");

        constructionMarkerBlock = (BlockConstructionMarker) CompatHooks.INSTANCE.getBlock(BlockConstructionMarker.class);
        BCRegistry.INSTANCE.registerBlock(constructionMarkerBlock.setUnlocalizedName("constructionMarkerBlock"), ItemConstructionMarker.class, false);

        BCRegistry.INSTANCE.registerTileEntity(TileConstructionMarker.class, "buildcraft.builders.ConstructionMarker", "net.minecraft.src.builders.TileConstructionMarker");

        SchematicRegistry.INSTANCE.readConfiguration(BuildCraftCore.mainConfiguration);

        if (BuildCraftCore.mainConfiguration.hasChanged()) {
            BuildCraftCore.mainConfiguration.save();
        }

        FMLInterModComms.sendMessage("BuildCraft|Transport", "blacklist-facade", new ItemStack(frameBlock, 1, -1));

        MinecraftForge.EVENT_BUS.register(this);

        StatementManager.registerActionProvider(new BuildersActionProvider());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new BuildersGuiHandler());

        MinecraftForge.EVENT_BUS.register(new BuilderTooltipHandler());
        EntityRegistry.registerModEntity(EntityMechanicalArm.class, "bcMechArm", EntityIds.MECHANICAL_ARM, instance, 50, 1, true);

        // Standard blocks
        ISchematicRegistry schemes = BuilderAPI.schematicRegistry;
        schemes.registerSchematicBlock(Blocks.air, SchematicAir.class);

        schemes.registerSchematicBlock(Blocks.snow, SchematicIgnore.class);
        schemes.registerSchematicBlock(Blocks.tallgrass, SchematicIgnore.class);
        schemes.registerSchematicBlock(Blocks.double_plant, SchematicIgnore.class);
        schemes.registerSchematicBlock(Blocks.ice, SchematicIgnore.class);
        schemes.registerSchematicBlock(Blocks.piston_head, SchematicIgnore.class);

        schemes.registerSchematicBlock(Blocks.dirt, SchematicDirt.class);
        schemes.registerSchematicBlock(Blocks.grass, SchematicDirt.class);

        schemes.registerSchematicBlock(Blocks.cactus, SchematicCactus.class);

        schemes.registerSchematicBlock(Blocks.farmland, SchematicFarmland.class);
        schemes.registerSchematicBlock(Blocks.wheat, SchematicSeeds.class, Items.wheat_seeds);
        schemes.registerSchematicBlock(Blocks.pumpkin_stem, SchematicSeeds.class, Items.pumpkin_seeds);
        schemes.registerSchematicBlock(Blocks.melon_stem, SchematicSeeds.class, Items.melon_seeds);
        schemes.registerSchematicBlock(Blocks.nether_wart, SchematicSeeds.class, Items.nether_wart);

        schemes.registerSchematicBlock(Blocks.torch, SchematicBlock.class);
        schemes.registerSchematicBlock(Blocks.REDSTONE_TORCH, SchematicBlock.class);
        schemes.registerSchematicBlock(Blocks.unlit_redstone_torch, SchematicBlock.class);

        schemes.registerSchematicBlock(Blocks.tripwire_hook, SchematicBlock.class);
        schemes.registerSchematicBlock(Blocks.tripwire, SchematicTripwire.class);

        schemes.registerSchematicBlock(Blocks.skull, SchematicSkull.class);

        schemes.registerSchematicBlock(Blocks.log, SchematicLog.class);
        schemes.registerSchematicBlock(Blocks.log2, SchematicLog.class);
        schemes.registerSchematicBlock(Blocks.hay_block, SchematicRotatedPillar.class);
        schemes.registerSchematicBlock(Blocks.quartz_block, SchematicQuartz.class);
        schemes.registerSchematicBlock(Blocks.hopper, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.anvil, SchematicCustomStack.class, new ItemStack(Blocks.anvil));

        schemes.registerSchematicBlock(Blocks.vine, SchematicVine.class);

        schemes.registerSchematicBlock(Blocks.furnace, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.lit_furnace, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.CHEST, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.dispenser, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.dropper, SchematicTile.class);

        schemes.registerSchematicBlock(Blocks.ender_chest, SchematicEnderChest.class);

        schemes.registerSchematicBlock(Blocks.lever, SchematicLever.class);

        schemes.registerSchematicBlock(Blocks.gold_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
        schemes.registerSchematicBlock(Blocks.iron_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
        schemes.registerSchematicBlock(Blocks.coal_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
        schemes.registerSchematicBlock(Blocks.lapis_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
        schemes.registerSchematicBlock(Blocks.diamond_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
        schemes.registerSchematicBlock(Blocks.redstone_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
        schemes.registerSchematicBlock(Blocks.lit_redstone_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
        schemes.registerSchematicBlock(Blocks.emerald_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());

        schemes.registerSchematicBlock(Blocks.gravel, SchematicGravel.class);

        schemes.registerSchematicBlock(Blocks.redstone_wire, SchematicRedstoneWire.class, new ItemStack(Items.REDSTONE));
        schemes.registerSchematicBlock(Blocks.cake, SchematicCustomStack.class, new ItemStack(Items.cake));
        schemes.registerSchematicBlock(Blocks.glowstone, SchematicCustomStack.class, new ItemStack(Blocks.glowstone));

        schemes.registerSchematicBlock(Blocks.powered_repeater, SchematicCustomStackFloored.class, new ItemStack(Items.repeater));
        schemes.registerSchematicBlock(Blocks.unpowered_repeater, SchematicCustomStackFloored.class, new ItemStack(Items.repeater));
        schemes.registerSchematicBlock(Blocks.powered_comparator, SchematicCustomStackFloored.class, new ItemStack(Items.comparator));
        schemes.registerSchematicBlock(Blocks.unpowered_comparator, SchematicCustomStackFloored.class, new ItemStack(Items.comparator));

        schemes.registerSchematicBlock(Blocks.daylight_detector, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.daylight_detector_inverted, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.jukebox, SchematicJukebox.class);
        schemes.registerSchematicBlock(Blocks.noteblock, SchematicTile.class);

        schemes.registerSchematicBlock(Blocks.redstone_lamp, SchematicRedstoneLamp.class);
        schemes.registerSchematicBlock(Blocks.lit_redstone_lamp, SchematicRedstoneLamp.class);

        schemes.registerSchematicBlock(Blocks.glass_pane, SchematicGlassPane.class);
        schemes.registerSchematicBlock(Blocks.stained_glass_pane, SchematicGlassPane.class);

        schemes.registerSchematicBlock(Blocks.piston, SchematicPiston.class);
        schemes.registerSchematicBlock(Blocks.piston_extension, SchematicIgnore.class);
        schemes.registerSchematicBlock(Blocks.sticky_piston, SchematicPiston.class);

        schemes.registerSchematicBlock(Blocks.oak_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.stone_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.brick_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.stone_brick_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.nether_brick_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.sandstone_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.spruce_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.birch_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.jungle_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.quartz_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.acacia_stairs, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.dark_oak_stairs, SchematicStandalone.class);

        schemes.registerSchematicBlock(Blocks.ACACIA_DOOR, SchematicDoor.class, new ItemStack(Items.ACACIA_DOOR));
        schemes.registerSchematicBlock(Blocks.BIRCH_DOOR, SchematicDoor.class, new ItemStack(Items.BIRCH_DOOR));
        schemes.registerSchematicBlock(Blocks.DARK_OAK_DOOR, SchematicDoor.class, new ItemStack(Items.DARK_OAK_DOOR));
        schemes.registerSchematicBlock(Blocks.JUNGLE_DOOR, SchematicDoor.class, new ItemStack(Items.JUNGLE_DOOR));
        schemes.registerSchematicBlock(Blocks.OAK_DOOR, SchematicDoor.class, new ItemStack(Items.OAK_DOOR));
        schemes.registerSchematicBlock(Blocks.SPRUCE_DOOR, SchematicDoor.class, new ItemStack(Items.SPRUCE_DOOR));
        schemes.registerSchematicBlock(Blocks.IRON_DOOR, SchematicDoor.class, new ItemStack(Items.IRON_DOOR));

        schemes.registerSchematicBlock(Blocks.bed, SchematicBed.class);

        schemes.registerSchematicBlock(Blocks.wall_sign, SchematicSignLike.class, true);
        schemes.registerSchematicBlock(Blocks.standing_sign, SchematicSignLike.class, false);

        schemes.registerSchematicBlock(Blocks.wall_banner, SchematicBanner.class, true);
        schemes.registerSchematicBlock(Blocks.standing_banner, SchematicBanner.class, false);

        schemes.registerSchematicBlock(Blocks.portal, SchematicPortal.class);

        schemes.registerSchematicBlock(Blocks.rail, SchematicRail.class);
        schemes.registerSchematicBlock(Blocks.activator_rail, SchematicRail.class);
        schemes.registerSchematicBlock(Blocks.detector_rail, SchematicRail.class);
        schemes.registerSchematicBlock(Blocks.golden_rail, SchematicRail.class);

        schemes.registerSchematicBlock(Blocks.beacon, SchematicTile.class);
        schemes.registerSchematicBlock(Blocks.brewing_stand, SchematicBrewingStand.class);
        schemes.registerSchematicBlock(Blocks.enchanting_table, SchematicTile.class);

        schemes.registerSchematicBlock(Blocks.fire, SchematicFire.class);

        schemes.registerSchematicBlock(Blocks.bedrock, SchematicBlockCreative.class);

        schemes.registerSchematicBlock(Blocks.command_block, SchematicTileCreative.class);
        schemes.registerSchematicBlock(Blocks.mob_spawner, SchematicTileCreative.class);

        schemes.registerSchematicBlock(Blocks.glass, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.stone_slab, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.double_stone_slab, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.wooden_slab, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.double_wooden_slab, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.stained_glass, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.acacia_fence, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.birch_fence, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.dark_oak_fence, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.jungle_fence, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.oak_fence, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.spruce_fence, SchematicStandalone.class);
        schemes.registerSchematicBlock(Blocks.iron_bars, SchematicStandalone.class);

        // Standard entities

        schemes.registerSchematicEntity(EntityArmorStand.class, SchematicArmorStand.class);

        schemes.registerSchematicEntity(EntityMinecartEmpty.class, SchematicMinecart.class, Items.minecart);
        schemes.registerSchematicEntity(EntityMinecartFurnace.class, SchematicMinecart.class, Items.furnace_minecart);
        schemes.registerSchematicEntity(EntityMinecartTNT.class, SchematicMinecart.class, Items.tnt_minecart);
        schemes.registerSchematicEntity(EntityMinecartChest.class, SchematicMinecart.class, Items.chest_minecart);
        schemes.registerSchematicEntity(EntityMinecartHopper.class, SchematicMinecart.class, Items.hopper_minecart);

        schemes.registerSchematicEntity(EntityPainting.class, SchematicHanging.class, Items.painting);
        schemes.registerSchematicEntity(EntityItemFrame.class, SchematicHanging.class, Items.item_frame);

        // BuildCraft blocks

        schemes.registerSchematicBlock(architectBlock, SchematicTile.class);
        schemes.registerSchematicBlock(builderBlock, SchematicBuilderLike.class);
        schemes.registerSchematicBlock(fillerBlock, SchematicBuilderLike.class);
        schemes.registerSchematicBlock(libraryBlock, SchematicTile.class);
        schemes.registerSchematicBlock(quarryBlock, SchematicBuilderLike.class);

        // schemes.registerSchematicBlock(markerBlock, SchematicWallSide.class);
        // schemes.registerSchematicBlock(pathMarkerBlock, SchematicWallSide.class);
        // schemes.registerSchematicBlock(constructionMarkerBlock, SchematicWallSide.class);

        // Factories required to save entities in world

        SchematicFactory.registerSchematicFactory(SchematicBlock.class, new SchematicFactoryBlock());
        SchematicFactory.registerSchematicFactory(SchematicMask.class, new SchematicFactoryMask());
        SchematicFactory.registerSchematicFactory(SchematicEntity.class, new SchematicFactoryEntity());

        LibraryAPI.registerHandler(new LibraryBlueprintTypeHandler(false)); // Template
        LibraryAPI.registerHandler(new LibraryBlueprintTypeHandler(true)); // Blueprint
        LibraryAPI.registerHandler(new LibraryBookTypeHandler());

        BlueprintDeployer.instance = new RealBlueprintDeployer();

        architectAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|builders:achievement.architect", "architectAchievement", 11, 2, BuildCraftBuilders.architectBlock, BuildCraftCore.goldGearAchievement));
        builderAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|builders:achievement.builder", "builderAchievement", 13, 2, BuildCraftBuilders.builderBlock, architectAchievement));
        blueprintAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|builders:achievement.blueprint", "blueprintAchievement", 11, 4, BuildCraftBuilders.blueprintItem, architectAchievement));
        templateAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|builders:achievement.template", "templateAchievement", 13, 4, BuildCraftBuilders.templateItem, blueprintAchievement));
        libraryAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|builders:achievement.blueprintLibrary", "blueprintLibraryAchievement", 15, 2, BuildCraftBuilders.libraryBlock, builderAchievement));
        chunkDestroyerAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("buildcraft|builders:achievement.chunkDestroyer", "chunkDestroyerAchievement", 9, 2, quarryBlock, BuildCraftCore.diamondGearAchievement));

        if (BuildCraftCore.loadDefaultRecipes) {
            loadRecipes();
        }

        BuilderProxy.proxy.registerBlockRenderers();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        HeuristicBlockDetection.start();
        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new QuarryChunkloadCallback());

        if (debugPrintSchematicList) {
            try {
                PrintWriter writer = new PrintWriter("SchematicDebug.txt", "UTF-8");
                writer.println("*** REGISTERED SCHEMATICS ***");
                SchematicRegistry reg = (SchematicRegistry) BuilderAPI.schematicRegistry;
                for (String s : reg.schematicBlocks.keySet()) {
                    writer.println(s + " -> " + reg.schematicBlocks.get(s).clazz.getCanonicalName());
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Refresh the client database once all the library type handlers are registered
        // The server database is refreshed later
        clientDB.refresh();
    }

    public static void loadRecipes() {
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(quarryBlock), "ipi", "gig", "dDd", 'i', "gearIron", 'p', "dustRedstone", 'g', "gearGold", 'd', "gearDiamond", 'D', Items.DIAMOND_PICKAXE);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(templateItem, 1), "ppp", "pip", "ppp", 'i', "dyeBlack", 'p', Items.PAPER);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(blueprintItem, 1), "ppp", "pip", "ppp", 'i', "gemLapis", 'p', Items.PAPER);

        if (constructionMarkerBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(constructionMarkerBlock, 1), "l ", "r ", 'l', "gearGold", 'r', Blocks.REDSTONE_TORCH);
        }

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(fillerBlock, 1), "btb", "ycy", "gCg", 'b', "dyeBlack", 't', BuildCraftCore.markerBlock, 'y', "dyeYellow", 'c', Blocks.CRAFTING_TABLE, 'g', "gearGold", 'C', Blocks.CHEST);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(builderBlock, 1), "btb", "ycy", "gCg", 'b', "dyeBlack", 't', BuildCraftCore.markerBlock, 'y', "dyeYellow", 'c', Blocks.CRAFTING_TABLE, 'g', "gearDiamond", 'C', Blocks.CHEST);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(architectBlock, 1), "btb", "ycy", "gCg", 'b', "dyeBlack", 't', BuildCraftCore.markerBlock, 'y', "dyeYellow", 'c', Blocks.CRAFTING_TABLE, 'g', "gearDiamond", 'C', new ItemStack(blueprintItem,
                1));

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(libraryBlock, 1), "igi", "bBb", "iri", 'B', new ItemStack(blueprintItem), 'b', Blocks.BOOKSHELF, 'i', "ingotIron", 'g', "gearIron", 'r', Items.REDSTONE);
    }

    @Mod.EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        TilePathMarker.clearAvailableMarkersList();
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        String blueprintPath = new File(DimensionManager.getCurrentSaveRootDirectory(), "buildcraft" + File.separator + "blueprints").getPath();
        serverDB.init(new String[] { oldBlueprintServerDir, blueprintPath }, blueprintPath);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void loadTextures(TextureStitchEvent.Pre evt) {
        TextureMap terrainTextures = evt.getMap();
        BuilderProxyClient.drillTexture = terrainTextures.registerSprite(new ResourceLocation("buildcraftbuilders:blocks/quarry/drill"));
        BuilderProxyClient.drillHeadTexture = terrainTextures.registerSprite(new ResourceLocation("buildcraftbuilders:blocks/quarry/drill_head"));
    }

    @Mod.EventHandler
    public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileBlueprintLibrary.class.getCanonicalName());
    }

    @Mod.EventHandler
    public void remap(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            if (mapping.name.equals("BuildCraft|Builders:buildToolBlock") || mapping.name.equals("BuildCraft|Builders:null")) {
                if (mapping.type == GameRegistry.Type.ITEM) {
                    mapping.remap(Item.getItemFromBlock(BuildCraftCore.decoratedBlock));
                } else {
                    mapping.remap(BuildCraftCore.decoratedBlock);
                }
            } else if (mapping.name.equals("BuildCraft|Builders:markerBlock")) {
                if (mapping.type == GameRegistry.Type.ITEM) {
                    mapping.remap(Item.getItemFromBlock(BuildCraftCore.markerBlock));
                } else {
                    mapping.remap(BuildCraftCore.markerBlock);
                }
            } else if (mapping.name.equals("BuildCraft|Builders:pathMarkerBlock")) {
                if (mapping.type == GameRegistry.Type.ITEM) {
                    mapping.remap(Item.getItemFromBlock(BuildCraftCore.pathMarkerBlock));
                } else {
                    mapping.remap(BuildCraftCore.pathMarkerBlock);
                }
            } else if (mapping.name.toLowerCase(Locale.ROOT).equals("buildcraft|builders:machineblock")) {
                if (mapping.type == GameRegistry.Type.ITEM) {
                    mapping.remap(Item.getItemFromBlock(quarryBlock));
                } else {
                    mapping.remap(quarryBlock);
                }
            }
        }
    }
}
