/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.BuildCraftFactory;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.lib.client.render.FluidRenderer;
import buildcraft.factory.TileRefinery;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.misc.RenderUtil;

public class RenderRefinery extends TileEntitySpecialRenderer<TileRefinery> {

    private static final Vec3d TANK_SIZE = new Vec3d(0.5, 1, 0.5);
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftfactory:textures/blocks/refinery/refinery.png");
    private static final float pixel = (float) (1.0 / 16.0);
    private final ModelRenderer tank;
    private final ModelRenderer[] magnet = new ModelRenderer[4];
    private final ModelBase model = new ModelBase() {};

    public RenderRefinery() {

        // constructor:
        tank = new ModelRenderer(model, 0, 0);
        tank.addBox(-4F, -8F, -4F, 8, 16, 8);
        tank.rotationPointX = 8;
        tank.rotationPointY = 8;
        tank.rotationPointZ = 8;

        // constructor:

        for (int i = 0; i < 4; ++i) {
            magnet[i] = new ModelRenderer(model, 32, i * 8);
            magnet[i].addBox(0, -8F, -8F, 8, 4, 4);
            magnet[i].rotationPointX = 8;
            magnet[i].rotationPointY = 8;
            magnet[i].rotationPointZ = 8;

        }
    }

    @Override
    public void renderTileEntityAt(TileRefinery tile, double x, double y, double z, float partialTicks, int arg) {
        FluidStack liquid1 = null, liquid2 = null, liquidResult = null;
        int color1 = 0xFFFFFF, color2 = 0xFFFFFF, colorResult = 0xFFFFFF;

        float anim = 0;
        int angle = 0;
        ModelRenderer theMagnet = magnet[0];
        if (tile != null) {
            if (tile.tanks[0].getFluid() != null) {
                liquid1 = tile.tanks[0].getFluid();
                color1 = tile.tanks[0].colorRenderCache;
            }

            if (tile.tanks[1].getFluid() != null) {
                liquid2 = tile.tanks[1].getFluid();
                color2 = tile.tanks[1].colorRenderCache;
            }

            if (tile.result.getFluid() != null) {
                liquidResult = tile.result.getFluid();
                colorResult = tile.result.colorRenderCache;
            }

            anim = tile.getAnimationStage();

            IBlockState state = tile.getWorld().getBlockState(tile.getPos());
            EnumFacing face = state.getBlock() == BuildCraftFactory.refineryBlock ? state.getValue(BuildCraftProperties.BLOCK_FACING) : EnumFacing.EAST;

            while (face != EnumFacing.EAST) {
                face = face.rotateY();
                angle += 90;
            }

            if (tile.animationSpeed <= 1) {
                theMagnet = magnet[0];
            } else if (tile.animationSpeed <= 2.5) {
                theMagnet = magnet[1];
            } else if (tile.animationSpeed <= 4.5) {
                theMagnet = magnet[2];
            } else {
                theMagnet = magnet[3];
            }
        }

        GL11.glPushMatrix();

        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glScalef(0.99F, 0.99F, 0.99F);

        GL11.glRotatef(angle, 0, 1, 0);

        bindTexture(TEXTURE);

        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        GL11.glTranslatef(-4F * pixel, 0, -4F * pixel);
        tank.render(pixel);
        GL11.glTranslatef(4F * pixel, 0, 4F * pixel);

        GL11.glTranslatef(-4F * pixel, 0, 4F * pixel);
        tank.render(pixel);
        GL11.glTranslatef(4F * pixel, 0, -4F * pixel);

        GL11.glTranslatef(4F * pixel, 0, 0);
        tank.render(pixel);
        GL11.glTranslatef(-4F * pixel, 0, 0);
        GL11.glPopMatrix();

        float trans1, trans2;

        if (anim <= 100) {
            trans1 = 12F * pixel * anim / 100F;
            trans2 = 0;
        } else if (anim <= 200) {
            trans1 = 12F * pixel - (12F * pixel * (anim - 100F) / 100F);
            trans2 = 12F * pixel * (anim - 100F) / 100F;
        } else {
            trans1 = 12F * pixel * (anim - 200F) / 100F;
            trans2 = 12F * pixel - (12F * pixel * (anim - 200F) / 100F);
        }

        GL11.glPushMatrix();
        GL11.glScalef(0.99F, 0.99F, 0.99F);
        GL11.glTranslatef(-0.51F, trans1 - 0.5F, -0.5F);
        theMagnet.render(pixel);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glScalef(0.99F, 0.99F, 0.99F);
        GL11.glTranslatef(-0.51F, trans2 - 0.5F, 12F * pixel - 0.5F);
        theMagnet.render(pixel);
        GL11.glPopMatrix();

        if (tile != null) {
            // GlStateManager.enableCull();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            if (liquid1 != null && liquid1.amount > 0) {
                int[] list1 = FluidRenderer.getFluidDisplayLists(liquid1, FluidRenderer.FluidType.STILL, TANK_SIZE);

                if (list1 != null) {
                    bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    RenderUtil.setGLColorFromInt(color1);
                    GL11.glPushMatrix();
                    GL11.glTranslatef(-0.25f, 0, -0.25f);
                    GL11.glScalef(0.99f, 0.99f, 0.99f);
                    GL11.glTranslatef(-0.25f, -0.5f, -0.25f);
                    GL11.glCallList(list1[getDisplayListIndex(tile.tanks[0])]);
                    GL11.glPopMatrix();
                }
            }

            if (liquid2 != null && liquid2.amount > 0) {
                int[] list2 = FluidRenderer.getFluidDisplayLists(liquid2, FluidRenderer.FluidType.STILL, TANK_SIZE);

                if (list2 != null) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(-0.25f, 0, 0.25f);
                    GL11.glScalef(0.99f, 0.99f, 0.99f);
                    GL11.glTranslatef(-0.25f, -0.5f, -0.25f);
                    bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    RenderUtil.setGLColorFromInt(color2);
                    GL11.glCallList(list2[getDisplayListIndex(tile.tanks[1])]);
                    GL11.glPopMatrix();
                }
            }

            if (liquidResult != null && liquidResult.amount > 0) {
                int[] list3 = FluidRenderer.getFluidDisplayLists(liquidResult, FluidRenderer.FluidType.STILL, TANK_SIZE);

                if (list3 != null) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(0.25f, 0, 0);
                    GL11.glScalef(0.99f, 0.99f, 0.99f);
                    GL11.glTranslatef(-0.25f, -0.5f, -0.25f);
                    bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    RenderUtil.setGLColorFromInt(colorResult);
                    GL11.glCallList(list3[getDisplayListIndex(tile.result)]);
                    GL11.glPopMatrix();
                }
            }

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            // GlStateManager.disableCull();
        }

        GL11.glPopMatrix();
    }

    private int getDisplayListIndex(Tank tank) {
        return Math.min((int) ((float) tank.getFluidAmount() / (float) tank.getCapacity() * (FluidRenderer.DISPLAY_STAGES - 1)),
                FluidRenderer.DISPLAY_STAGES - 1);
    }
}
