package io.github.misaka19465.createrustycopper.mixin;

import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import io.github.misaka19465.createrustycopper.content.CopperShaftBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaftBlock.class, remap = false)
public abstract class ShaftBlockMixin {
    @Inject(method = "isShaft", at = @At("HEAD"), cancellable = true)
    private static void createRustyCopper$isShaft(BlockState state, CallbackInfoReturnable<Boolean> callback) {
        if (state.getBlock() instanceof CopperShaftBlock) {
            callback.setReturnValue(true);
        }
    }
}
