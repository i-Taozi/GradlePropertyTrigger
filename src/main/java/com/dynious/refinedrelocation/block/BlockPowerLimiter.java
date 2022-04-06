package com.dynious.refinedrelocation.block;

import cofh.api.block.IDismantleable;
import com.dynious.refinedrelocation.RefinedRelocation;
import com.dynious.refinedrelocation.client.renderer.DirectionalRenderer;
import com.dynious.refinedrelocation.helper.GuiHelper;
import com.dynious.refinedrelocation.helper.IOHelper;
import com.dynious.refinedrelocation.lib.Mods;
import com.dynious.refinedrelocation.lib.Names;
import com.dynious.refinedrelocation.lib.Resources;
import com.dynious.refinedrelocation.tileentity.TilePowerLimiter;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;

@Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = Mods.COFH_BLOCK_API_ID)
public class BlockPowerLimiter extends BlockContainer implements IDismantleable
{
    private final IIcon[] icons = new IIcon[3];
    private final IIcon[] iconsDisabled = new IIcon[3];

    public BlockPowerLimiter()
    {
        super(Material.rock);
        this.setBlockName(Names.powerLimiter);
        this.setHardness(3.0F);
        this.setCreativeTab(RefinedRelocation.tabRefinedRelocation);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TilePowerLimiter();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        if (!world.isRemote)
        {
            TilePowerLimiter tile = (TilePowerLimiter) world.getTileEntity(x, y, z);
            if (player.isSneaking())
            {
                tile.setDisablePower(!tile.getDisablePower());
                return true;
            }
            GuiHelper.openGui(player, tile);
        }
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
    {
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block par5)
    {
        super.onNeighborBlockChange(world, x, y, z, par5);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null && tile instanceof TilePowerLimiter)
        {
            ((TilePowerLimiter) tile).blocksChanged = true;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        icons[0] = iconRegister.registerIcon(Resources.MOD_ID + ":" + Names.powerLimiter + "Front");
        icons[1] = iconRegister.registerIcon(Resources.MOD_ID + ":" + Names.powerLimiter + "Back");
        icons[2] = iconRegister.registerIcon(Resources.MOD_ID + ":" + Names.powerLimiter + "Side");

        iconsDisabled[0] = icons[0];
        iconsDisabled[1] = iconRegister.registerIcon(Resources.MOD_ID + ":" + Names.powerLimiter + "BackDisabled");
        iconsDisabled[2] = iconRegister.registerIcon(Resources.MOD_ID + ":" + Names.powerLimiter + "SideDisabled");
    }

    @Override
    public IIcon getIcon(int par1, int par2)
    {
        return icons[1];
    }


    @Override
    public IIcon getIcon(IBlockAccess worldObj, int x, int y, int z, int side)
    {
        TilePowerLimiter tile = (TilePowerLimiter) worldObj.getTileEntity(x, y, z);
        IIcon[] icons = this.icons;
        if(tile.getDisablePower()) {
            icons = iconsDisabled;
        }
        int sideIdx;
        if (tile.getConnectedDirection().ordinal() == side)
        {
            sideIdx = 0;
        } else if(tile.getConnectedDirection().getOpposite().ordinal() == side) {
            sideIdx = 1;
        }
        else  {
            sideIdx = 2;
        }
        return icons[sideIdx];
    }

    @Override
    public int getRenderType()
    {
        return DirectionalRenderer.renderId;
    }

    @Optional.Method(modid = Mods.COFH_BLOCK_API_ID)
    @Override
    public ArrayList<ItemStack> dismantleBlock(EntityPlayer player, World world, int x,
                                               int y, int z, boolean returnBlock)
    {
        int meta = world.getBlockMetadata(x, y, z);

        ArrayList<ItemStack> items = this.getDrops(world, x, y, z, meta, 0);

        for (ItemStack item : items)
        {
            IOHelper.spawnItemInWorld(world, item, x, y, z);
        }

        world.setBlockToAir(x, y, z);
        return null;
    }

    @Optional.Method(modid = Mods.COFH_BLOCK_API_ID)
    @Override
    public boolean canDismantle(EntityPlayer player, World world, int x, int y, int z)
    {
        return true;
    }

    @Override
    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
    {
        TilePowerLimiter tile = (TilePowerLimiter) worldObj.getTileEntity(x, y, z);
        return tile.rotateBlock();
    }
}
