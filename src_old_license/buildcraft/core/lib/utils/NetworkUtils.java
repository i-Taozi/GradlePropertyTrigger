package buildcraft.core.lib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import io.netty.buffer.ByteBuf;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Deprecated
public final class NetworkUtils {
    private NetworkUtils() {

    }

    public static void writeUTF(ByteBuf data, String str) {
        try {
            if (str == null || str.length() == 0) {
                data.writeInt(0);
                return;
            }
            byte[] b = str.getBytes("UTF-8");
            data.writeInt(b.length);
            data.writeBytes(b);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            data.writeInt(0);
        }
    }

    public static String readUTF(ByteBuf data) {
        try {
            int len = data.readInt();
            if (len == 0) {
                return "";
            }
            byte[] b = new byte[len];
            data.readBytes(b);
            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeNBT(ByteBuf data, NBTTagCompound nbt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(nbt, baos);
            byte[] compressed = baos.toByteArray();
            data.writeInt(compressed.length);
            data.writeBytes(compressed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NBTTagCompound readNBT(ByteBuf data) {
        try {
            int length = data.readInt();
            byte[] compressed = new byte[length];
            data.readBytes(compressed);
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            return CompressedStreamTools.readCompressed(bais);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeStack(ByteBuf data, ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize < 0) {
            data.writeByte(0);
        } else {
            // ItemStacks generally shouldn't have a stackSize above 64,
            // so we use this "trick" to save bandwidth by storing it in the first byte.
            data.writeByte((MathUtils.clamp(stack.stackSize + 1, 0, 64) & 0x7F) | (stack.hasTagCompound() ? 128 : 0));
            data.writeShort(Item.getIdFromItem(stack.getItem()));
            data.writeShort(stack.getItemDamage());
            if (stack.hasTagCompound()) {
                writeNBT(data, stack.getTagCompound());
            }
        }
    }

    public static ItemStack readStack(ByteBuf data) {
        int flags = data.readUnsignedByte();
        if (flags == 0) {
            return null;
        } else {
            boolean hasCompound = (flags & 0x80) != 0;
            int stackSize = (flags & 0x7F) - 1;
            int itemId = data.readUnsignedShort();
            int itemDamage = data.readShort();
            ItemStack stack = new ItemStack(Item.getItemById(itemId), stackSize, itemDamage);
            if (hasCompound) {
                stack.setTagCompound(readNBT(data));
            }
            return stack;
        }
    }

    public static void writeByteArray(ByteBuf stream, byte[] data) {
        stream.writeInt(data.length);
        stream.writeBytes(data);
    }

    public static byte[] readByteArray(ByteBuf stream) {
        byte[] data = new byte[stream.readInt()];
        stream.readBytes(data, 0, data.length);
        return data;
    }

    public static void writeVec3d(ByteBuf stream, Vec3d vector) {
        stream.writeDouble(vector.xCoord);
        stream.writeDouble(vector.yCoord);
        stream.writeDouble(vector.zCoord);
    }

    public static Vec3d readVec3d(ByteBuf stream) {
        double x = stream.readDouble();
        double y = stream.readDouble();
        double z = stream.readDouble();
        return new Vec3d(x, y, z);
    }

    public static void writeBlockPos(ByteBuf stream, BlockPos pos) {
        stream.writeInt(pos.getX());
        stream.writeInt(pos.getY());
        stream.writeInt(pos.getZ());
    }

    public static BlockPos readBlockPos(ByteBuf stream) {
        return new BlockPos(stream.readInt(), stream.readInt(), stream.readInt());
    }

    public static <E extends Enum<E>> void writeEnum(ByteBuf stream, E value) {
        // TODO: Make this check if the value implements "INetSerialisable" and write using that.
        if (value == null) writeUTF(stream, "");
        else writeUTF(stream, value.name());
    }

    public static <E extends Enum<E>> E readEnum(ByteBuf stream, Class<E> clazz) {
        String s = readUTF(stream);
        for (E val : clazz.getEnumConstants()) {
            if (val.name().equals(s)) return val;
        }
        return null;
    }
}
