package dev.ashu.rsm.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ashu.rsm.client.IncandescenceRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    /**
     * Incandescence over the first-person arm. At the tail of renderHand, because only then is the arm posed as it
     * was drawn; NeoForge's RenderArmEvent fires before that.
     */
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void rsm$incandescentArm(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player,
                                     ModelPart rendererArm, ModelPart rendererArmwear, CallbackInfo ci) {
        boolean right = rendererArm == ((PlayerRenderer) (Object) this).getModel().rightArm;
        IncandescenceRenderer.renderArm(player, rendererArm, right, poseStack, buffer);
    }
}
