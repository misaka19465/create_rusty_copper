package io.github.misaka19465.createrustycopper.mixin;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.github.misaka19465.createrustycopper.content.CopperStress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticBlockEntityMixin {
    @Inject(method = "initialize", at = @At("TAIL"))
    private void createRustyCopper$refreshLoadedStress(CallbackInfo callback) {
        CopperStress.refreshLoadedNetwork((KineticBlockEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void createRustyCopper$refreshConfiguredStress(CallbackInfo callback) {
        CopperStress.refresh((KineticBlockEntity) (Object) this);
    }
}
