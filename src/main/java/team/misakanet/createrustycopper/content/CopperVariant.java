package team.misakanet.createrustycopper.content;

import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

public enum CopperVariant {
    COPPER("", WeatherState.UNAFFECTED, false),
    EXPOSED("exposed_", WeatherState.EXPOSED, false),
    WEATHERED("weathered_", WeatherState.WEATHERED, false),
    OXIDIZED("oxidized_", WeatherState.OXIDIZED, false),
    WAXED_COPPER("", WeatherState.UNAFFECTED, true),
    WAXED_EXPOSED("exposed_", WeatherState.EXPOSED, true),
    WAXED_WEATHERED("weathered_", WeatherState.WEATHERED, true),
    WAXED_OXIDIZED("oxidized_", WeatherState.OXIDIZED, true);

    private final String stagePrefix;
    private final WeatherState weatherState;
    private final boolean waxed;

    CopperVariant(String stagePrefix, WeatherState weatherState, boolean waxed) {
        this.stagePrefix = stagePrefix;
        this.weatherState = weatherState;
        this.waxed = waxed;
    }

    public WeatherState weatherState() {
        return weatherState;
    }

    public boolean waxed() {
        return waxed;
    }

    public String modelPrefix() {
        return stagePrefix + "copper_";
    }

    public String blockId(String component) {
        return blockId(component, "");
    }

    public String blockId(String component, String casing) {
        return (waxed ? "waxed_" : "") + (casing.isEmpty() ? "" : casing + "_encased_")
                + modelPrefix() + component;
    }
}
