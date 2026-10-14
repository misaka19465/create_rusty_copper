package io.github.misaka19465.createrustycopper.content;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import io.github.misaka19465.createrustycopper.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class CopperShaftBlock extends ShaftBlock implements CopperKineticBlock {
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
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BARE.get();
    }
}
