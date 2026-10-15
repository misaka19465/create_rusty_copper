package team.misakanet.createrustycopper.client.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.misakanet.createrustycopper.content.CopperVariant;
import team.misakanet.createrustycopper.registry.ModBlocks;

public final class CopperShaftScenes {
    private CopperShaftScenes() {
    }

    // Adapted from Create 6.0.10's KineticsScenes.shaftAsRelay and shaftsCanBeEncased (MIT).
    public static void relay(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("copper_shaft", "Relaying rotation with Copper Shafts");
        scene.configureBasePlate(0, 0, 5);
        replaceShafts(scene, util, 64);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos gaugePos = util.grid().at(0, 1, 2);
        Selection gauge = util.select().position(gaugePos);
        scene.world().showSection(gauge, Direction.UP);
        scene.world().setKineticSpeed(gauge, 0);
        scene.idle(5);
        scene.world().showSection(util.select().position(5, 1, 2), Direction.DOWN);
        scene.idle(10);

        for (int i = 4; i >= 1; i--) {
            if (i == 2) {
                scene.rotateCameraY(70);
            }
            scene.idle(5);
            scene.world().showSection(util.select().position(i, 1, 2), Direction.DOWN);
        }

        scene.world().setKineticSpeed(gauge, 64);
        scene.effects().indicateSuccess(gaugePos);
        scene.idle(10);
        scene.overlay().showText(1000)
                .placeNearTarget()
                .text("Copper Shafts relay rotation in a straight line.")
                .pointAt(util.vector().of(3, 1.5, 2.5));
        scene.idle(20);
        scene.markAsFinished();
    }

    public static void encasing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("copper_shaft_casing", "Encasing Copper Shafts");
        scene.configureBasePlate(0, 0, 5);
        replaceShafts(scene, util, 32);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection assembly = util.select().fromTo(0, 1, 2, 5, 1, 4);
        Selection andesite = util.select().position(3, 1, 2);
        Selection brass = util.select().position(1, 1, 2);
        var copper = ModBlocks.of(CopperVariant.COPPER);
        scene.world().showSection(assembly, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showControls(util.vector().topOf(3, 1, 2), Pointing.DOWN, 60)
                .rightClick().withItem(AllBlocks.ANDESITE_CASING.asStack());
        scene.idle(7);
        scene.world().setBlocks(andesite, copper.andesiteShaft().get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Axis.X), true);
        scene.world().setKineticSpeed(assembly, 32);
        scene.idle(10);

