package buildcraft.transport.statements;

import javax.annotation.Nullable;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;

public class TriggerPowerRequested extends BCStatement implements ITriggerInternal {

    public TriggerPowerRequested() {
        super("buildcraft:powerRequested");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (!(source instanceof IGate)) {
            return false;
        }
        PipeFlow f = ((IGate) source).getPipeHolder().getPipe().getFlow();
        if (!(f instanceof PipeFlowPower)) {
            return false;
        }
        final PipeFlowPower flow = (PipeFlowPower) f;

        return flow.getPowerRequested(null) > 0;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.pipe.requestsEnergy");
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_POWER_REQUESTED;
    }

}
