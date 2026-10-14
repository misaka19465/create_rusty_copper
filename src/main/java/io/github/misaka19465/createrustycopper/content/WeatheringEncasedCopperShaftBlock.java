package io.github.misaka19465.createrustycopper.content;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public final class WeatheringEncasedCopperShaftBlock extends EncasedCopperShaftBlock
        implements WeatheringCopperKineticBlock {
    public WeatheringEncasedCopperShaftBlock(Properties properties, Supplier<Block> casing, Supplier<CopperShaftBlock> shaft,
            CopperVariant variant) {
        super(properties, casing, shaft, variant);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        changeOverTime(state, level, pos, random);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return getAge() != WeatheringCopper.WeatherState.OXIDIZED;
    }
}
