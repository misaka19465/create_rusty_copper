package team.misakanet.createrustycopper.client;

import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import team.misakanet.createrustycopper.CreateRustyCopper;
import team.misakanet.createrustycopper.content.CopperVariant;
import team.misakanet.createrustycopper.registry.ModBlockEntities;
import team.misakanet.createrustycopper.registry.ModBlocks;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreateRustyCopper.MOD_ID, dist = Dist.CLIENT)
public final class CreateRustyCopperClient {
    public CreateRustyCopperClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent));
        CopperPartialModels.init();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::wrapModels);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            SimpleBlockEntityVisualizer.builder(ModBlockEntities.BARE.get()).factory(CopperKineticVisual::new)
                    .skipVanillaRender(blockEntity -> false).apply();
            SimpleBlockEntityVisualizer.builder(ModBlockEntities.ENCASED_COGWHEEL.get()).factory(CopperKineticVisual::new)
                    .skipVanillaRender(blockEntity -> false).apply();
            SimpleBlockEntityVisualizer.builder(ModBlockEntities.ENCASED_SHAFT.get()).factory(CopperKineticVisual::new)
                    .skipVanillaRender(blockEntity -> false).apply();
        });
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BARE.get(), CopperKineticRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENCASED_COGWHEEL.get(), CopperKineticRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENCASED_SHAFT.get(), CopperKineticRenderer::new);
    }

    private void wrapModels(ModelEvent.ModifyBakingResult event) {
        for (CopperVariant variant : CopperVariant.values()) {
            for (var block : ModBlocks.of(variant).bareBlocks()) {
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    var location = BlockModelShaper.stateToModelLocation(state);
                    event.getModels().computeIfPresent(location, (key, model) -> new BracketedKineticBlockModel(model));
                }
            }
        }
    }
}
