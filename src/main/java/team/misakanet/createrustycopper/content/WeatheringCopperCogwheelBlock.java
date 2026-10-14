package team.misakanet.createrustycopper.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public final class WeatheringCopperCogwheelBlock extends CopperCogwheelBlock implements WeatheringCopperKineticBlock {
    public WeatheringCopperCogwheelBlock(Properties properties, boolean large, CopperVariant variant) {
        super(properties, large, variant);
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