        scene.overlay().showControls(util.vector().topOf(1, 0, 2), Pointing.UP, 60)
                .rightClick().withItem(AllBlocks.BRASS_CASING.asStack());
        scene.idle(7);
        scene.world().setBlocks(brass, copper.brassShaft().get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Axis.X), true);
        scene.world().setKineticSpeed(assembly, 32);
        scene.idle(10);
        scene.overlay().showText(100)
                .placeNearTarget()
                .text("Brass or Andesite Casing can be used to decorate Copper Shafts.")
                .pointAt(util.vector().topOf(1, 1, 2));
        scene.idle(70);
    }

    public static void weathering(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("copper_shaft_weathering", "Oxidation and stress");
        scene.configureBasePlate(0, 0, 5);
        replaceShafts(scene, util, 32);

        BlockPos gaugePos = util.grid().at(0, 1, 2);
        Selection gauge = util.select().position(gaugePos);
        Selection shafts = util.select().fromTo(1, 1, 2, 4, 1, 2);
        Selection network = util.select().fromTo(0, 1, 2, 5, 1, 2);
        scene.world().setBlock(gaugePos, AllBlocks.STRESSOMETER.getDefaultState()
                .setValue(GaugeBlock.FACING, Direction.UP)
                .setValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE, true), false);
        scene.world().setKineticSpeed(network, 32);
        setStress(scene, gauge, 0);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(network, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showOutlineWithText(shafts, 90)
                .text("Unwaxed Copper Shafts oxidize over time through four stages.")
                .pointAt(util.vector().topOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(100);

        scene.world().setBlocks(shafts, shaft(CopperVariant.EXPOSED), false);
        scene.world().setKineticSpeed(shafts, 32);
        setStress(scene, gauge, 0.35f);
        scene.overlay().showOutlineWithText(shafts, 140)
                .text("By default, each later oxidation stage consumes more stress capacity.")
                .colored(PonderPalette.MEDIUM)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(70);
        scene.world().setBlocks(shafts, shaft(CopperVariant.WEATHERED), false);
        scene.world().setKineticSpeed(shafts, 32);
        setStress(scene, gauge, 0.8f);
        scene.idle(80);

        scene.world().setBlocks(shafts, shaft(CopperVariant.OXIDIZED), false);
        scene.world().setKineticSpeed(network, 0);
        setStress(scene, gauge, 1.125f);
        scene.effects().indicateRedstone(gaugePos);
        scene.overlay().showOutlineWithText(network, 110)
                .text("Fully oxidized shafts can overstress the whole connected network and stop it.")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(120);

        scene.overlay().showText(100)
                .text("The result depends on the server's rust penalty settings and the network's stress capacity.")
                .pointAt(util.vector().topOf(gaugePos))
                .placeNearTarget();
        scene.idle(110);

        scene.overlay().showControls(util.vector().topOf(3, 1, 2), Pointing.DOWN, 50)
                .rightClick().withItem(AllBlocks.ANDESITE_CASING.asStack());
        scene.idle(7);
        scene.world().setBlock(util.grid().at(3, 1, 2), ModBlocks.of(CopperVariant.OXIDIZED)
                .andesiteShaft().get().defaultBlockState().setValue(ShaftBlock.AXIS, Axis.X), true);
        scene.overlay().showText(100)
                .text("Casing does not prevent oxidation or remove its stress cost.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(110);
        scene.markAsFinished();
    }

    public static void maintenance(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("copper_shaft_maintenance", "Scraping and waxing Copper Shafts");
        scene.configureBasePlate(0, 0, 5);
        replaceShafts(scene, util, 32);

        BlockPos target = util.grid().at(2, 1, 2);
        Selection targetSelection = util.select().position(target);
        scene.world().setBlock(target, shaft(CopperVariant.WEATHERED), false);
        scene.world().setKineticSpeed(util.select().fromTo(0, 1, 2, 5, 1, 2), 32);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showOutlineWithText(targetSelection, 100)
                .text("Right-click with an Axe to scrape off one oxidation stage at a time.")
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnShaft(scene, util, target, new ItemStack(Items.IRON_AXE), CopperVariant.EXPOSED, ParticleTypes.SCRAPE);
        scene.idle(105);

        scene.overlay().showText(100)
                .text("Honeycomb waxes copper at its current stage and prevents further oxidation.")
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnShaft(scene, util, target, new ItemStack(Items.HONEYCOMB), CopperVariant.WAXED_EXPOSED,
                ParticleTypes.WAX_ON);
        scene.idle(105);

        scene.overlay().showOutlineWithText(targetSelection, 90)
                .text("Wax preserves existing oxidation and its stress cost.")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(100)
                .text("On waxed copper, the first Axe use removes only the wax.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnShaft(scene, util, target, new ItemStack(Items.IRON_AXE), CopperVariant.EXPOSED, ParticleTypes.WAX_OFF);
        scene.idle(105);

        scene.overlay().showText(90)
                .text("Use the Axe again to remove the next oxidation stage.")
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnShaft(scene, util, target, new ItemStack(Items.IRON_AXE), CopperVariant.COPPER, ParticleTypes.SCRAPE);
        scene.effects().indicateSuccess(target);
        scene.idle(95);

        scene.overlay().showText(110)
                .text("Scrape first, then wax to protect clean copper without a rust penalty.")
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        useOnShaft(scene, util, target, new ItemStack(Items.HONEYCOMB), CopperVariant.WAXED_COPPER, ParticleTypes.WAX_ON);
        scene.idle(115);
        scene.markAsFinished();
    }

    private static void replaceShafts(CreateSceneBuilder scene, SceneBuildingUtil util, float speed) {
        scene.world().modifyBlocks(util.select().everywhere(), state -> state.is(AllBlocks.SHAFT.get())
                ? shaft(CopperVariant.COPPER).getBlock().withPropertiesOf(state) : state, false);
        // Replaced block entities need their scripted rotation restored.
        scene.world().setKineticSpeed(util.select().fromTo(1, 1, 2, 4, 1, 2), speed);
        scene.world().setKineticSpeed(util.select().position(0, 1, 2), speed);
    }

    private static BlockState shaft(CopperVariant variant) {
        return ModBlocks.of(variant).shaft().get().defaultBlockState().setValue(ShaftBlock.AXIS, Axis.X);
    }

    private static void setStress(CreateSceneBuilder scene, Selection gauge, float value) {
        scene.world().modifyBlockEntityNBT(gauge, StressGaugeBlockEntity.class, nbt -> {
            nbt.putFloat("Value", value);
            nbt.putInt("Color", value >= 1 ? 0xFF0000 : value >= 0.5f ? 0xFFAA00 : 0x00FF00);
        });
    }

    private static void useOnShaft(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos target, ItemStack item,
            CopperVariant variant, ParticleOptions particles) {
        scene.overlay().showControls(util.vector().blockSurface(target, Direction.NORTH), Pointing.RIGHT, 50)
                .rightClick().withItem(item);
        scene.idle(7);
        scene.world().setBlock(target, shaft(variant), false);
        scene.world().setKineticSpeed(util.select().position(target), 32);
        scene.effects().emitParticles(util.vector().centerOf(target),
                scene.effects().particleEmitterWithinBlockSpace(particles, Vec3.ZERO), 12, 1);
    }
}
