package io.github.misaka19465.createrustycopper.registry;

import java.util.Arrays;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import io.github.misaka19465.createrustycopper.CreateRustyCopper;
import io.github.misaka19465.createrustycopper.content.CopperVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateRustyCopper.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BracketedKineticBlockEntity>> BARE =
            BLOCK_ENTITIES.register("bracketed_kinetic", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new BracketedKineticBlockEntity(ModBlockEntities.BARE.get(), pos, state),
                    Arrays.stream(CopperVariant.values()).flatMap(v -> ModBlocks.of(v).bareBlocks().stream())
                            .toArray(Block[]::new)).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SimpleKineticBlockEntity>> ENCASED_COGWHEEL =
            BLOCK_ENTITIES.register("encased_cogwheel", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new SimpleKineticBlockEntity(ModBlockEntities.ENCASED_COGWHEEL.get(), pos, state),
                    Arrays.stream(CopperVariant.values()).flatMap(v -> ModBlocks.of(v).encasedCogwheels())
                            .toArray(Block[]::new)).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KineticBlockEntity>> ENCASED_SHAFT =
            BLOCK_ENTITIES.register("encased_shaft", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new KineticBlockEntity(ModBlockEntities.ENCASED_SHAFT.get(), pos, state),
                    Arrays.stream(CopperVariant.values()).flatMap(v -> ModBlocks.of(v).encasedShafts())
                            .toArray(Block[]::new)).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
