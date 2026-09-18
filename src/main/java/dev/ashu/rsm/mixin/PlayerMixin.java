package dev.ashu.rsm.mixin;

import dev.ashu.rsm.power.FlightHooks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * In fast flight the local player is moved by the mod. Vanilla travel would either apply
     * creative-flight friction or, because the fall-flying flag is set for the pose, elytra physics.
     */
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void rsm$fastFlightTravel(Vec3 travelVector, CallbackInfo ci) {
        if (FlightHooks.overrideTravel((Player) (Object) this)) {
            ci.cancel();
        }
    }
}
