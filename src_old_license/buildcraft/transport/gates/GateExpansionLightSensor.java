/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gates;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.statements.ITriggerInternal;

public final class GateExpansionLightSensor extends GateExpansionBuildcraft implements IGateExpansion {

    public static GateExpansionLightSensor INSTANCE = new GateExpansionLightSensor();

    private GateExpansionLightSensor() {
        super("light_sensor");
    }

    @Override
    public GateExpansionController makeController(TileEntity pipeTile) {
        return new GateExpansionControllerLightSensor(pipeTile);
    }

    @Override
    public float getPixelExtrusion() {
        /* Otherwise this conflicts with the redstone fader and autarchic pulsar */
        return 0.02f;
    }

    private class GateExpansionControllerLightSensor extends GateExpansionController {

        public GateExpansionControllerLightSensor(TileEntity pipeTile) {
            super(GateExpansionLightSensor.this, pipeTile);
        }

        @Override
        public void addTriggers(Collection<ITriggerInternal> list) {
            super.addTriggers(list);
            list.add(BuildCraftTransport.triggerLightSensorBright);
            list.add(BuildCraftTransport.triggerLightSensorDark);
        }
    }
}
