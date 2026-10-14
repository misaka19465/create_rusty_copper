package io.github.misaka19465.createrustycopper.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class CopperKineticRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T> {
    public CopperKineticRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
            int light, int overlay) {
        if (VisualizationManager.supportsVisualization(blockEntity.getLevel())) {
            return;
        }

        BlockState state = blockEntity.getBlockState();
        CopperPartialModels.Parts parts = CopperPartialModels.of(state);
        Direction.Axis axis = getRotationAxisOf(blockEntity);
        Direction facing = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        boolean large = ICogWheel.isLargeCog(state);
        boolean encasedCog = state.getBlock() instanceof EncasedCogwheelBlock;
        PartialModel main = large ? parts.shaftlessLargeCogwheel()
                : ICogWheel.isSmallCog(state) ? encasedCog ? parts.shaftlessCogwheel() : parts.cogwheel() : parts.shaft();
        var vertexConsumer = buffers.getBuffer(RenderType.solid());
        renderRotatingBuffer(blockEntity, CachedBuffers.partialFacingVertical(main, state, facing), poseStack,
                vertexConsumer, light);

        if (!large && !encasedCog) {
            return;
        }

        // Large wheels and their shafts use different tooth-alignment offsets in Create.
        float shaftAngle = large
                ? BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft((SimpleKineticBlockEntity) blockEntity, axis)
                : getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis);
        if (encasedCog) {
            EncasedCogwheelBlock cogwheel = (EncasedCogwheelBlock) state.getBlock();
            for (Direction direction : Direction.values()) {
                if (cogwheel.hasShaftTowards(blockEntity.getLevel(), blockEntity.getBlockPos(), state, direction)) {
                    SuperByteBuffer shaft = CachedBuffers.partialFacing(parts.shaftHalf(), state, direction);
                    kineticRotationTransform(shaft, blockEntity, axis, shaftAngle, light).renderInto(poseStack, vertexConsumer);
                }
            }
        } else {
            SuperByteBuffer shaft = CachedBuffers.partialFacingVertical(parts.cogwheelShaft(), state, facing);
            kineticRotationTransform(shaft, blockEntity, axis, shaftAngle, light).renderInto(poseStack, vertexConsumer);
        }
    }
}
