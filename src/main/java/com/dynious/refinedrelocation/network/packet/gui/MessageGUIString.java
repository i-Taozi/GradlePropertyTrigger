package com.dynious.refinedrelocation.network.packet.gui;

import com.dynious.refinedrelocation.container.IContainerNetworked;
import com.dynious.refinedrelocation.network.NetworkHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class MessageGUIString extends MessageGUI implements IMessageHandler<MessageGUIString, IMessage>
{

    private String value = "";

    public MessageGUIString()
    {
    }

    public MessageGUIString(int id, String value)
    {
        super(id);
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        value = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, value);
    }

    @Override
    public IMessage onMessage(MessageGUIString message, MessageContext ctx)
    {
        EntityPlayer entityPlayer = ctx.side == Side.SERVER ? ctx.getServerHandler().playerEntity : NetworkHandler.getClientPlayerEntity();
        Container container = entityPlayer.openContainer;
        if (container == null || !(container instanceof IContainerNetworked))
        {
            return null;
        }

        ((IContainerNetworked) container).onMessageString(message.id, message.value, entityPlayer, ctx.side);

        return null;
    }

}
