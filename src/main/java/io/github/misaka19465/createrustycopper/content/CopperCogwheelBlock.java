package io.github.misaka19465.createrustycopper.content;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import io.github.misaka19465.createrustycopper.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class CopperCogwheelBlock extends CogWheelBlock implements CopperKineticBlock {
    private final CopperVariant variant;

    public CopperCogwheelBlock(Properties properties, boolean large, CopperVariant variant) {
        super(large, properties);
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
