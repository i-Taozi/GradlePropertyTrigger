/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.builders.TileAbstractBuilder;

public abstract class RenderBuilder<B extends TileAbstractBuilder> extends RenderBoxProvider<B> {
    private static final RenderBuildingItems renderItems = new RenderBuildingItems();

    @Override
    public void renderTileEntityAt(B builder, double x, double y, double z, float f, int arg) {
        Minecraft.getMinecraft().mcProfiler.startSection("builder");
        super.renderTileEntityAt(builder, x, y, z, f, arg);

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTranslated(x, y, z);
        GL11.glTranslated(-builder.getPos().getX(), -builder.getPos().getY(), -builder.getPos().getZ());

        if (builder.getPathLaser() != null) {
            for (LaserData laser : builder.getPathLaser()) {
                if (laser != null) {
                    GL11.glPushMatrix();
                    RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().getTextureManager(), laser,
                            EntityLaser.LASER_STRIPES_YELLOW);
                    GL11.glPopMatrix();
                }
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        }

        // GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopAttrib();
        GL11.glPopMatrix();

        renderItems.render(builder, x, y, z);
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
