package buildcraft.core.client;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

@Deprecated
public class BuildCraftStateMapper extends StateMapperBase {
    public static final BuildCraftStateMapper INSTANCE = new BuildCraftStateMapper();

    public static String getPropertyString(IBlockState state) {
        return INSTANCE.getPropertyString(state.getProperties());
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        ResourceLocation location = Block.REGISTRY.getNameForObject(state.getBlock());
        location = new ResourceLocation(location.getResourceDomain().replace("|", ""), location.getResourcePath());
        return new ModelResourceLocation(location, getPropertyString(state));
    }
}
