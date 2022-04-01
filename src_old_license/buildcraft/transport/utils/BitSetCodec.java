/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.utils;

import java.util.BitSet;

public class BitSetCodec {
    public byte encode(BitSet set) {
        byte result = 0;
        for (byte i = 0; i < 8; i++) {
            result |= set.get(i) ? (1 << i) : 0;
        }
        return result;
    }

    public boolean decode(byte data, BitSet target) {
        byte localData = data;
        int t = 1;

        boolean dirty = false;

        for (byte i = 0; i < 8; i++) {
            boolean newValue = (localData & t) != 0;
            boolean current = target.get(i);
            if (newValue != current) {
                dirty = true;
                target.set(i, newValue);
            }
            t <<= 1;
        }

        return dirty;
    }
}
