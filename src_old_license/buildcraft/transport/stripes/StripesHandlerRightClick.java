package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerRightClick implements IStripesHandler {
    public static final List<Item> items = new ArrayList<>();

    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return (stack.getItem() == Items.potionitem && ItemPotion.isSplash(stack.getItemDamage())) || items.contains(stack.getItem());
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        ItemStack remainingStack = stack.getItem().onItemRightClick(stack, world, player);
        activator.sendItem(remainingStack, direction.getOpposite());
        return true;
    }

}
