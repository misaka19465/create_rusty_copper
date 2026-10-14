package team.misakanet.createrustycopper.content;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import team.misakanet.createrustycopper.config.ModServerConfig;
import team.misakanet.createrustycopper.registry.ModBlocks;

public final class CopperStress {
    private CopperStress() {
    }

    public static void register() {
        for (CopperVariant variant : CopperVariant.values()) {
            for (var block : ModBlocks.of(variant).allBlocks()) {
                BlockStressValues.IMPACTS.register(block, () -> ModServerConfig.stressImpact(variant));
            }
        }
    }

    public static void refresh(KineticBlockEntity kinetic) {
        if (!(kinetic.getBlockState().getBlock() instanceof CopperKineticBlock)
                || kinetic.getLevel() == null || kinetic.getLevel().isClientSide || !kinetic.hasNetwork()) {
            return;
        }
        KineticNetwork network = kinetic.getOrCreateNetwork();
        Float previousImpact = network.members.get(kinetic);
        if (previousImpact != null) {
            float impact = kinetic.calculateStressApplied();
            // Tooltips also recalculate lastStressApplied, so compare against the network's stored value.
            if (Float.compare(previousImpact, impact) != 0) {
                network.updateStressFor(kinetic, impact);
                kinetic.notifyUpdate();
            }
        }
    }

    public static void refreshLoadedNetwork(KineticBlockEntity kinetic) {
        if (kinetic.getLevel() == null || kinetic.getLevel().isClientSide || !kinetic.hasNetwork()) {
            return;
        }
        KineticNetwork network = kinetic.getOrCreateNetwork();
        if (network.members.keySet().stream().noneMatch(member -> member.getBlockState().getBlock() instanceof CopperKineticBlock)) {
            return;
        }
        // addSilently changes member costs without recalculating totals or updating saved overload flags.
        network.updateNetwork();
        network.sync();
    }
}
