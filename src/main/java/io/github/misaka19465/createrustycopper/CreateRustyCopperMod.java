package io.github.misaka19465.createrustycopper;

import io.github.misaka19465.createrustycopper.registry.ModBlocks;
import io.github.misaka19465.createrustycopper.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(CreateRustyCopper.MOD_ID)
public final class CreateRustyCopperMod {
    public CreateRustyCopperMod(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CreateRustyCopper::init);
    }
}

