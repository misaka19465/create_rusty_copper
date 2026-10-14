package team.misakanet.createrustycopper.content;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import team.misakanet.createrustycopper.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface CopperKineticBlock {
    CopperVariant variant();

    static boolean isAppearanceChange(BlockState oldState, BlockState newState) {
        if (oldState.is(newState.getBlock())
                || !(oldState.getBlock() instanceof CopperKineticBlock before)
                || !(newState.getBlock() instanceof CopperKineticBlock after)
                || !oldState.getValues().equals(newState.getValues())) {
            return false;
        }

        int component = ModBlocks.of(before.variant()).allBlocks().indexOf(oldState.getBlock());
        return component >= 0 && ModBlocks.of(after.variant()).allBlocks().get(component) == newState.getBlock();
    }

    static boolean preserveKinetics(BlockState state, Level level, BlockPos pos, BlockState oldState) {
        if (!isAppearanceChange(oldState, state)) {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof KineticBlockEntity kinetic) {
            // Vanilla item and weathering handlers replace blocks directly. Keep Create's data and connections.
            kinetic.setBlockState(state);
            kinetic.preventSpeedUpdate = 2;
            CopperStress.refresh(kinetic);
            kinetic.notifyUpdate();
        }
        return true;
    }
}
