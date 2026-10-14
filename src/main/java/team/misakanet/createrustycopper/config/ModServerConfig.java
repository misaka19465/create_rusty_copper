package team.misakanet.createrustycopper.config;

import team.misakanet.createrustycopper.CreateRustyCopper;
import team.misakanet.createrustycopper.content.CopperVariant;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue RUST_PENALTY_ENABLED;
    public static final ModConfigSpec.DoubleValue EXPOSED_STRESS_IMPACT;
    public static final ModConfigSpec.DoubleValue WEATHERED_STRESS_IMPACT;
    public static final ModConfigSpec.DoubleValue OXIDIZED_STRESS_IMPACT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        String prefix = CreateRustyCopper.MOD_ID + ".configuration.";
        builder.comment("Additional stress consumed by copper transmission components at each oxidation stage.",
                        "Values are in SU per RPM per component; waxing and casing do not remove existing oxidation.",
                        "Create's disableStress setting also disables these penalties.")
                .translation(prefix + "rustPenalty").push("rustPenalty");
        RUST_PENALTY_ENABLED = builder.comment("Enable extra stress consumption from copper oxidation.")
                .translation(prefix + "enabled").define("enabled", true);
        EXPOSED_STRESS_IMPACT = builder.comment("Additional stress impact of exposed copper. Zero disables this stage's penalty.")
                .translation(prefix + "exposedStressImpact").defineInRange("exposedStressImpact", 8.0, 0.0, 1.0E12);
        WEATHERED_STRESS_IMPACT = builder.comment("Additional stress impact of weathered copper. Zero disables this stage's penalty.")
                .translation(prefix + "weatheredStressImpact").defineInRange("weatheredStressImpact", 32.0, 0.0, 1.0E12);
        OXIDIZED_STRESS_IMPACT = builder.comment("Additional stress impact of fully oxidized copper.",
                        "The very high default overloads ordinary networks, stopping all connected machinery.",
                        "This is a finite stress cost, so enough capacity can overcome it. Zero disables this stage's penalty.")
                .translation(prefix + "oxidizedStressImpact").defineInRange("oxidizedStressImpact", 1.0E9, 0.0, 1.0E12);
        builder.pop();
        SPEC = builder.build();
    }

    private ModServerConfig() {
    }

    public static double stressImpact(CopperVariant variant) {
        if (!value(RUST_PENALTY_ENABLED)) {
            return 0;
        }
        return switch (variant.weatherState()) {
            case UNAFFECTED -> 0;
            case EXPOSED -> value(EXPOSED_STRESS_IMPACT);
            case WEATHERED -> value(WEATHERED_STRESS_IMPACT);
            case OXIDIZED -> value(OXIDIZED_STRESS_IMPACT);
        };
    }

    private static <T> T value(ModConfigSpec.ConfigValue<T> config) {
        return SPEC.isLoaded() ? config.get() : config.getDefault();
    }
}
