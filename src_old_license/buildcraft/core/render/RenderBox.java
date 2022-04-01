/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import buildcraft.core.LaserData;
import buildcraft.lib.misc.data.Box;

public final class RenderBox {

    /** Deactivate constructor */
    private RenderBox() {}

    public static void doRender(World world, TextureManager t, ResourceLocation texture, Box box) {
        Minecraft.getMinecraft().mcProfiler.startSection("box");
        Minecraft.getMinecraft().mcProfiler.startSection("prepare");
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);

        box.createLaserData();
        Minecraft.getMinecraft().mcProfiler.endStartSection("draw");

        for (LaserData l : box.lasersData) {
            // l.update();
            GL11.glPushMatrix();
            GL11.glTranslated(0.5F, 0.5F, 0.5F);
            RenderLaser.doRenderLaser(world, t, l, texture);
            GL11.glPopMatrix();
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
