package com.dynious.refinedrelocation.network.packet.gui;

import com.dynious.refinedrelocation.container.IContainerNetworked;
import com.dynious.refinedrelocation.network.NetworkHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class MessageGUIAction extends MessageGUI implements IMessageHandler<MessageGUIAction, IMessage>
{

    public MessageGUIAction()
    {
    }

    public MessageGUIAction(int id)
    {
        super(id);
    }

    @Override
    public IMessage onMessage(MessageGUIAction message, MessageContext ctx)
    {
        EntityPlayer entityPlayer = ctx.side == Side.SERVER ? ctx.getServerHandler().playerEntity : NetworkHandler.getClientPlayerEntity();
        Container container = entityPlayer.openContainer;
        if (container == null || !(container instanceof IContainerNetworked))
        {
            return null;
        }

        ((IContainerNetworked) container).onMessageAction(message.id, entityPlayer, ctx.side);

        return null;
    }

}
