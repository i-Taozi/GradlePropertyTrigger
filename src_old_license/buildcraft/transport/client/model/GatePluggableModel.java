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
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;

import buildcraft.api.gates.GateExpansionModelKey;
import buildcraft.api.gates.IExpansionBaker;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.IPipePluggableDynamicRenderer;
import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.transport.client.model.key.KeyPlugGate;

import javax.vecmath.Matrix4f;

public final class GatePluggableModel extends BakedModelHolder implements IPluggableModelBaker<KeyPlugGate>, IPipePluggableDynamicRenderer {
    private static final ResourceLocation mainLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/gate_main.obj");
    private static final ResourceLocation materialLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/gate_material.obj");

    public static final GatePluggableModel INSTANCE = new GatePluggableModel();

    private GatePluggableModel() {}

    public IModel modelMain() {
        return getModelOBJ(mainLoc);
    }

    public IModel modelMaterial() {
        return getModelOBJ(materialLoc);
    }

    @Override
    public void renderDynamicPluggable(IPipe pipe, EnumFacing side, PipePluggable pipePluggable, double x, double y, double z) {
        // TODO!
    }

    @Override
    public VertexFormat getVertexFormat() {
        return DefaultVertexFormats.BLOCK;
    }

    @Override
    public ImmutableList<BakedQuad> bake(KeyPlugGate key) {
        return ImmutableList.copyOf(bakeCutout(key, DefaultVertexFormats.BLOCK));
    }

    public List<BakedQuad> bakeCutout(KeyPlugGate key, VertexFormat format) {
        List<BakedQuad> quads = Lists.newArrayList();
        List<MutableQuad> bakedQuads = renderGate(key, format);
        Matrix4f matrix = MatrixUtil.rotateTowardsFace(key.side);
        for (MutableQuad quad : bakedQuads) {
            quad.transform(matrix);
            quad.setCalculatedDiffuse();
            quads.add(quad.toUnpacked());
        }

        return quads;
    }

    public List<MutableQuad> renderGate(KeyPlugGate gate, VertexFormat format) {
        TextureAtlasSprite logicSprite = gate.lit ? gate.logic.getIconLit() : gate.logic.getIconDark();
        TextureAtlasSprite materialSprite = gate.material.getIconBlock();

        IModel main = modelMain();
        IModel material = modelMaterial();

        List<MutableQuad> quads = Lists.newArrayList();
        IFlexibleBakedModel baked = main.bake(ModelRotation.X0_Y0, format, singleTextureFunction(logicSprite));
        for (BakedQuad quad : baked.getGeneralQuads()) {
            MutableQuad mutable = MutableQuad.create(quad, format);
            quads.add(mutable);
        }

        if (materialSprite != null) {// Its null for redstone (As we don't render any material for redstone gates)
            baked = material.bake(ModelRotation.X0_Y0, format, singleTextureFunction(materialSprite));
            for (BakedQuad quad : baked.getGeneralQuads()) {
                quads.add(MutableQuad.create(quad, format));
            }
        }

        for (GateExpansionModelKey<?> expansion : gate.expansions) {
            generate(quads, expansion);
        }
        return quads;
    }

    private static <K extends GateExpansionModelKey<K>> void generate(List<MutableQuad> quads, GateExpansionModelKey<K> expansion) {
        IExpansionBaker<K> baker = expansion.baker;
        VertexFormat format = baker.getVertexFormat();
        for (BakedQuad q : baker.bake((K) expansion)) {
            quads.add(MutableQuad.create(q, format));
        }
    }
}
