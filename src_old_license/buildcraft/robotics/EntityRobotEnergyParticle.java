/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityRobotEnergyParticle extends EntityFX {
    private float smokeParticleScale;

    public EntityRobotEnergyParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        this(world, x, y, z, vx, vy, vz, 1.0F);
    }

    public EntityRobotEnergyParticle(World world, double x, double y, double z, double vx, double vy, double vz, float size) {
        super(world, x, y, z, vx, vy, vz);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += vx;
        this.motionY += vy;
        this.motionZ += vz;
        this.particleRed = (float) (Math.random() * 0.6);
        this.particleGreen = 0;
        this.particleBlue = 0;
        this.particleScale *= 0.75F;
        this.particleScale *= size;
        this.smokeParticleScale = this.particleScale;
        this.particleMaxAge = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
        this.particleMaxAge = (int) (this.particleMaxAge * size);
        this.noClip = false;
    }

    @Override
    public void renderParticle(WorldRenderer worldRenderer, Entity entity, float partialTicks, float f1, float f2, float f3, float f4, float f5) {
        float f6 = (this.particleAge + partialTicks) / this.particleMaxAge * 32.0F;

        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f6 > 1.0F) {
            f6 = 1.0F;
        }

        this.particleScale = this.smokeParticleScale * f6;// FIXME EntityRobotEnergyParticle
        super.renderParticle(worldRenderer, entity, partialTicks, f1, f2, f3, f4, f5);
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        this.motionX *= 0.98;
        this.motionY += 0.0005;
        this.motionZ *= 0.98;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }
}
