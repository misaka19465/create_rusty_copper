package team.misakanet.createrustycopper.registry;

import com.simibubi.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import team.misakanet.createrustycopper.CreateRustyCopper;
import team.misakanet.createrustycopper.content.CopperVariant;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CreateRustyCopper.MOD_ID);

    static {
        for (CopperVariant variant : CopperVariant.values()) {
            ModBlocks.Components blocks = ModBlocks.of(variant);
            ITEMS.register(variant.blockId("cogwheel"), () -> new CogwheelBlockItem(blocks.cogwheel().get(), new Item.Properties()));
            ITEMS.register(variant.blockId("large_cogwheel"),
                    () -> new CogwheelBlockItem(blocks.largeCogwheel().get(), new Item.Properties()));
            ITEMS.registerSimpleBlockItem(blocks.shaft());
        }
    }

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

