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

        var smallCogwheels = Arrays.stream(CopperVariant.values())
                .map(variant -> ModBlocks.of(variant).cogwheel().getId()).toList();
        var largeCogwheels = Arrays.stream(CopperVariant.values())
                .map(variant -> ModBlocks.of(variant).largeCogwheel().getId()).toList();
        ResourceLocation small = ResourceLocation.fromNamespaceAndPath("create", "cog/small");
        ResourceLocation large = ResourceLocation.fromNamespaceAndPath("create", "cog/large");
        ResourceLocation speedup = ResourceLocation.fromNamespaceAndPath("create", "cog/speedup");
        ResourceLocation cogEncasing = ResourceLocation.fromNamespaceAndPath("create", "cog/encasing");
        helper.forComponents(smallCogwheels)
                .addStoryBoard(small, CopperCogwheelScenes::smallRelay, AllCreatePonderTags.KINETIC_RELAYS)
                .addStoryBoard(speedup, CopperCogwheelScenes::smallSpeedup)
                .addStoryBoard(cogEncasing, CopperCogwheelScenes::smallEncasing)
                .addStoryBoard(speedup, CopperCogwheelScenes::smallWeathering)
                .addStoryBoard(speedup, CopperCogwheelScenes::smallMaintenance);
        helper.forComponents(largeCogwheels)
                .addStoryBoard(speedup, CopperCogwheelScenes::largeSpeedup)
                .addStoryBoard(large, CopperCogwheelScenes::largeRelay, AllCreatePonderTags.KINETIC_RELAYS)
                .addStoryBoard(cogEncasing, CopperCogwheelScenes::largeEncasing)
                .addStoryBoard(speedup, CopperCogwheelScenes::largeWeathering)
                .addStoryBoard(speedup, CopperCogwheelScenes::largeMaintenance);
    }
}
