package io.github.misaka19465.createrustycopper;

import io.github.misaka19465.createrustycopper.config.ModServerConfig;
import io.github.misaka19465.createrustycopper.content.CopperStress;
import io.github.misaka19465.createrustycopper.registry.ModBlocks;
import io.github.misaka19465.createrustycopper.registry.ModBlockEntities;
import io.github.misaka19465.createrustycopper.registry.ModCreativeTabs;
import io.github.misaka19465.createrustycopper.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(CreateRustyCopper.MOD_ID)
public final class CreateRustyCopperMod {
    public CreateRustyCopperMod(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ModServerConfig.SPEC);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CreateRustyCopper.init();
            ModBlocks.registerEncasing();
            CopperStress.register();
        });
    }
}

