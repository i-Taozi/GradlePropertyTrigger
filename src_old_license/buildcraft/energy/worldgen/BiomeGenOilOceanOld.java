/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.worldgen;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraftforge.common.BiomeDictionary;

public final class BiomeGenOilOceanOld extends BiomeGenOcean {

    protected static final BiomeGenBase.Height height_OilOcean = new BiomeGenBase.Height(0.1F, 0.2F);

    private BiomeGenOilOceanOld(int id) {
        super(id);
        setBiomeName("Ocean Oil Field");
        setColor(112);
        setHeight(height_Oceans);
    }

    public static BiomeGenOilOceanOld makeBiome(int id) {
        BiomeGenOilOceanOld biome = new BiomeGenOilOceanOld(id);
        BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.WATER);
        OilPopulateOld.INSTANCE.excessiveBiomes.add(biome.biomeID);
        OilPopulateOld.INSTANCE.surfaceDepositBiomes.add(biome.biomeID);
        return biome;
    }
}
