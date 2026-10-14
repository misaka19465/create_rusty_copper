package team.misakanet.createrustycopper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import team.misakanet.createrustycopper.content.CopperVariant;
import team.misakanet.createrustycopper.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(CreateRustyCopper.MOD_ID)
public final class CopperWeatheringGameTests {
    private static final BlockPos POS = new BlockPos(5, 3, 5);
    private static final CopperVariant[] UNWAXED = {
            CopperVariant.COPPER, CopperVariant.EXPOSED, CopperVariant.WEATHERED, CopperVariant.OXIDIZED
    };
    private static final CopperVariant[] WAXED = {
            CopperVariant.WAXED_COPPER, CopperVariant.WAXED_EXPOSED,
            CopperVariant.WAXED_WEATHERED, CopperVariant.WAXED_OXIDIZED
    };
    private static int previousRandomTickSpeed;

    @BeforeBatch(batch = "copper_weathering")
    public static void disableUncontrolledRandomTicks(ServerLevel level) {
        previousRandomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
    }

    @AfterBatch(batch = "copper_weathering")
    public static void restoreRandomTicks(ServerLevel level) {
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(previousRandomTickSpeed, level.getServer());
    }

    @GameTestGenerator
    public static Collection<TestFunction> tests() {
        List<TestFunction> tests = new ArrayList<>();
        for (int family = 0; family < ModBlocks.of(CopperVariant.COPPER).allBlocks().size(); family++) {
            int index = family;
            String name = BuiltInRegistries.BLOCK.getKey(block(CopperVariant.COPPER, family)).getPath();
            tests.add(test("items_" + name, helper -> itemTransitions(helper, index)));
            tests.add(test("oxidation_" + name, helper -> oxidation(helper, index)));
            tests.add(test("kinetics_" + name, helper -> kinetics(helper, index)));
        }
        tests.add(test("vanilla_probability", CopperWeatheringGameTests::probability));
        tests.add(test("vanilla_neighbors", CopperWeatheringGameTests::neighbors));
        tests.add(test("shield_use_intent", CopperWeatheringGameTests::shield));
        tests.add(test("component_replacement", CopperWeatheringGameTests::componentReplacement));
        return tests;
    }

    private static TestFunction test(String name, Consumer<GameTestHelper> test) {
        return new TestFunction("copper_weathering", CreateRustyCopper.MOD_ID + ":" + name,
                CreateRustyCopper.MOD_ID + ":empty", 100, 0, true, test);
    }

