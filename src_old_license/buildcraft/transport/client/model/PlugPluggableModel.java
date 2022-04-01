package buildcraft.transport.client.model;

import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;

import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.client.model.PerspAwareModelBase;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;

import javax.vecmath.Matrix4f;

public class PlugPluggableModel extends BakedModelHolder implements IPluggableModelBaker<ModelKeyPlug> {
    public static final PlugPluggableModel INSTANCE = new PlugPluggableModel();

    private static final ResourceLocation plugLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/plug.obj");

    private static final ResourceLocation plugSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/plug");
    private static TextureAtlasSprite spritePlug;

    private PlugPluggableModel() {}

    public static PerspAwareModelBase create() {
        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
        VertexFormat format = DefaultVertexFormats.ITEM;
        quads.addAll(INSTANCE.bakeCutout(EnumFacing.SOUTH, format));
        return new PerspAwareModelBase(format, quads.build(), spritePlug, getPluggableTransforms());
    }

    public IModel modelPlug() {
        return getModelOBJ(plugLoc);
    }

    @SubscribeEvent
    public void textureStitch(TextureStitchEvent.Pre event) {
        spritePlug = null;
        spritePlug = event.map.getTextureExtry(plugSpriteLoc.toString());
        if (spritePlug == null) spritePlug = event.map.registerSprite(plugSpriteLoc);
    }

    @Override
    public ImmutableList<BakedQuad> bake(ModelKeyPlug key) {
        // Assume its cutout
        return ImmutableList.copyOf(bakeCutout(key.side, getVertexFormat()));
    }

    @Override
    public VertexFormat getVertexFormat() {
        return DefaultVertexFormats.BLOCK;
    }

    private List<BakedQuad> bakeCutout(EnumFacing face, VertexFormat format) {
        IModel model = modelPlug();
        TextureAtlasSprite sprite = spritePlug;

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderPlug(model, sprite, format);
        Matrix4f matrix = MatrixUtil.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.transform(matrix);
            ModelUtil.appendBakeQuads(quads, format, mutable);
        }

        return quads;
    }

    public static List<BakedQuad> renderPlug(IModel model, TextureAtlasSprite sprite, VertexFormat format) {
        List<BakedQuad> quads = Lists.newArrayList();
        IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, format, singleTextureFunction(sprite));
        for (BakedQuad quad : baked.getGeneralQuads()) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.colouri(0xFF_FF_FF_FF);
            ModelUtil.appendBakeQuads(quads, format, mutable);
        }
        return quads;
    }
}
