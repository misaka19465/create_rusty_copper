package io.github.misaka19465.createrustycopper.content;

public enum CopperVariant {
    COPPER("", false),
    EXPOSED("exposed_", false),
    WEATHERED("weathered_", false),
    OXIDIZED("oxidized_", false),
    WAXED_COPPER("", true),
    WAXED_EXPOSED("exposed_", true),
    WAXED_WEATHERED("weathered_", true),
    WAXED_OXIDIZED("oxidized_", true);

    private final String stagePrefix;
    private final boolean waxed;

    CopperVariant(String stagePrefix, boolean waxed) {
        this.stagePrefix = stagePrefix;
        this.waxed = waxed;
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
