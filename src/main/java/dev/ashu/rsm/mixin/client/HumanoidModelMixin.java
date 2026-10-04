package dev.ashu.rsm.mixin.client;

import dev.ashu.rsm.client.ClapAnimation;
import dev.ashu.rsm.client.ClientGrabs;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {

    /**
     * Arm poses on top of whatever vanilla set up: holding a Captive out, and the Clap (both arms raised along the
     * look and turned towards each other). At the tail, so PlayerModel copies them to the sleeves and the armor
     * layer copies them to the armor.
     */
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void rsm$poseArms(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                              float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (ClientGrabs.isHolding(entity.getId())) rsm$holdPose((HumanoidModel<?>) (Object) this, entity);
        float progress = ClapAnimation.progress(entity.getId());
        if (progress < 0.0F) return;
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        float weight = ClapAnimation.weight(progress);
        float inward = ClapAnimation.inward(progress);
        float xRot = -Mth.HALF_PI + model.head.xRot;
        rsm$blend(model.rightArm, weight, xRot, model.head.yRot - inward);
        rsm$blend(model.leftArm, weight, xRot, model.head.yRot + inward);
    }

    /**
     * A Holder holds the Captive out with the main arm: straight ahead at chest height, or overhead along the
     * flight line in fast flight (body horizontal), fist first like a ram.
     */
    private static void rsm$holdPose(HumanoidModel<?> model, LivingEntity holder) {
        boolean right = holder.getMainArm() == HumanoidArm.RIGHT;
        ModelPart arm = right ? model.rightArm : model.leftArm;
        arm.xRot = holder.isFallFlying() ? -Mth.PI : -1.35F;
        arm.yRot = right ? -0.1F : 0.1F;
        arm.zRot = 0.0F;
    }

    private static void rsm$blend(ModelPart arm, float weight, float xRot, float yRot) {
        arm.xRot = Mth.lerp(weight, arm.xRot, xRot);
        arm.yRot = Mth.lerp(weight, arm.yRot, yRot);
        arm.zRot = Mth.lerp(weight, arm.zRot, 0.0F);
    }
}
