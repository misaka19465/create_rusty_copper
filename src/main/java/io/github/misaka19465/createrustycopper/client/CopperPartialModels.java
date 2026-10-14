package io.github.misaka19465.createrustycopper.client;

import java.util.HashMap;
import java.util.Map;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.misaka19465.createrustycopper.CreateRustyCopper;
import io.github.misaka19465.createrustycopper.content.CopperKineticBlock;
import io.github.misaka19465.createrustycopper.content.CopperVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class CopperPartialModels {
    private static final Map<String, Parts> PARTS = new HashMap<>();

    static {
        for (CopperVariant variant : CopperVariant.values()) {
            PARTS.computeIfAbsent(variant.modelPrefix(), prefix -> new Parts(
                    partial(prefix + "cogwheel"), partial(prefix + "cogwheel_shaftless"),
                    partial(prefix + "large_cogwheel_shaftless"), partial(prefix + "cogwheel_shaft"),
                    partial(prefix + "shaft"), partial(prefix + "shaft_half")));
        }
    }

    private CopperPartialModels() {
    }

    private static PartialModel partial(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateRustyCopper.MOD_ID, "block/" + path));
    }

    public static Parts of(BlockState state) {
        return PARTS.get(((CopperKineticBlock) state.getBlock()).variant().modelPrefix());
    }

    public static void init() {
    }

    public record Parts(PartialModel cogwheel, PartialModel shaftlessCogwheel, PartialModel shaftlessLargeCogwheel,
            PartialModel cogwheelShaft, PartialModel shaft, PartialModel shaftHalf) {
    }
}
