package team.misakanet.createrustycopper.content;

import java.util.function.Supplier;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import team.misakanet.createrustycopper.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EncasedCopperShaftBlock extends EncasedShaftBlock implements CopperKineticBlock {
    private final CopperVariant variant;
    private final Supplier<CopperShaftBlock> shaft;

    public EncasedCopperShaftBlock(Properties properties, Supplier<Block> casing, Supplier<CopperShaftBlock> shaft,
            CopperVariant variant) {
        super(properties, casing);
        this.shaft = shaft;
        this.variant = variant;
    }

    @Override
    public CopperVariant variant() {
        return variant;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!CopperKineticBlock.isAppearanceChange(state, newState)) {
            super.onRemove(state, level, pos, newState, moving);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (!CopperKineticBlock.preserveKinetics(state, level, pos, oldState)) {
            super.onPlace(state, level, pos, oldState, moving);
        }
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ENCASED_SHAFT.get();
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            context.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, context.getClickedPos(), Block.getId(state));
            KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(),
                    shaft.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (target instanceof BlockHitResult hit) {
            return new ItemStack(hit.getDirection().getAxis() == getRotationAxis(state) ? shaft.get() : getCasing());
        }
        return new ItemStack(shaft.get());
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        return ItemRequirement.of(shaft.get().defaultBlockState(), blockEntity);
    }
}
