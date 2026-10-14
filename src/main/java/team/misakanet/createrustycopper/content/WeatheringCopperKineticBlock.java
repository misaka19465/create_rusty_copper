package team.misakanet.createrustycopper.content;

import net.minecraft.world.level.block.WeatheringCopper;

public interface WeatheringCopperKineticBlock extends CopperKineticBlock, WeatheringCopper {
    @Override
    default WeatherState getAge() {
        return variant().weatherState();
    }
}
