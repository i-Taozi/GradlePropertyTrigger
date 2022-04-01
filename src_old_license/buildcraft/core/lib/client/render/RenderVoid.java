/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderVoid<E extends Entity> extends Render<E> {
    public RenderVoid() {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    @Override
    public void doRender(E entity, double d, double d1, double d2, float f, float f1) {

    }

    @Override
    protected ResourceLocation getEntityTexture(E entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
