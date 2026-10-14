package io.github.misaka19465.createrustycopper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import io.github.misaka19465.createrustycopper.content.CopperVariant;
import io.github.misaka19465.createrustycopper.config.ModServerConfig;
import io.github.misaka19465.createrustycopper.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(CreateRustyCopper.MOD_ID)
public final class CopperKineticsGameTests {
    private static boolean previousPenaltyEnabled;

    @BeforeBatch(batch = "copper_kinetics")
    public static void disableRustPenalties(ServerLevel level) {
        previousPenaltyEnabled = ModServerConfig.RUST_PENALTY_ENABLED.get();
        ModServerConfig.RUST_PENALTY_ENABLED.set(false);
    }

    @AfterBatch(batch = "copper_kinetics")
    public static void restoreRustPenalties(ServerLevel level) {
        ModServerConfig.RUST_PENALTY_ENABLED.set(previousPenaltyEnabled);
    }

    @GameTestGenerator
    public static Collection<TestFunction> tests() {
        List<TestFunction> tests = new ArrayList<>();
        for (CopperVariant variant : CopperVariant.values()) {
            tests.add(test("shafts", variant, helper -> shafts(helper, variant)));
            tests.add(test("gears", variant, helper -> gears(helper, variant)));
            tests.add(test("encased_transmission", variant, helper -> encasedTransmission(helper, variant)));
            tests.add(test("casing", variant, helper -> casing(helper, variant)));
            tests.add(test("components", variant, helper -> components(helper, variant)));
            tests.add(test("belts", variant, helper -> belts(helper, variant)));
        }
        return tests;
    }

    private static TestFunction test(String name, CopperVariant variant, java.util.function.Consumer<GameTestHelper> test) {
        return new TestFunction("copper_kinetics", CreateRustyCopper.MOD_ID + ":" + name + "_" + variant.blockId("shaft"),
                CreateRustyCopper.MOD_ID + ":empty", 100, 0, true, test);
    }

    private static void shafts(GameTestHelper helper, CopperVariant variant) {
        Block shaft = ModBlocks.of(variant).shaft().get();
        BlockPos[] motors = {new BlockPos(1, 2, 2), new BlockPos(7, 1, 2), new BlockPos(2, 2, 7)};
        Direction[] directions = {Direction.EAST, Direction.UP, Direction.SOUTH};
        for (int i = 0; i < motors.length; i++) {
            Direction direction = directions[i];
            motor(helper, motors[i], direction);
            helper.setBlock(motors[i].relative(direction), axis(shaft, direction.getAxis()));
            helper.setBlock(motors[i].relative(direction, 2), axis(AllBlocks.SHAFT.get(), direction.getAxis()));
            helper.setBlock(motors[i].relative(direction, 3), axis(shaft, direction.getAxis()));
        }
        helper.runAfterDelay(20, () -> {
            for (int i = 0; i < motors.length; i++) {
                for (int distance = 1; distance <= 3; distance++) {
                    speed(helper, motors[i].relative(directions[i], distance), 16);
                }
            }
            helper.succeed();
        });
    }

    private static void gears(GameTestHelper helper, CopperVariant variant) {
        ModBlocks.Components blocks = ModBlocks.of(variant);
        motor(helper, new BlockPos(2, 2, 1), Direction.SOUTH);
        helper.setBlock(2, 2, 2, axis(blocks.cogwheel().get(), Direction.Axis.Z));
        helper.setBlock(3, 2, 2, axis(blocks.cogwheel().get(), Direction.Axis.Z));
        helper.setBlock(4, 2, 2, axis(AllBlocks.COGWHEEL.get(), Direction.Axis.Z));

        motor(helper, new BlockPos(7, 2, 1), Direction.SOUTH);
        helper.setBlock(7, 2, 2, axis(blocks.largeCogwheel().get(), Direction.Axis.Z));
        helper.setBlock(8, 3, 2, axis(AllBlocks.COGWHEEL.get(), Direction.Axis.Z));

        motor(helper, new BlockPos(1, 2, 7), Direction.EAST);
        helper.setBlock(2, 2, 7, axis(blocks.largeCogwheel().get(), Direction.Axis.X));
        helper.setBlock(3, 3, 7, axis(AllBlocks.LARGE_COGWHEEL.get(), Direction.Axis.Y));

        motor(helper, new BlockPos(7, 2, 6), Direction.SOUTH);
        helper.setBlock(7, 2, 7, axis(blocks.cogwheel().get(), Direction.Axis.Z));
        helper.setBlock(8, 3, 7, axis(blocks.largeCogwheel().get(), Direction.Axis.Z));
        helper.runAfterDelay(20, () -> {
            speed(helper, new BlockPos(2, 2, 2), 16);
            speed(helper, new BlockPos(3, 2, 2), -16);
            speed(helper, new BlockPos(4, 2, 2), 16);
            speed(helper, new BlockPos(8, 3, 2), -32);
            speed(helper, new BlockPos(3, 3, 7), 16);
            speed(helper, new BlockPos(8, 3, 7), -8);
            helper.succeed();
        });
    }

