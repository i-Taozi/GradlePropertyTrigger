package buildcraft.transport.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import buildcraft.BuildCraftTransport;
import buildcraft.api.items.IItemCustomPipeRender;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.client.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

public class PipeTransportRendererItems extends PipeTransportRenderer<PipeTransportItems> {
    private static final int MAX_ITEMS_TO_RENDER = 10;

    private static final EntityItem dummyEntityItem = new EntityItem(null);
    private static final RenderEntityItem customRenderItem;

    static {
        customRenderItem = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()) {
            @Override
            public boolean shouldBob() {
                return false;
            }

            @Override
            public boolean shouldSpreadItems() {
                return false;
            }
        };
    }

    @Override
    public void render(Pipe<PipeTransportItems> pipe, double x, double y, double z, float f) {
        GL11.glPushMatrix();

        float light = pipe.container.getWorld().getLightBrightness(pipe.container.getPos());

        int count = 0;
        for (TravelingItem item : pipe.transport.items) {
            if (count >= MAX_ITEMS_TO_RENDER) {
                break;
            }

            if (item == null || item.getContainer() != pipe.container) {
                continue;
            }

            Vec3d vpos = item.getRelativePos(f * item.getSpeed());

            doRenderItem(item, x + vpos.xCoord, y + vpos.yCoord, z + vpos.zCoord, light, item.color);
            count++;
        }

        GL11.glPopMatrix();
    }

    public static void doRenderItem(TravelingItem travellingItem, double x, double y, double z, float light, EnumDyeColor color) {

        if (travellingItem == null || travellingItem.getItemStack() == null) {
            return;
        }

        float renderScale = 0.9f;
        ItemStack itemstack = travellingItem.getItemStack();

        int val = (travellingItem.id * 191) ^ 0x8d35;

        x += 0.004f * (4 - ((val % 9)));
        y += 0.004f * (4 - ((val / 9) % 9));
        z += 0.004f * (4 - ((val / 81) % 9));

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y + 0.05f, (float) z);
        GL11.glPushMatrix();
        GlStateManager.color(1, 1, 1, 1);

        if (!travellingItem.hasDisplayList) {
            travellingItem.displayList = GLAllocation.generateDisplayLists(1);
            travellingItem.hasDisplayList = true;

            GL11.glNewList(travellingItem.displayList, GL11.GL_COMPILE);
            if (itemstack.getItem() instanceof IItemCustomPipeRender) {
                IItemCustomPipeRender render = (IItemCustomPipeRender) itemstack.getItem();
                float itemScale = render.getPipeRenderScale(itemstack);
                GL11.glScalef(renderScale * itemScale, renderScale * itemScale, renderScale * itemScale);
                itemScale = 1 / itemScale;

                if (!render.renderItemInPipe(itemstack, x, y, z)) {
                    dummyEntityItem.setEntityItemStack(itemstack);
                    customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
                }

                GL11.glScalef(itemScale, itemScale, itemScale);
            } else {
                GL11.glScalef(renderScale, renderScale, renderScale);
                dummyEntityItem.setEntityItemStack(itemstack);
                customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
            }
            GL11.glEndList();
        }

        GL11.glCallList(travellingItem.displayList);
        // Some items don't reset their colour properly, so lets just kick both the state manager AND OpenGL to the same
        // state
        GL11.glColor4f(1, 1, 1, 1);
        GlStateManager.color(1, 1, 1, 1);

        GL11.glPopMatrix();
        if (color != null) {// The box around an item that decides what colour lenses it can go through
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            EntityResizableCuboid erc = new EntityResizableCuboid(null);
            erc.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.ItemBox.ordinal());
            erc.xSize = 1;
            erc.ySize = 1;
            erc.zSize = 1;

            GL11.glPushMatrix();
            renderScale /= 2f;
            GL11.glTranslatef(0, 0.2f, 0);
            GL11.glScalef(renderScale, renderScale, renderScale);
            GL11.glTranslatef(-0.5f, -0.5f, -0.5f);

            RenderUtil.setGLColorFromInt(ColorUtils.getLightHex(color));
            RenderResizableCuboid.INSTANCE.renderCube(erc);
            GlStateManager.color(1, 1, 1, 1);

            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

    }
}
