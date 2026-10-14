package io.github.misaka19465.createrustycopper.registry;

import io.github.misaka19465.createrustycopper.CreateRustyCopper;
import io.github.misaka19465.createrustycopper.content.CopperVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateRustyCopper.MOD_ID);

    static {
        TABS.register("main", () -> CreativeModeTab.builder()
                .title(Component.translatable("mod.create_rusty_copper.name"))
                .icon(() -> new ItemStack(ModBlocks.of(CopperVariant.COPPER).cogwheel().get()))
                .displayItems((parameters, output) -> {
                    for (CopperVariant variant : CopperVariant.values()) {
                        ModBlocks.of(variant).bareBlocks().forEach(output::accept);
                    }
                }).build());
    }

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
