/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

import buildcraft.builders.TileConstructionMarker;
import buildcraft.core.EntityLaser;
import buildcraft.core.render.RenderBoxProvider;
import buildcraft.core.render.RenderBuildingItems;
import buildcraft.core.render.RenderLaser;

public class RenderConstructionMarker extends RenderBoxProvider<TileConstructionMarker> {
    private final RenderBuildingItems renderItems = new RenderBuildingItems();

    private ModelBase model = new ModelBase() {};
    private ModelRenderer box;

    public RenderConstructionMarker() {
        box = new ModelRenderer(model, 0, 1);
        box.addBox(-8F, -8F, -8F, 16, 4, 16);
        box.rotationPointX = 8;
        box.rotationPointY = 8;
        box.rotationPointZ = 8;
    }

    @Override
    public void renderTileEntityAt(TileConstructionMarker marker, double x, double y, double z, float f, int aThing) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("bpt_marker");
        super.renderTileEntityAt(marker, x, y, z, f, aThing);

        if (marker != null) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glTranslated(x, y, z);
            GL11.glTranslated(-marker.getPos().getX(), -marker.getPos().getY(), -marker.getPos().getZ());

            if (marker.laser != null) {
                GL11.glPushMatrix();
                RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, marker.laser, EntityLaser.LASER_STRIPES_YELLOW);
                GL11.glPopMatrix();
            }

            if (marker.itemBlueprint != null) {
                doRenderItem(marker.itemBlueprint, marker.getPos().getX() + 0.5F, marker.getPos().getY() + 0.2F, marker.getPos().getZ() + 0.5F);
            }

            GlStateManager.disableBlend();
            GL11.glPopAttrib();
            GL11.glPopMatrix();

            renderItems.render(marker, x, y, z);
        }
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    public void doRenderItem(ItemStack stack, double x, double y, double z) {
        if (stack == null) {
            return;
        }

        float renderScale = 1.5f;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glTranslatef(0, 0.25F, 0);
        GL11.glScalef(renderScale, renderScale, renderScale);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);

        GL11.glPopMatrix();
    }
}
