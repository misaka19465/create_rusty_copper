package io.github.misaka19465.createrustycopper.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import io.github.misaka19465.createrustycopper.CreateRustyCopper;
import io.github.misaka19465.createrustycopper.content.CopperCogwheelBlock;
import io.github.misaka19465.createrustycopper.content.CopperShaftBlock;
import io.github.misaka19465.createrustycopper.content.CopperVariant;
import io.github.misaka19465.createrustycopper.content.EncasedCopperCogwheelBlock;
import io.github.misaka19465.createrustycopper.content.EncasedCopperShaftBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CreateRustyCopper.MOD_ID);

    private static final Map<CopperVariant, Components> VARIANTS = new EnumMap<>(CopperVariant.class);

    static {
        for (CopperVariant variant : CopperVariant.values()) {
            DeferredBlock<CopperCogwheelBlock> cogwheel = BLOCKS.register(variant.blockId("cogwheel"),
                    () -> new CopperCogwheelBlock(properties(), false, variant));
            DeferredBlock<CopperCogwheelBlock> largeCogwheel = BLOCKS.register(variant.blockId("large_cogwheel"),
                    () -> new CopperCogwheelBlock(properties(), true, variant));
            DeferredBlock<CopperShaftBlock> shaft = BLOCKS.register(variant.blockId("shaft"),
                    () -> new CopperShaftBlock(properties().forceSolidOff(), variant));
            VARIANTS.put(variant, new Components(cogwheel, largeCogwheel, shaft,
                    encasedCogwheel(variant, false, "andesite", cogwheel),
                    encasedCogwheel(variant, true, "andesite", largeCogwheel),
                    encasedShaft(variant, "andesite", shaft),
                    encasedCogwheel(variant, false, "brass", cogwheel),
                    encasedCogwheel(variant, true, "brass", largeCogwheel),
                    encasedShaft(variant, "brass", shaft)));
        }
    }

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.5F, 6.0F)
                .sound(SoundType.COPPER).noOcclusion();
    }

    private static DeferredBlock<EncasedCopperCogwheelBlock> encasedCogwheel(CopperVariant variant, boolean large,
            String casing, DeferredBlock<CopperCogwheelBlock> bare) {
        return BLOCKS.register(variant.blockId(large ? "large_cogwheel" : "cogwheel", casing),
                () -> new EncasedCopperCogwheelBlock(properties(), large,
                        () -> casing.equals("andesite") ? AllBlocks.ANDESITE_CASING.get() : AllBlocks.BRASS_CASING.get(),
                        bare, variant));
    }

    private static DeferredBlock<EncasedCopperShaftBlock> encasedShaft(CopperVariant variant, String casing,
            DeferredBlock<CopperShaftBlock> bare) {
        return BLOCKS.register(variant.blockId("shaft", casing), () -> new EncasedCopperShaftBlock(properties(),
                () -> casing.equals("andesite") ? AllBlocks.ANDESITE_CASING.get() : AllBlocks.BRASS_CASING.get(),
                bare, variant));
    }

    public static Components of(CopperVariant variant) {
        return VARIANTS.get(variant);
    }

    public static void registerEncasing() {
        for (Components components : VARIANTS.values()) {
            EncasingRegistry.addVariant(components.cogwheel().get(), components.andesiteCogwheel().get());
            EncasingRegistry.addVariant(components.cogwheel().get(), components.brassCogwheel().get());
            EncasingRegistry.addVariant(components.largeCogwheel().get(), components.andesiteLargeCogwheel().get());
            EncasingRegistry.addVariant(components.largeCogwheel().get(), components.brassLargeCogwheel().get());
            EncasingRegistry.addVariant(components.shaft().get(), components.andesiteShaft().get());
            EncasingRegistry.addVariant(components.shaft().get(), components.brassShaft().get());
        }
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

    public record Components(DeferredBlock<CopperCogwheelBlock> cogwheel,
            DeferredBlock<CopperCogwheelBlock> largeCogwheel, DeferredBlock<CopperShaftBlock> shaft,
            DeferredBlock<EncasedCopperCogwheelBlock> andesiteCogwheel,
            DeferredBlock<EncasedCopperCogwheelBlock> andesiteLargeCogwheel,
            DeferredBlock<EncasedCopperShaftBlock> andesiteShaft,
            DeferredBlock<EncasedCopperCogwheelBlock> brassCogwheel,
            DeferredBlock<EncasedCopperCogwheelBlock> brassLargeCogwheel,
            DeferredBlock<EncasedCopperShaftBlock> brassShaft) {

        public List<Block> bareBlocks() {
            return List.of(cogwheel.get(), largeCogwheel.get(), shaft.get());
        }

        public Stream<Block> encasedCogwheels() {
            return Stream.of(andesiteCogwheel.get(), andesiteLargeCogwheel.get(), brassCogwheel.get(),
                    brassLargeCogwheel.get());
        }

        public Stream<Block> encasedShafts() {
            return Stream.of(andesiteShaft.get(), brassShaft.get());
        }
    }
}

