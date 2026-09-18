package dev.ashu.rsm.mixin.adastra;

import dev.ashu.rsm.compat.adastra.AdAstraBridge;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.utils.ModUtils;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applied only when Ad Astra is present (requiredMods in neoforge.mods.toml). */
@Mixin(value = ModUtils.class, remap = false)
public abstract class ModUtilsMixin {

    /** Survival players normally need to sit in a rocket above the atmosphere; a Bearer flying there qualifies too. */
    @Inject(method = "canTeleportToPlanet", at = @At("HEAD"), cancellable = true)
    private static void rsm$bearerMayTravel(Player player, Planet targetPlanet, CallbackInfoReturnable<Boolean> cir) {
        if (AdAstraBridge.bearerMayTravel(player, targetPlanet)) {
            cir.setReturnValue(true);
        }
    }
}
