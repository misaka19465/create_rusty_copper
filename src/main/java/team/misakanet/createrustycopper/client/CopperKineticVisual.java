package team.misakanet.createrustycopper.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.core.Direction;

public final class CopperKineticVisual<T extends KineticBlockEntity> extends KineticBlockEntityVisual<T>
        implements SimpleTickableVisual {
    private final List<RotatingInstance> instances = new ArrayList<>();
    private final List<RotatingInstance> largeCogShafts = new ArrayList<>();

    public CopperKineticVisual(VisualizationContext context, T blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        CopperPartialModels.Parts parts = CopperPartialModels.of(blockState);
        boolean large = ICogWheel.isLargeCog(blockState);
        boolean encasedCog = blockState.getBlock() instanceof EncasedCogwheelBlock;
        PartialModel main = large ? parts.shaftlessLargeCogwheel()
                : ICogWheel.isSmallCog(blockState) ? encasedCog ? parts.shaftlessCogwheel() : parts.cogwheel() : parts.shaft();
        add(main, Direction.UP, Direction.get(Direction.AxisDirection.POSITIVE, rotationAxis()), false);

        if (encasedCog) {
            EncasedCogwheelBlock cogwheel = (EncasedCogwheelBlock) blockState.getBlock();
            for (Direction direction : Direction.values()) {
                if (cogwheel.hasShaftTowards(blockEntity.getLevel(), pos, blockState, direction)) {
                    add(parts.shaftHalf(), Direction.SOUTH, direction, large);
                }
            }
        } else if (large) {
            add(parts.cogwheelShaft(), Direction.UP, Direction.get(Direction.AxisDirection.POSITIVE, rotationAxis()), true);
        }
    }

    private void add(PartialModel model, Direction from, Direction to, boolean largeCogShaft) {
        RotatingInstance instance = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(model))
                .createInstance();
        instance.setup(blockEntity).setPosition(getVisualPosition()).rotateToFace(from, to);
        if (largeCogShaft) {
            instance.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos));
            largeCogShafts.add(instance);
        }
        instance.setChanged();
        instances.add(instance);
    }

    @Override
    public void update(float partialTick) {
        for (RotatingInstance instance : instances) {
            instance.setup(blockEntity);
            if (largeCogShafts.contains(instance)) {
                instance.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos));
            }
            instance.setChanged();
        }
    }

    @Override
    public void tick(Context context) {
        instances.forEach(instance -> applyOverstressEffect(blockEntity, instance));
    }

    @Override
    public void updateLight(float partialTick) {
        relight(instances.toArray(RotatingInstance[]::new));
    }

    @Override
    protected void _delete() {
        instances.forEach(RotatingInstance::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        instances.forEach(consumer);
    }
}
