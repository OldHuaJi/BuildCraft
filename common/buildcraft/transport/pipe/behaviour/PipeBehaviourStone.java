package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.PipeBehaviour;

public class PipeBehaviourStone extends PipeBehaviour {
    public PipeBehaviourStone(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStone(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return 0;
    }
}