package io.github.misaka19465.createrustycopper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import io.github.misaka19465.createrustycopper.config.ModServerConfig;
import io.github.misaka19465.createrustycopper.content.CopperVariant;
import io.github.misaka19465.createrustycopper.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(CreateRustyCopper.MOD_ID)
public final class CopperStressGameTests {
    private static final BlockPos POS = new BlockPos(3, 3, 3);
    private static final BlockPos OTHER = new BlockPos(8, 3, 3);
    private static SavedConfig previousConfig;
    private static int previousRandomTickSpeed;

    @BeforeBatch(batch = "copper_stress")
    public static void prepareStressTests(ServerLevel level) {
        prepare(level);
    }

    @AfterBatch(batch = "copper_stress")
    public static void restoreStressTests(ServerLevel level) {
        restore(level);
    }

    @BeforeBatch(batch = "copper_stress_reload")
    public static void prepareReloadTest(ServerLevel level) {
        prepare(level);
    }

    @AfterBatch(batch = "copper_stress_reload")
    public static void restoreReloadTest(ServerLevel level) {
        restore(level);
    }

    @BeforeBatch(batch = "copper_stress_persistence")
    public static void preparePersistenceTest(ServerLevel level) {
        prepare(level);
    }

    @AfterBatch(batch = "copper_stress_persistence")
    public static void restorePersistenceTest(ServerLevel level) {
        restore(level);
    }

    private static void prepare(ServerLevel level) {
        previousConfig = new SavedConfig(ModServerConfig.RUST_PENALTY_ENABLED.get(),
                ModServerConfig.EXPOSED_STRESS_IMPACT.get(), ModServerConfig.WEATHERED_STRESS_IMPACT.get(),
                ModServerConfig.OXIDIZED_STRESS_IMPACT.get());
        configure(true, 8, 32, 1.0E9);
        previousRandomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
    }

    private static void restore(ServerLevel level) {
        configure(previousConfig.enabled(), previousConfig.exposed(), previousConfig.weathered(), previousConfig.oxidized());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(previousRandomTickSpeed, level.getServer());
    }

    @GameTestGenerator
    public static Collection<TestFunction> tests() {
        List<TestFunction> tests = new ArrayList<>();
        tests.add(test("copper_stress", "stress_all_variants", CopperStressGameTests::impacts));
        tests.add(test("copper_stress", "stress_speed_ratios", CopperStressGameTests::speedRatios));
        for (int family = 0; family < ModBlocks.of(CopperVariant.COPPER).allBlocks().size(); family++) {
            int index = family;
            String name = BuiltInRegistries.BLOCK.getKey(block(CopperVariant.COPPER, family)).getPath();
            tests.add(test("copper_stress", "stress_oxidation_recovery_" + name, helper -> oxidationAndRecovery(helper, index)));
            tests.add(test("copper_stress", "stress_removal_recovery_" + name, helper -> removalAndRecovery(helper, index)));
        }
        tests.add(test("copper_stress_reload", "stress_live_configuration", CopperStressGameTests::liveConfiguration));
        tests.add(test("copper_stress_persistence", "stress_saved_configuration", CopperStressGameTests::savedConfiguration));
        return tests;
    }

    private static TestFunction test(String batch, String name, Consumer<GameTestHelper> test) {
        return new TestFunction(batch, CreateRustyCopper.MOD_ID + ":" + name,
                CreateRustyCopper.MOD_ID + ":empty", 160, 0, true, test);
    }

    private static void impacts(GameTestHelper helper) {
        for (CopperVariant variant : CopperVariant.values()) {
            float expected = switch (variant.weatherState()) {
                case UNAFFECTED -> 0;
                case EXPOSED -> 8;
                case WEATHERED -> 32;
                case OXIDIZED -> 1.0E9F;
            };
            for (Block block : ModBlocks.of(variant).allBlocks()) {
                helper.setBlock(POS, state(block));
                KineticBlockEntity kinetic = helper.getBlockEntity(POS);
                helper.assertTrue(kinetic.calculateStressApplied() == expected,
                        "All copper components must use their oxidation stage's stress impact, including waxed and encased parts");
                helper.assertTrue(BlockStressValues.getImpact(block) == expected,
                        "Create's stress API must report the same impact as the block entity");
            }
        }
        helper.assertTrue(BlockStressValues.getImpact(AllBlocks.SHAFT.get()) == 0,
                "Create's ordinary shafts must retain their original stress impact");
        helper.succeed();
    }

    private static void speedRatios(GameTestHelper helper) {
        helper.setBlock(POS.north(), motor());
        helper.setBlock(POS, state(block(CopperVariant.WEATHERED, 1)));
        BlockPos small = POS.offset(1, 1, 0);
        helper.setBlock(small, state(block(CopperVariant.EXPOSED, 0)));
        helper.runAfterDelay(20, () -> {
            KineticBlockEntity large = helper.getBlockEntity(POS);
            KineticBlockEntity cog = helper.getBlockEntity(small);
            helper.assertTrue(large.getSpeed() == 16 && cog.getSpeed() == -32,
                    "Stress penalties must retain Create's gearing and rotation direction");
            helper.assertTrue(large.getOrCreateNetwork().calculateStress() == 32 * 16 + 8 * 32,
                    "Each component's stress must use its own absolute theoretical RPM");
            helper.succeed();
        });
    }

