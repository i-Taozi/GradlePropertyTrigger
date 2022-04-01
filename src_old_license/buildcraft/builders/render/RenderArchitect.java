/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import buildcraft.builders.TileArchitect;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.render.RenderBoxProvider;
import buildcraft.core.render.RenderLaser;

public class RenderArchitect extends RenderBoxProvider<TileArchitect> {

    @Override
    public void renderTileEntityAt(TileArchitect architect, double x, double y, double z, float f, int aThing) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("architect");
        super.renderTileEntityAt(architect, x, y, z, f, aThing);
        Minecraft.getMinecraft().mcProfiler.startSection("lasers");
        if (architect != null) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glTranslated(x, y, z);
            GL11.glTranslated(-architect.getPos().getX(), -architect.getPos().getY(), -architect.getPos().getZ());

            for (LaserData laser : architect.subLasers) {
                if (laser != null) {
                    GL11.glPushMatrix();
                    RenderLaser.doRenderLaserWave(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, laser,
                            EntityLaser.LASER_BLUE);

                    GL11.glPopMatrix();
                }
            }

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

}
