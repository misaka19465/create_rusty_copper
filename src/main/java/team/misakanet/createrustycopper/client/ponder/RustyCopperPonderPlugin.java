package team.misakanet.createrustycopper.client.ponder;

import java.util.Arrays;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import team.misakanet.createrustycopper.CreateRustyCopper;
import team.misakanet.createrustycopper.content.CopperVariant;
import team.misakanet.createrustycopper.registry.ModBlocks;

public final class RustyCopperPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateRustyCopper.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        var shafts = Arrays.stream(CopperVariant.values())
                .map(variant -> ModBlocks.of(variant).shaft().getId()).toList();

        // Reuse Create's schematics at runtime; only the teaching subject is replaced.
        ResourceLocation relay = ResourceLocation.fromNamespaceAndPath("create", "shaft/relay");
        ResourceLocation encasing = ResourceLocation.fromNamespaceAndPath("create", "shaft/encasing");
        helper.forComponents(shafts)
                .addStoryBoard(relay, CopperShaftScenes::relay, AllCreatePonderTags.KINETIC_RELAYS)
                .addStoryBoard(encasing, CopperShaftScenes::encasing)
                .addStoryBoard(relay, CopperShaftScenes::weathering)
                .addStoryBoard(relay, CopperShaftScenes::maintenance);
    }
}
