package team.misakanet.createrustycopper.content;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import team.misakanet.createrustycopper.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CopperShaftBlock extends ShaftBlock implements CopperKineticBlock {
    private final CopperVariant variant;

    public CopperShaftBlock(Properties properties, CopperVariant variant) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public CopperVariant variant() {
        return variant;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!CopperKineticBlock.isAppearanceChange(state, newState)) {
            super.onRemove(state, level, pos, newState, moving);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (!CopperKineticBlock.preserveKinetics(state, level, pos, oldState)) {
            super.onPlace(state, level, pos, oldState, moving);
        }
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BARE.get();
    }
}