    private static void casing(GameTestHelper helper, CopperVariant variant) {
        BlockPos pos = new BlockPos(5, 2, 5);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (Block bare : ModBlocks.of(variant).bareBlocks()) {
            for (Block encased : EncasingRegistry.getVariants(bare)) {
                helper.setBlock(pos, axis(bare, Direction.Axis.Z));
                BlockHitResult hit = hit(helper, pos, Direction.SOUTH);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(((EncasedBlock) encased).getCasing()));
                helper.assertTrue(((EncasableBlock) bare).tryEncase(helper.getBlockState(pos), helper.getLevel(),
                        helper.absolutePos(pos), player.getMainHandItem(), player, InteractionHand.MAIN_HAND, hit)
                        .consumesAction(), "Casing must apply");
                helper.assertBlockPresent(encased, pos);
                helper.assertBlockProperty(pos, BlockStateProperties.AXIS, Direction.Axis.Z);
                helper.assertTrue(helper.getBlockEntity(pos).getType().isValid(helper.getBlockState(pos)),
                        "Encased block entity must accept this block");
                drops(helper, pos, bare);
                List<ItemRequirement.StackRequirement> requirements =
                        ItemRequirement.of(helper.getBlockState(pos), helper.getBlockEntity(pos)).getRequiredItems();
                helper.assertTrue(requirements.size() == 1 && requirements.getFirst().stack.is(bare.asItem()),
                        "Schematic requirements must preserve the copper variant");

                player.setItemInHand(InteractionHand.MAIN_HAND, AllItems.WRENCH.asStack());
                UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
                if (encased instanceof EncasedCogwheelBlock cogwheel) {
                    cogwheel.onWrenched(helper.getBlockState(pos), context);
                    helper.assertBlockProperty(pos, EncasedCogwheelBlock.TOP_SHAFT, true);
                    cogwheel.onWrenched(helper.getBlockState(pos), context);
                    helper.assertBlockProperty(pos, EncasedCogwheelBlock.TOP_SHAFT, false);
                }
                ((IWrenchable) encased).onSneakWrenched(helper.getBlockState(pos), context);
                helper.assertBlockPresent(bare, pos);
                helper.assertBlockProperty(pos, BlockStateProperties.AXIS, Direction.Axis.Z);
            }
        }
        helper.succeed();
    }

    private static void encasedTransmission(GameTestHelper helper, CopperVariant variant) {
        ModBlocks.Components blocks = ModBlocks.of(variant);
        motor(helper, new BlockPos(2, 2, 1), Direction.SOUTH);
        helper.setBlock(2, 2, 2, axis(blocks.andesiteShaft().get(), Direction.Axis.Z));
        helper.setBlock(2, 2, 3, axis(blocks.brassShaft().get(), Direction.Axis.Z));
        helper.setBlock(2, 2, 4, axis(AllBlocks.SHAFT.get(), Direction.Axis.Z));

        motor(helper, new BlockPos(7, 2, 1), Direction.SOUTH);
        helper.setBlock(7, 2, 2, axis(blocks.andesiteCogwheel().get(), Direction.Axis.Z)
                .setValue(EncasedCogwheelBlock.BOTTOM_SHAFT, true));
        helper.setBlock(8, 2, 2, axis(blocks.brassCogwheel().get(), Direction.Axis.Z));

        motor(helper, new BlockPos(2, 2, 6), Direction.SOUTH);
        helper.setBlock(2, 2, 7, axis(blocks.brassLargeCogwheel().get(), Direction.Axis.Z)
                .setValue(EncasedCogwheelBlock.BOTTOM_SHAFT, true));
        helper.setBlock(3, 3, 7, axis(blocks.andesiteCogwheel().get(), Direction.Axis.Z));

        motor(helper, new BlockPos(7, 2, 6), Direction.SOUTH);
        helper.setBlock(7, 2, 7, axis(blocks.andesiteLargeCogwheel().get(), Direction.Axis.Z));
        helper.runAfterDelay(20, () -> {
            speed(helper, new BlockPos(2, 2, 4), 16);
            speed(helper, new BlockPos(8, 2, 2), -16);
            speed(helper, new BlockPos(3, 3, 7), -32);
            speed(helper, new BlockPos(7, 2, 7), 0);
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, AllItems.WRENCH.asStack());
            blocks.andesiteLargeCogwheel().get().onWrenched(helper.getBlockState(new BlockPos(7, 2, 7)),
                    new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, new BlockPos(7, 2, 7), Direction.NORTH)));
            helper.runAfterDelay(10, () -> {
                speed(helper, new BlockPos(7, 2, 7), 16);
                helper.succeed();
            });
        });
    }

    private static void components(GameTestHelper helper, CopperVariant variant) {
        BlockPos pos = new BlockPos(5, 2, 5);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (Block block : ModBlocks.of(variant).bareBlocks()) {
            helper.setBlock(pos, axis(block, Direction.Axis.Z).setValue(BlockStateProperties.WATERLOGGED, true));
            helper.assertTrue(helper.getBlockState(pos).getFluidState().getType() == Fluids.WATER, "Must support waterlogging");
            helper.assertTrue(helper.getBlockState(pos).isRandomlyTicking()
                            == (!variant.waxed() && variant.weatherState() != WeatherState.OXIDIZED),
                    "Only unwaxed copper before full oxidation must receive random ticks");
            helper.assertTrue(helper.getBlockEntity(pos).getType().isValid(helper.getBlockState(pos)),
                    "Bare block entity must accept this block");
            KineticBlockEntity kinetic = helper.getBlockEntity(pos);
            helper.assertTrue(kinetic.calculateStressApplied() == 0, "Disabled rust penalties must preserve zero stress cost");
            drops(helper, pos, block);

            player.setItemInHand(InteractionHand.MAIN_HAND, AllBlocks.METAL_BRACKET.asStack());
            player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos, Direction.UP)));
            BracketedKineticBlockEntity bracketed = helper.getBlockEntity(pos);
            BracketedBlockEntityBehaviour bracket = bracketed.getBehaviour(BracketedBlockEntityBehaviour.TYPE);
            helper.assertTrue(bracket.isBracketPresent(), "Must accept Create brackets");
            player.setItemInHand(InteractionHand.MAIN_HAND, AllItems.WRENCH.asStack());
            ((IWrenchable) block).onWrenched(helper.getBlockState(pos),
                    new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos, Direction.UP)));
            helper.assertTrue(!bracket.isBracketPresent(), "Wrench must remove bracket");
            ((IWrenchable) block).onSneakWrenched(helper.getBlockState(pos),
                    new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos, Direction.UP)));
            helper.assertTrue(player.getInventory().contains(new ItemStack(block)), "Wrench must return the same component");
        }
        helper.succeed();
    }

    private static void belts(GameTestHelper helper, CopperVariant variant) {
        BlockPos start = new BlockPos(2, 2, 3);
        BlockPos end = new BlockPos(7, 2, 3);
        motor(helper, start.north(), Direction.SOUTH);
        helper.setBlock(start, axis(ModBlocks.of(variant).shaft().get(), Direction.Axis.Z));
        helper.setBlock(end, axis(AllBlocks.SHAFT.get(), Direction.Axis.Z));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, AllItems.BELT_CONNECTOR.asStack());
        helper.assertTrue(BeltConnectorItem.validateAxis(helper.getLevel(), helper.absolutePos(start)),
                "Belt connector must recognize copper shafts");
        helper.assertTrue(player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                hit(helper, start, Direction.UP))).consumesAction(), "First belt endpoint must be accepted");
        helper.assertTrue(player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                hit(helper, end, Direction.UP))).consumesAction(), "Second belt endpoint must be accepted");
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(AllBlocks.BELT.get(), start);
            helper.assertBlockPresent(AllBlocks.BELT.get(), end);
            speed(helper, end, 16);
            helper.succeed();
        });
    }

    private static BlockState axis(Block block, Direction.Axis axis) {
        return block.defaultBlockState().setValue(BlockStateProperties.AXIS, axis);
    }

    private static void motor(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(CreativeMotorBlock.FACING, facing));
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face) {
        BlockPos absolute = helper.absolutePos(pos);
        return new BlockHitResult(Vec3.atCenterOf(absolute), face, absolute, false);
    }

    private static void speed(GameTestHelper helper, BlockPos pos, float expected) {
        KineticBlockEntity blockEntity = helper.getBlockEntity(pos);
        helper.assertTrue(blockEntity != null && blockEntity.getSpeed() == expected,
                "Expected " + expected + " RPM at " + pos + "; got " + (blockEntity == null ? "no block entity" : blockEntity.getSpeed()));
    }

    private static void drops(GameTestHelper helper, BlockPos pos, Block expected) {
        List<ItemStack> drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                helper.getBlockEntity(pos));
        helper.assertTrue(drops.size() == 1 && drops.getFirst().is(expected.asItem()) && drops.getFirst().getCount() == 1,
                "Drops must preserve the copper variant");
    }
}