    private static void itemTransitions(GameTestHelper helper, int family) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int stage = 0; stage < UNWAXED.length; stage++) {
            BlockState initial = state(UNWAXED[stage], family);
            helper.setBlock(POS, initial);
            attachBracket(helper, player);
            BlockEntity original = helper.getBlockEntity(POS);

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
            helper.useBlock(POS, player, hit(helper));
            assertState(helper, block(WAXED[stage], family).withPropertiesOf(initial));
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "Waxing must consume one honeycomb");
            assertPreserved(helper, original);
            helper.assertTrue(useItem(helper, player) == InteractionResult.PASS, "Already waxed copper must reject honeycomb");
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "Rejected waxing must not consume honeycomb");

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
            helper.useBlock(POS, player, hit(helper));
            assertState(helper, initial);
            helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "Wax removal must damage the axe once");
            assertPreserved(helper, original);

            int uses = 1;
            for (int previous = stage - 1; previous >= 0; previous--) {
                helper.useBlock(POS, player, hit(helper));
                assertState(helper, block(UNWAXED[previous], family).withPropertiesOf(initial));
                helper.assertTrue(player.getMainHandItem().getDamageValue() == ++uses,
                        "Scraping must damage the axe once per removed oxidation stage");
                assertPreserved(helper, original);
            }
            helper.assertTrue(useItem(helper, player) == InteractionResult.PASS, "Fresh copper must reject scraping");
            helper.assertTrue(player.getMainHandItem().getDamageValue() == uses, "Rejected scraping must not damage the axe");
        }
        helper.succeed();
    }

    private static void oxidation(GameTestHelper helper, int family) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int stage = 0; stage < UNWAXED.length; stage++) {
            BlockState initial = state(UNWAXED[stage], family);
            helper.setBlock(POS, initial);
            attachBracket(helper, player);
            BlockEntity original = helper.getBlockEntity(POS);
            helper.assertTrue(initial.getBlock() instanceof WeatheringCopper, "Unwaxed copper must use vanilla weathering");
            helper.assertTrue(initial.isRandomlyTicking() == (stage < 3), "Only unfinished oxidation must receive random ticks");
            helper.assertTrue(((WeatheringCopper) initial.getBlock()).getAge() == WeatherState.values()[stage],
                    "Vanilla weathering age must match the registered stage");
            helper.assertTrue(WeatheringCopper.getFirst(initial) == block(CopperVariant.COPPER, family).withPropertiesOf(initial),
                    "The first oxidation stage must retain block properties");
            initial.randomTick(helper.getLevel(), helper.absolutePos(POS), random(0, 0));
            assertState(helper, block(UNWAXED[Math.min(stage + 1, 3)], family).withPropertiesOf(initial));
            assertPreserved(helper, original);

            BlockState waxed = block(WAXED[stage], family).withPropertiesOf(initial);
            helper.setBlock(POS, waxed);
            helper.assertTrue(!(waxed.getBlock() instanceof WeatheringCopper), "Waxed copper must not participate in weathering");
            helper.assertTrue(!waxed.isRandomlyTicking(), "Waxed copper must not receive random ticks");
            helper.assertTrue(WeatheringCopper.getNext(waxed.getBlock()).isEmpty(), "Waxed copper must have no next oxidation stage");
            helper.assertTrue(WeatheringCopper.getPrevious(waxed).isEmpty(), "Wax must be removed before scraping oxidation");
            waxed.randomTick(helper.getLevel(), helper.absolutePos(POS), random());
            assertState(helper, waxed);
        }
        helper.succeed();
    }

    private static void kinetics(GameTestHelper helper, int family) {
        BlockState initial = state(CopperVariant.EXPOSED, family);
        helper.setBlock(POS.north(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(CreativeMotorBlock.FACING, Direction.SOUTH));
        helper.setBlock(POS, initial);
        helper.setBlock(POS.south(), AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        attachBracket(helper, player);
        helper.runAfterDelay(20, () -> {
            KineticBlockEntity original = helper.getBlockEntity(POS);
            Long network = original.network;
            assertSpeed(helper, POS);
            assertSpeed(helper, POS.south());
            initial.randomTick(helper.getLevel(), helper.absolutePos(POS), random(0, 0));
            assertState(helper, block(CopperVariant.WEATHERED, family).withPropertiesOf(initial));
            assertPreserved(helper, original);

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB));
            helper.useBlock(POS, player, hit(helper));
            assertState(helper, block(CopperVariant.WAXED_WEATHERED, family).withPropertiesOf(initial));
            assertPreserved(helper, original);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
            helper.useBlock(POS, player, hit(helper));
            assertState(helper, block(CopperVariant.WEATHERED, family).withPropertiesOf(initial));
            assertPreserved(helper, original);
            helper.useBlock(POS, player, hit(helper));
            assertState(helper, initial);
            assertPreserved(helper, original);
            helper.assertTrue(network != null && network.equals(original.network), "Appearance changes must retain the kinetic network");
            assertSpeed(helper, POS);
            assertSpeed(helper, POS.south());
            helper.runAfterDelay(10, () -> {
                assertPreserved(helper, original);
                assertSpeed(helper, POS);
                assertSpeed(helper, POS.south());
                helper.succeed();
            });
        });
    }

    private static void probability(GameTestHelper helper) {
        BlockState fresh = state(CopperVariant.COPPER, 2);
        helper.setBlock(POS, fresh);
        fresh.randomTick(helper.getLevel(), helper.absolutePos(POS), random(0.05688889F));
        assertState(helper, fresh);
        fresh.randomTick(helper.getLevel(), helper.absolutePos(POS), random(0, 0.75F));
        assertState(helper, fresh);
        fresh.randomTick(helper.getLevel(), helper.absolutePos(POS), random(0.05688888F, 0.749999F));
        assertState(helper, state(CopperVariant.EXPOSED, 2));

        BlockState exposed = helper.getBlockState(POS);
        exposed.randomTick(helper.getLevel(), helper.absolutePos(POS), random(0, 0.999999F));
        assertState(helper, state(CopperVariant.WEATHERED, 2));
        helper.succeed();
    }

    private static void neighbors(GameTestHelper helper) {
        BlockState exposed = state(CopperVariant.EXPOSED, 2);
        WeatheringCopper weathering = (WeatheringCopper) exposed.getBlock();
        helper.setBlock(POS, exposed);
        BlockPos edge = POS.offset(3, 1, 0);
        helper.setBlock(edge, Blocks.COPPER_BLOCK);
        helper.assertTrue(next(helper, weathering, exposed).isEmpty(), "Younger vanilla copper at Manhattan distance four must prevent oxidation");
        helper.setBlock(edge, Blocks.AIR);
        BlockPos outside = POS.offset(3, 2, 0);
        helper.setBlock(outside, Blocks.COPPER_BLOCK);
        helper.assertTrue(next(helper, weathering, exposed).isPresent(), "Copper outside Manhattan distance four must be ignored");
        helper.setBlock(outside, Blocks.AIR);

        BlockPos neighbor = POS.north();
        helper.setBlock(neighbor, block(CopperVariant.COPPER, 7));
        helper.assertTrue(next(helper, weathering, exposed).isEmpty(), "Younger encased copper must prevent oxidation");
        helper.setBlock(POS, Blocks.EXPOSED_COPPER);
        helper.assertTrue(next(helper, (WeatheringCopper) Blocks.EXPOSED_COPPER, Blocks.EXPOSED_COPPER.defaultBlockState()).isEmpty(),
                "Mod copper must influence vanilla copper weathering");
        helper.setBlock(POS, exposed);
        helper.setBlock(neighbor, block(CopperVariant.WAXED_COPPER, 7));
        helper.assertTrue(next(helper, weathering, exposed).isPresent(), "Waxed mod copper must not prevent oxidation");
        helper.setBlock(neighbor, Blocks.WAXED_COPPER_BLOCK);
        helper.assertTrue(next(helper, weathering, exposed).isPresent(), "Waxed vanilla copper must not prevent oxidation");

        helper.setBlock(neighbor, Blocks.EXPOSED_COPPER);
        helper.assertTrue(weathering.getNextState(exposed, helper.getLevel(), helper.absolutePos(POS), random(0.249999F)).isPresent(),
                "One equally aged neighbor must allow oxidation below probability one quarter");
        helper.assertTrue(weathering.getNextState(exposed, helper.getLevel(), helper.absolutePos(POS), random(0.25F)).isEmpty(),
                "One equally aged neighbor must suppress oxidation at probability one quarter");
        helper.setBlock(POS.south(), Blocks.WEATHERED_COPPER);
        helper.assertTrue(weathering.getNextState(exposed, helper.getLevel(), helper.absolutePos(POS), random(0.44F)).isPresent(),
                "Older neighbors must increase the squared age-ratio probability");
        helper.assertTrue(weathering.getNextState(exposed, helper.getLevel(), helper.absolutePos(POS), random(0.45F)).isEmpty(),
                "The age-ratio probability must be squared");
        helper.succeed();
    }

    private static java.util.Optional<BlockState> next(GameTestHelper helper, WeatheringCopper block, BlockState state) {
        return block.getNextState(state, helper.getLevel(), helper.absolutePos(POS), random(0));
    }

    private static void shield(GameTestHelper helper) {
        BlockState initial = state(CopperVariant.WAXED_WEATHERED, 2);
        helper.setBlock(POS, initial);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        helper.assertTrue(useItem(helper, player) == InteractionResult.PASS, "Vanilla shield use intent must take priority over scraping");
        assertState(helper, initial);
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 0, "Shield use must not damage the axe");
        player.setShiftKeyDown(true);
        helper.assertTrue(useItem(helper, player).consumesAction(), "Secondary use must allow wax removal while holding a shield");
        assertState(helper, state(CopperVariant.WEATHERED, 2));
        helper.succeed();
    }

    private static void componentReplacement(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.setBlock(POS, state(CopperVariant.COPPER, 2));
        attachBracket(helper, player);
        BlockEntity original = helper.getBlockEntity(POS);
        helper.setBlock(POS, state(CopperVariant.EXPOSED, 0));
        helper.assertTrue(helper.getBlockEntity(POS) != original && original.isRemoved(),
                "Replacing a shaft with a cogwheel must use normal block entity removal");
        helper.setBlock(POS, Blocks.AIR);
        helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(POS)) == null,
                "Removing a copper block must remove its block entity");
        helper.succeed();
    }

    private static Block block(CopperVariant variant, int family) {
        return ModBlocks.of(variant).allBlocks().get(family);
    }

    private static BlockState state(CopperVariant variant, int family) {
        BlockState state = block(variant, family).defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        if (state.hasProperty(EncasedCogwheelBlock.TOP_SHAFT)) {
            state = state.setValue(EncasedCogwheelBlock.TOP_SHAFT, true).setValue(EncasedCogwheelBlock.BOTTOM_SHAFT, true);
        }
        return state;
    }

    private static void attachBracket(GameTestHelper helper, Player player) {
        if (helper.getBlockEntity(POS) instanceof BracketedKineticBlockEntity) {
            player.setItemInHand(InteractionHand.MAIN_HAND, AllBlocks.METAL_BRACKET.asStack());
            helper.useBlock(POS, player, hit(helper));
            assertBracket(helper);
        }
    }

    private static void assertState(GameTestHelper helper, BlockState expected) {
        helper.assertTrue(helper.getBlockState(POS) == expected,
                "Expected " + expected + "; got " + helper.getBlockState(POS));
    }

    private static void assertPreserved(GameTestHelper helper, BlockEntity expected) {
        helper.assertTrue(helper.getBlockEntity(POS) == expected && !expected.isRemoved(),
                "Appearance changes must retain the same block entity");
        if (expected instanceof BracketedKineticBlockEntity) {
            assertBracket(helper);
        }
    }

    private static void assertBracket(GameTestHelper helper) {
        BracketedKineticBlockEntity bracketed = helper.getBlockEntity(POS);
        helper.assertTrue(bracketed.getBehaviour(BracketedBlockEntityBehaviour.TYPE).isBracketPresent(),
                "Appearance changes must retain attached Create brackets");
    }

    private static void assertSpeed(GameTestHelper helper, BlockPos pos) {
        KineticBlockEntity kinetic = helper.getBlockEntity(pos);
        helper.assertTrue(kinetic.getSpeed() == 16, "Expected continuous 16 RPM at " + pos + "; got " + kinetic.getSpeed());
    }

    private static InteractionResult useItem(GameTestHelper helper, Player player) {
        return player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper)));
    }

    private static BlockHitResult hit(GameTestHelper helper) {
        BlockPos absolute = helper.absolutePos(POS);
        return new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
    }

    private static RandomSource random(float... values) {
        return new LegacyRandomSource(0) {
            private int index;

            @Override
            public float nextFloat() {
                if (index == values.length) {
                    throw new IllegalStateException("Vanilla weathering requested an unexpected random sample");
                }
                return values[index++];
            }
        };
    }
}