    private static void oxidationAndRecovery(GameTestHelper helper, int family) {
        line(helper, POS, CopperVariant.EXPOSED, family);
        line(helper, OTHER, CopperVariant.COPPER, 2);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.runAfterDelay(20, () -> {
            KineticBlockEntity original = helper.getBlockEntity(POS);
            Long network = original.network;
            running(helper, POS, 16);
            impact(helper, POS, 8);
            oxidize(helper);
            helper.assertBlockPresent(block(CopperVariant.WEATHERED, family), POS);
            impact(helper, POS, 32);
            running(helper, POS, 16);
            oxidize(helper);
            helper.assertBlockPresent(block(CopperVariant.OXIDIZED, family), POS);
            running(helper, POS, 0);
            running(helper, OTHER, 16);
            helper.assertTrue(helper.getBlockEntity(POS) == original && network.equals(original.network),
                    "Oxidation must overload the existing network without replacing the block entity");
            helper.assertTrue(original.getTheoreticalSpeed() == 16,
                    "An overloaded network must retain theoretical speed to avoid zeroing its stress cost");

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB));
            helper.useBlock(POS, player, hit(helper));
            helper.assertBlockPresent(block(CopperVariant.WAXED_OXIDIZED, family), POS);
            running(helper, POS, 0);
            helper.runAfterDelay(10, () -> {
                running(helper, POS, 0);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
                helper.useBlock(POS, player, hit(helper));
                running(helper, POS, 0);
                helper.useBlock(POS, player, hit(helper));
                helper.assertBlockPresent(block(CopperVariant.WEATHERED, family), POS);
                impact(helper, POS, 32);
                running(helper, POS, 16);
                helper.useBlock(POS, player, hit(helper));
                impact(helper, POS, 8);
                helper.useBlock(POS, player, hit(helper));
                impact(helper, POS, 0);
                helper.runAfterDelay(10, () -> {
                    running(helper, POS, 16);
                    helper.assertTrue(helper.getBlockEntity(POS) == original && network.equals(original.network),
                            "Scraping must resume the same network");
                    helper.succeed();
                });
            });
        });
    }

    private static void removalAndRecovery(GameTestHelper helper, int family) {
        line(helper, POS, CopperVariant.WAXED_OXIDIZED, family);
        helper.runAfterDelay(20, () -> {
            KineticBlockEntity jammed = helper.getBlockEntity(POS);
            helper.assertTrue(jammed.hasNetwork() && jammed.isOverStressed(), "Placed oxidized copper must overload its network");
            running(helper, POS, 0);
            helper.setBlock(POS, state(AllBlocks.SHAFT.get()));
            helper.runAfterDelay(20, () -> {
                running(helper, POS, 16);
                helper.assertTrue(jammed.isRemoved(), "Replacement must remove the oxidized block entity and its stress cost");
                helper.succeed();
            });
        });
    }

    private static void liveConfiguration(GameTestHelper helper) {
        line(helper, POS, CopperVariant.OXIDIZED, 2);
        line(helper, OTHER, CopperVariant.WEATHERED, 5);
        helper.runAfterDelay(20, () -> {
            running(helper, POS, 0);
            running(helper, OTHER, 16);
            ModServerConfig.RUST_PENALTY_ENABLED.set(false);
            // A goggle inspection can recalculate lastStressApplied before the next tick.
            KineticBlockEntity kinetic = helper.getBlockEntity(POS);
            helper.assertTrue(kinetic.calculateStressApplied() == 0, "The disable switch must affect stress queries immediately");
            helper.runAfterDelay(5, () -> {
                running(helper, POS, 16);
                running(helper, OTHER, 16);
                impact(helper, POS, 0);
                configure(true, 0.5, 2.5, 8);
                helper.runAfterDelay(5, () -> {
                    impact(helper, POS, 8);
                    impact(helper, OTHER, 2.5F);
                    running(helper, POS, 16);
                    ModServerConfig.OXIDIZED_STRESS_IMPACT.set(1.0E9);
                    ModServerConfig.WEATHERED_STRESS_IMPACT.set(1.0E9);
                    helper.runAfterDelay(5, () -> {
                        running(helper, POS, 0);
                        running(helper, OTHER, 0);
                        configure(true, 0, 0, 0);
                        helper.runAfterDelay(5, () -> {
                            running(helper, POS, 16);
                            running(helper, OTHER, 16);
                            impact(helper, POS, 0);
                            impact(helper, OTHER, 0);
                            helper.succeed();
                        });
                    });
                });
            });
        });
    }

    private static void line(GameTestHelper helper, BlockPos pos, CopperVariant variant, int family) {
        helper.setBlock(pos.north(), motor());
        helper.setBlock(pos, state(block(variant, family)));
        helper.setBlock(pos.south(), drill());
    }

    private static void savedConfiguration(GameTestHelper helper) {
        line(helper, POS, CopperVariant.OXIDIZED, 2);
        line(helper, OTHER, CopperVariant.COPPER, 2);
        helper.runAfterDelay(20, () -> {
            running(helper, POS, 0);
            reloadLine(helper, List.of(POS.north(), POS, POS.south()), () -> ModServerConfig.RUST_PENALTY_ENABLED.set(false));
            helper.runAfterDelay(10, () -> {
                impact(helper, POS, 0);
                running(helper, POS, 16);
                running(helper, OTHER, 16);
                reloadLine(helper, List.of(POS.south(), POS, POS.north()), () -> configure(true, 8, 32, 1.0E9));
                helper.runAfterDelay(10, () -> {
                    impact(helper, POS, 1.0E9F);
                    running(helper, POS, 0);
                    running(helper, OTHER, 16);
                    reloadLine(helper, List.of(POS.north(), POS, POS.south()), () -> ModServerConfig.OXIDIZED_STRESS_IMPACT.set(8.0));
                    helper.runAfterDelay(10, () -> {
                        impact(helper, POS, 8);
                        running(helper, POS, 16);
                        running(helper, OTHER, 16);
                        helper.succeed();
                    });
                });
            });
        });
    }

    private static void reloadLine(GameTestHelper helper, List<BlockPos> positions, Runnable changeConfiguration) {
        ServerLevel level = helper.getLevel();
        List<KineticBlockEntity> originals = positions.stream()
                .map(pos -> (KineticBlockEntity) helper.getBlockEntity(pos)).toList();
        List<CompoundTag> saved = originals.stream().map(kinetic -> kinetic.saveWithFullMetadata(level.registryAccess())).toList();
        var network = originals.getFirst().getOrCreateNetwork();
        helper.assertTrue(network.members.size() == 3, "Only the saved test network may be reset");
        for (KineticBlockEntity kinetic : originals) {
            kinetic.onChunkUnloaded();
            level.removeBlockEntity(kinetic.getBlockPos());
            network.remove(kinetic);
        }
        changeConfiguration.run();
        for (int index = 0; index < originals.size(); index++) {
            KineticBlockEntity original = originals.get(index);
            BlockEntity loaded = BlockEntity.loadStatic(original.getBlockPos(), original.getBlockState(), saved.get(index), level.registryAccess());
            helper.assertTrue(loaded instanceof KineticBlockEntity, "Saved copper networks must deserialize their block entities");
            // Restore the settled topology so source propagation cannot mask stale saved stress totals.
            ((KineticBlockEntity) loaded).updateSpeed = false;
            level.setBlockEntity(loaded);
        }
    }

    private static void running(GameTestHelper helper, BlockPos pos, float expected) {
        for (BlockPos member : List.of(pos.north(), pos, pos.south())) {
            KineticBlockEntity kinetic = helper.getBlockEntity(member);
            helper.assertTrue(kinetic.getSpeed() == expected,
                    "Expected " + expected + " RPM throughout the network, including the motor and drill; got "
                            + kinetic.getSpeed() + " at " + member);
        }
    }

    private static void impact(GameTestHelper helper, BlockPos pos, float expected) {
        KineticBlockEntity kinetic = helper.getBlockEntity(pos);
        helper.assertTrue(kinetic.calculateStressApplied() == expected
                        && kinetic.getOrCreateNetwork().members.get(kinetic) == expected,
                "The network must use the current stage and configuration's stress impact");
    }

    private static void oxidize(GameTestHelper helper) {
        RandomSource random = new LegacyRandomSource(0) {
            @Override
            public float nextFloat() {
                return 0;
            }
        };
        helper.getBlockState(POS).randomTick(helper.getLevel(), helper.absolutePos(POS), random);
    }

    private static Block block(CopperVariant variant, int family) {
        return ModBlocks.of(variant).allBlocks().get(family);
    }

    private static BlockState state(Block block) {
        BlockState state = block.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
        if (state.hasProperty(EncasedCogwheelBlock.TOP_SHAFT)) {
            state = state.setValue(EncasedCogwheelBlock.TOP_SHAFT, true).setValue(EncasedCogwheelBlock.BOTTOM_SHAFT, true);
        }
        return state;
    }

    private static BlockState motor() {
        return AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(CreativeMotorBlock.FACING, Direction.SOUTH);
    }

    private static BlockState drill() {
        return AllBlocks.MECHANICAL_DRILL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.SOUTH);
    }

    private static BlockHitResult hit(GameTestHelper helper) {
        BlockPos absolute = helper.absolutePos(POS);
        return new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
    }

    private static void configure(boolean enabled, double exposed, double weathered, double oxidized) {
        ModServerConfig.RUST_PENALTY_ENABLED.set(enabled);
        ModServerConfig.EXPOSED_STRESS_IMPACT.set(exposed);
        ModServerConfig.WEATHERED_STRESS_IMPACT.set(weathered);
        ModServerConfig.OXIDIZED_STRESS_IMPACT.set(oxidized);
    }

    private record SavedConfig(boolean enabled, double exposed, double weathered, double oxidized) {
    }
}
