package team.misakanet.createrustycopper.client.ponder;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.misakanet.createrustycopper.content.CopperVariant;
import team.misakanet.createrustycopper.registry.ModBlocks;

public final class CopperCogwheelScenes {
    private CopperCogwheelScenes() {
    }

    // Adapted from Create 6.0.10's KineticsScenes cogwheel storyboards (MIT).
    public static void smallRelay(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("copper_cogwheel", "Relaying rotation with Copper Cogwheels");
        scene.configureBasePlate(0, 0, 5);
        replaceCogwheels(scene, util, false);
        BlockPos gauge = util.grid().at(4, 1, 1);
        Selection gaugeSelect = util.select().position(gauge);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(gaugeSelect, Direction.UP);
        scene.world().setKineticSpeed(gaugeSelect, 0);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(1, 1, 3, 1, 1, 5), Direction.DOWN);
        scene.idle(10);

        for (int i = 1; i <= 4; i++) {
            scene.idle(5);
            if (i == 2) {
                scene.world().showSection(util.select().position(0, 1, 2), Direction.DOWN);
            }
            scene.world().showSection(util.select().position(i, 1, 2), Direction.DOWN);
        }

        scene.world().setKineticSpeed(gaugeSelect, 64);
        scene.effects().indicateSuccess(gauge);
        scene.idle(10);
        scene.overlay().showText(60)
                .text("Copper Cogwheels relay rotation to adjacent cogwheels.")
                .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 2), Direction.EAST));

        scene.idle(60);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 2, 1, 1), Direction.SOUTH);
        scene.idle(10);
        scene.effects().rotationDirectionIndicator(util.grid().at(1, 1, 1));
        scene.effects().rotationDirectionIndicator(util.grid().at(2, 1, 1));
        scene.idle(20);
        scene.overlay().showText(100)
                .text("Neighbouring shafts connected like this rotate in opposite directions.")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 2), Direction.NORTH));
        scene.idle(70);
    }

    private static void speedup(SceneBuilder builder, SceneBuildingUtil util, boolean large) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(sceneId(large) + "_speedup", "Gearshifting with Copper Cogwheels");
        scene.configureBasePlate(0, 0, 5);
        replaceCogwheels(scene, util, large);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(5, 1, 2, 4, 1, 2), Direction.DOWN);
        scene.idle(10);

        BlockPos lowerCog = util.grid().at(3, 1, 2);
        BlockPos upperCog = util.grid().at(3, 2, 3);
        Block largeCog = large ? cogwheel(CopperVariant.COPPER, true) : AllBlocks.LARGE_COGWHEEL.get();
        Block smallCog = large ? AllBlocks.COGWHEEL.get() : cogwheel(CopperVariant.COPPER, false);
        BlockState largeCogState = largeCog.defaultBlockState().setValue(CogWheelBlock.AXIS, Axis.X);
        BlockState smallCogState = smallCog.defaultBlockState().setValue(CogWheelBlock.AXIS, Axis.X);

        scene.world().setBlock(lowerCog, largeCogState, false);
        scene.world().setBlock(upperCog, smallCogState, false);
        scene.world().setKineticSpeed(util.select().position(lowerCog), 32);
        BlockPos upperShaftEnd = upperCog.west(3);
        BlockPos lowerShaftEnd = lowerCog.west(3);

        scene.world().setKineticSpeed(util.select().fromTo(upperCog, upperShaftEnd), -64);
        scene.world().showSection(util.select().fromTo(lowerCog, upperCog), Direction.EAST);
        scene.overlay().showText(60)
                .text("Large and Small cogs can be connected diagonally")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(upperCog, Direction.WEST));
        scene.idle(80);

        Selection gaugesSelect = util.select().fromTo(0, 1, 2, 2, 2, 3);
        scene.world().showSection(gaugesSelect, Direction.DOWN);
        scene.overlay().showText(80)
                .text("Shifting from large to small cogs, the conveyed speed will be doubled")
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 3), Direction.NORTH));
        scene.idle(30);
        scene.effects().rotationSpeedIndicator(upperCog);
        scene.idle(60);

        scene.overlay().showText(30)
                .sharedText(ResourceLocation.fromNamespaceAndPath("create", "rpm32"))
                .colored(PonderPalette.FAST)
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(upperShaftEnd, Direction.WEST));
        scene.idle(5);
        scene.overlay().showText(30)
                .sharedText(ResourceLocation.fromNamespaceAndPath("create", "rpm16"))
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(lowerShaftEnd, Direction.WEST));
        scene.idle(45);

        scene.world().setKineticSpeed(util.select().fromTo(lowerCog, upperShaftEnd), 0);
        ElementLink<WorldSectionElement> cogs =
            scene.world().makeSectionIndependent(util.select().fromTo(lowerCog, upperCog));
        scene.world().moveSection(cogs, util.vector().of(0, 1, 0), 5);
        scene.idle(5);
        scene.world().rotateSection(cogs, 180, 0, 0, 10);
        scene.idle(10);
        scene.world().setBlock(lowerCog, smallCogState, false);
        scene.world().setBlock(upperCog, largeCogState, false);
        scene.world().rotateSection(cogs, 180, 0, 0, 0);
        scene.world().moveSection(cogs, util.vector().of(0, -1, 0), 5);
        scene.idle(5);

        scene.world().setKineticSpeed(util.select().fromTo(lowerCog, lowerShaftEnd), 32);
        scene.world().setKineticSpeed(util.select().fromTo(upperCog, upperShaftEnd), -16);

        scene.overlay().showText(80)
                .text("Shifting the opposite way, the conveyed speed will be halved")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 3), Direction.NORTH));
        scene.idle(10);
        scene.effects().rotationSpeedIndicator(upperCog);
        scene.idle(80);

        scene.overlay().showText(60)
                .sharedText(ResourceLocation.fromNamespaceAndPath("create", "rpm8"))
                .colored(PonderPalette.SLOW)
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(upperShaftEnd, Direction.WEST));
        scene.idle(5);
        scene.overlay().showText(60)
                .sharedText(ResourceLocation.fromNamespaceAndPath("create", "rpm16"))
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(lowerShaftEnd, Direction.WEST));
        scene.idle(40);
    }

    public static void largeRelay(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("large_copper_cogwheel", "Relaying rotation with Large Copper Cogwheels");
        scene.configureBasePlate(1, 1, 5);
        replaceCogwheels(scene, util, true);
        scene.world().setBlock(util.grid().at(4, 2, 3), cogwheel(CopperVariant.COPPER, true).defaultBlockState()
                .setValue(CogWheelBlock.AXIS, Axis.X), false);

        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(3, 2, 4), Direction.NORTH);

        for (int i = 3; i >= 1; i--) {
            scene.idle(5);
            if (i == 3) {
                scene.world().showSection(util.select().position(3, 2, 5), Direction.DOWN);
            }
            scene.world().showSection(util.select().position(3, 2, i), Direction.DOWN);
        }

        scene.overlay().showText(70)
                .text("Large Copper Cogwheels connect to other large cogwheels at right angles.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(3, 1, 4));
        scene.idle(70);
        scene.world().hideSection(util.select().fromTo(3, 2, 1, 3, 2, 5), Direction.SOUTH);

        scene.idle(15);
        scene.world().modifyBlock(util.grid().at(3, 2, 3), s -> s.setValue(CogWheelBlock.AXIS, Axis.X), false);
        scene.world().setKineticSpeed(util.select().fromTo(1, 2, 3, 5, 2, 3), 16);
        scene.world().showSection(util.select().position(4, 2, 3), Direction.WEST);

        for (int i = 3; i >= 1; i--) {
            scene.idle(5);
            if (i == 3) {
                scene.world().showSection(util.select().position(5, 2, 3), Direction.DOWN);
            }
            scene.world().showSection(util.select().position(i, 2, 3), Direction.DOWN);
        }

        scene.idle(5);
        scene.overlay().showText(90)
                .text("This relays rotation between different axes without changing speed.")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 3), Direction.WEST));
        scene.effects().rotationSpeedIndicator(util.grid().at(3, 1, 3));
        scene.effects().rotationSpeedIndicator(util.grid().at(4, 2, 3));
        scene.idle(60);
    }

    private static void encasing(SceneBuilder builder, SceneBuildingUtil util, boolean large) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(sceneId(large) + "_casing", "Encasing Copper Cogwheels");
        scene.configureBasePlate(0, 0, 5);
        replaceCogwheels(scene, util, large);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection large1 = util.select().position(4, 1, 3);
        Selection small1 = util.select().fromTo(3, 1, 2, 3, 2, 2);
        Selection small2 = util.select().position(2, 1, 2);
        Selection large2 = util.select().fromTo(1, 1, 3, 1, 1, 4);
        Selection shaft2 = util.select().position(2, 2, 2);

        scene.world().setKineticSpeed(shaft2, 0);
        scene.idle(10);

        scene.world().showSection(large1, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(small1, Direction.DOWN);
        scene.world().showSection(small2, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(large2, Direction.EAST);
        scene.idle(20);

        Block andesiteEncased = large ? AllBlocks.ANDESITE_ENCASED_COGWHEEL.get()
                : ModBlocks.of(CopperVariant.COPPER).andesiteCogwheel().get();
        ItemStack andesiteCasingItem = AllBlocks.ANDESITE_CASING.asStack();

        scene.overlay().showControls(util.vector().topOf(3, 0, 2), Pointing.UP, 100).rightClick()
                .withItem(andesiteCasingItem);
        scene.idle(7);
        scene.world().setBlocks(util.select().position(3, 1, 2), andesiteEncased.defaultBlockState()
                .setValue(EncasedCogwheelBlock.AXIS, Axis.Y)
                .setValue(EncasedCogwheelBlock.TOP_SHAFT, true), true);
        scene.world().setKineticSpeed(util.select().position(3, 1, 2), -32);
        scene.idle(15);

        scene.overlay().showControls(util.vector().topOf(2, 1, 2), Pointing.DOWN, 30).rightClick()
                .withItem(andesiteCasingItem);
        scene.idle(7);
        scene.world().setBlocks(small2, andesiteEncased.defaultBlockState()
                .setValue(EncasedCogwheelBlock.AXIS, Axis.Y), true);
        scene.world().setKineticSpeed(small2, 32);
        scene.idle(15);

        Block brassEncased = large ? ModBlocks.of(CopperVariant.COPPER).brassLargeCogwheel().get()
                : AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL.get();
        ItemStack brassCasingItem = AllBlocks.BRASS_CASING.asStack();

        scene.overlay().showControls(util.vector().topOf(1, 0, 3), Pointing.UP, 60).rightClick()
                .withItem(brassCasingItem);
        scene.idle(7);
        scene.world().setBlocks(util.select().position(1, 1, 3), brassEncased.defaultBlockState()
                .setValue(EncasedCogwheelBlock.AXIS, Axis.Y), true);
        scene.world().setKineticSpeed(util.select().position(1, 1, 3), -16);

        scene.idle(10);
        scene.overlay().showText(70)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Brass or Andesite Casing can be used to decorate Copper Cogwheels")
                .pointAt(util.vector().topOf(1, 1, 3));
        scene.idle(80);

        ElementLink<WorldSectionElement> shaftLink = scene.world().showIndependentSection(shaft2, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .text("Components added after encasing will not connect to the shaft outputs")
                .pointAt(util.vector().centerOf(2, 2, 2));
        scene.idle(90);

        scene.world().moveSection(shaftLink, new Vec3(0, .5f, 0), 10);
        scene.idle(10);

        scene.addKeyframe();
        Vec3 wrenchHere = util.vector().topOf(2, 1, 2)
                .add(.25, 0, -.25);
        scene.overlay().showControls(wrenchHere, Pointing.RIGHT, 25).rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(7);
        scene.world().cycleBlockProperty(util.grid().at(2, 1, 2), EncasedCogwheelBlock.TOP_SHAFT);
        scene.idle(15);
        scene.world().moveSection(shaftLink, new Vec3(0, -.5f, 0), 10);
        scene.idle(10);
        scene.world().setKineticSpeed(shaft2, 32);
        scene.effects().rotationDirectionIndicator(util.grid().at(2, 2, 2));
        scene.idle(20);

        scene.overlay().showText(90)
                .placeNearTarget()
                .colored(PonderPalette.GREEN)
                .text("The Wrench can be used to toggle connections")
                .pointAt(wrenchHere.add(-.5, 0, .5));
        scene.idle(40);

        scene.overlay().showControls(wrenchHere, Pointing.RIGHT, 25).rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(7);
        scene.world().cycleBlockProperty(util.grid().at(2, 1, 2), EncasedCogwheelBlock.TOP_SHAFT);
        scene.world().setKineticSpeed(shaft2, 0);
    }

    public static void smallSpeedup(SceneBuilder builder, SceneBuildingUtil util) {
        speedup(builder, util, false);
    }

    public static void largeSpeedup(SceneBuilder builder, SceneBuildingUtil util) {
        speedup(builder, util, true);
    }

    public static void smallEncasing(SceneBuilder builder, SceneBuildingUtil util) {
        encasing(builder, util, false);
    }

    public static void largeEncasing(SceneBuilder builder, SceneBuildingUtil util) {
        encasing(builder, util, true);
    }

    public static void smallWeathering(SceneBuilder builder, SceneBuildingUtil util) {
        weathering(builder, util, false);
    }

    public static void largeWeathering(SceneBuilder builder, SceneBuildingUtil util) {
        weathering(builder, util, true);
    }

    public static void smallMaintenance(SceneBuilder builder, SceneBuildingUtil util) {
        maintenance(builder, util, false);
    }

    public static void largeMaintenance(SceneBuilder builder, SceneBuildingUtil util) {
        maintenance(builder, util, true);
    }

    private static void weathering(SceneBuilder builder, SceneBuildingUtil util, boolean large) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(sceneId(large) + "_weathering", "Copper Cogwheel oxidation and stress");
        prepareCareScene(scene, util, large, CopperVariant.COPPER);

        BlockPos target = util.grid().at(3, 1, 2);
        Selection cog = util.select().position(target);
        Selection network = util.select().fromTo(0, 1, 2, 5, 2, 3);
        scene.overlay().showOutlineWithText(cog, 90)
                .text("Unwaxed Copper Cogwheels oxidize over time through four stages.")
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        scene.idle(100);

        setCogwheel(scene, util, target, large, CopperVariant.EXPOSED);
        setStress(scene, util, 0.35f);
        scene.overlay().showOutlineWithText(cog, 140)
                .text("By default, each later oxidation stage consumes more stress capacity.")
                .colored(PonderPalette.MEDIUM)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        scene.idle(70);
        setCogwheel(scene, util, target, large, CopperVariant.WEATHERED);
        setStress(scene, util, 0.8f);
        scene.idle(80);

        setCogwheel(scene, util, target, large, CopperVariant.OXIDIZED);
        scene.world().setKineticSpeed(network, 0);
        setStress(scene, util, 1.125f);
        scene.effects().indicateRedstone(util.grid().at(1, 1, 2));
        scene.overlay().showOutlineWithText(network, 110)
                .text("One fully oxidized cogwheel can overstress the connected network and stop all its machinery.")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        scene.idle(120);

        scene.overlay().showText(100)
                .text("The result depends on the server's rust penalty settings and the network's stress capacity.")
                .pointAt(util.vector().topOf(1, 1, 2))
                .placeNearTarget();
        scene.idle(110);

        scene.overlay().showControls(util.vector().topOf(target), Pointing.DOWN, 50)
                .rightClick().withItem(AllBlocks.ANDESITE_CASING.asStack());
        scene.idle(7);
        var blocks = ModBlocks.of(CopperVariant.OXIDIZED);
        Block encased = large ? blocks.andesiteLargeCogwheel().get() : blocks.andesiteCogwheel().get();
        scene.world().setBlock(target, encased.defaultBlockState()
                .setValue(EncasedCogwheelBlock.AXIS, Axis.X)
                .setValue(EncasedCogwheelBlock.TOP_SHAFT, true)
                .setValue(EncasedCogwheelBlock.BOTTOM_SHAFT, true), true);
        scene.overlay().showText(100)
                .text("Casing does not prevent oxidation or remove its stress cost.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        scene.idle(110);
        scene.markAsFinished();
    }

    private static void maintenance(SceneBuilder builder, SceneBuildingUtil util, boolean large) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(sceneId(large) + "_maintenance", "Scraping and waxing Copper Cogwheels");
        prepareCareScene(scene, util, large, CopperVariant.WEATHERED);

        BlockPos target = util.grid().at(3, 1, 2);
        Selection cog = util.select().position(target);
        scene.overlay().showOutlineWithText(cog, 100)
                .text("Right-click with an Axe to scrape off one oxidation stage at a time.")
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnCogwheel(scene, util, target, large, new ItemStack(Items.IRON_AXE), CopperVariant.EXPOSED,
                ParticleTypes.SCRAPE);
        setStress(scene, util, 0.35f);
        scene.idle(105);

        scene.overlay().showText(100)
                .text("Honeycomb waxes a cogwheel at its current stage and prevents further oxidation.")
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnCogwheel(scene, util, target, large, new ItemStack(Items.HONEYCOMB), CopperVariant.WAXED_EXPOSED,
                ParticleTypes.WAX_ON);
        scene.idle(105);

        scene.overlay().showOutlineWithText(cog, 90)
                .text("Wax preserves existing oxidation and its stress cost.")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(100)
                .text("On a waxed cogwheel, the first Axe use removes only the wax.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnCogwheel(scene, util, target, large, new ItemStack(Items.IRON_AXE), CopperVariant.EXPOSED,
                ParticleTypes.WAX_OFF);
        scene.idle(105);

        scene.overlay().showText(90)
                .text("Use the Axe again to remove the next oxidation stage.")
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnCogwheel(scene, util, target, large, new ItemStack(Items.IRON_AXE), CopperVariant.COPPER,
                ParticleTypes.SCRAPE);
        setStress(scene, util, 0);
        scene.effects().indicateSuccess(target);
        scene.idle(95);

        scene.overlay().showText(110)
                .text("Scrape first, then wax to protect clean copper without a rust penalty.")
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnCogwheel(scene, util, target, large, new ItemStack(Items.HONEYCOMB), CopperVariant.WAXED_COPPER,
                ParticleTypes.WAX_ON);
        scene.idle(115);
        scene.markAsFinished();
    }

    private static void prepareCareScene(CreateSceneBuilder scene, SceneBuildingUtil util, boolean large,
            CopperVariant variant) {
        scene.configureBasePlate(0, 0, 5);
        replaceCogwheels(scene, util, large);
        BlockPos target = util.grid().at(3, 1, 2);
        scene.world().setBlock(target, cogwheel(variant, large).defaultBlockState()
                .setValue(CogWheelBlock.AXIS, Axis.X), false);
        Block otherCogwheel = large ? AllBlocks.COGWHEEL.get() : AllBlocks.LARGE_COGWHEEL.get();
        scene.world().setBlock(util.grid().at(3, 2, 3), otherCogwheel.defaultBlockState()
                .setValue(CogWheelBlock.AXIS, Axis.X), false);
        scene.world().setBlock(util.grid().at(1, 1, 2), AllBlocks.STRESSOMETER.getDefaultState()
                .setValue(GaugeBlock.FACING, Direction.UP)
                .setValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE, true), false);
        scene.world().setKineticSpeed(util.select().fromTo(0, 1, 2, 5, 1, 2), 32);
        scene.world().setKineticSpeed(util.select().fromTo(0, 2, 3, 3, 2, 3), large ? -64 : -16);
        setStress(scene, util, variant == CopperVariant.WEATHERED ? 0.8f : 0);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(0, 1, 2, 5, 2, 3), Direction.DOWN);
        scene.idle(20);
    }

    private static void replaceCogwheels(CreateSceneBuilder scene, SceneBuildingUtil util, boolean large) {
        Map<BlockPos, Float> speeds = new HashMap<>();
        Selection all = util.select().everywhere();
        // Preserve the schematic's signed speeds when replacements create new block entities.
        scene.world().modifyBlockEntityNBT(all, KineticBlockEntity.class,
                nbt -> speeds.put(new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")), nbt.getFloat("Speed")));
        Block original = large ? AllBlocks.LARGE_COGWHEEL.get() : AllBlocks.COGWHEEL.get();
        Block copper = cogwheel(CopperVariant.COPPER, large);
        scene.world().modifyBlocks(all, state -> state.is(original) ? copper.withPropertiesOf(state) : state, false);
        scene.world().modifyBlockEntityNBT(all, KineticBlockEntity.class, nbt -> {
            Float speed = speeds.get(new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")));
            if (speed != null) {
                nbt.putFloat("Speed", speed);
            }
        });
    }

    private static void setCogwheel(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos target, boolean large,
            CopperVariant variant) {
        scene.world().modifyBlock(target, state -> cogwheel(variant, large).withPropertiesOf(state), false);
        scene.world().setKineticSpeed(util.select().position(target), 32);
    }

    private static void setStress(CreateSceneBuilder scene, SceneBuildingUtil util, float value) {
        scene.world().modifyBlockEntityNBT(util.select().position(1, 1, 2),
                StressGaugeBlockEntity.class, nbt -> {
                    nbt.putFloat("Value", value);
                    nbt.putInt("Color", value >= 1 ? 0xFF0000 : value >= 0.5f ? 0xFFAA00 : 0x00FF00);
                });
    }

    private static void useOnCogwheel(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos target, boolean large,
            ItemStack item, CopperVariant variant, ParticleOptions particles) {
        scene.overlay().showControls(util.vector().blockSurface(target, Direction.NORTH), Pointing.RIGHT, 50)
                .rightClick().withItem(item);
        scene.idle(7);
        setCogwheel(scene, util, target, large, variant);
        scene.effects().emitParticles(util.vector().centerOf(target),
                scene.effects().particleEmitterWithinBlockSpace(particles, Vec3.ZERO), 12, 1);
    }

    private static Block cogwheel(CopperVariant variant, boolean large) {
        var blocks = ModBlocks.of(variant);
        return large ? blocks.largeCogwheel().get() : blocks.cogwheel().get();
    }

    private static String sceneId(boolean large) {
        return large ? "large_copper_cogwheel" : "copper_cogwheel";
    }
}
