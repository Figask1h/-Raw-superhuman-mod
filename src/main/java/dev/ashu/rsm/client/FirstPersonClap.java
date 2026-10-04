package dev.ashu.rsm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;

/**
 * The Clap seen from the eyes: both bare arms of the player model, posed like the third-person animation and
 * drawn from where the camera sits, so they swing up from below the screen and meet in the middle. Replaces
 * vanilla's first-person hands while the animation runs.
 */
final class FirstPersonClap {
    /** The camera is at eye height (1.62 blocks); the model's neck pivot is at 1.501. In model units (1/16 block). */
    private static final float EYES_ABOVE_NECK = 1.9F;

    static void render(AbstractClientPlayer player, float progress, PoseStack poseStack, MultiBufferSource buffers, int light) {
        PlayerRenderer renderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        ResourceLocation skin = player.getSkin().texture();
        float raise = -Mth.HALF_PI * ClapAnimation.weight(progress);
        float inward = ClapAnimation.inward(progress);

        poseStack.pushPose();
        // Camera space (x right, y up, -z ahead) to model space (x and y flipped, as LivingEntityRenderer does), at the neck.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, EYES_ABOVE_NECK / 16.0F, 0.0F);
        renderArm(player, true, model.rightArm, model.rightSleeve, raise, -inward, skin, poseStack, buffers, light);
        renderArm(player, false, model.leftArm, model.leftSleeve, raise, inward, skin, poseStack, buffers, light);
        poseStack.popPose();
    }

    /** Draws one arm (its sleeve layer and Incandescence too) in the given pose, then puts the shared model parts back. */
    private static void renderArm(AbstractClientPlayer player, boolean right, ModelPart arm, ModelPart sleeve, float xRot, float yRot,
                                  ResourceLocation skin, PoseStack poseStack, MultiBufferSource buffers, int light) {
        PartPose armPose = arm.storePose();
        PartPose sleevePose = sleeve.storePose();
        boolean armVisible = arm.visible;
        boolean sleeveVisible = sleeve.visible;

        arm.resetPose();
        arm.setRotation(xRot, yRot, 0.0F);
        arm.visible = true;
        arm.render(poseStack, buffers.getBuffer(RenderType.entitySolid(skin)), light, OverlayTexture.NO_OVERLAY);
        if (player.isModelPartShown(right ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE)) {
            sleeve.copyFrom(arm);
            sleeve.visible = true;
            sleeve.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(skin)), light, OverlayTexture.NO_OVERLAY);
        }
        IncandescenceRenderer.renderArm(player, arm, right, poseStack, buffers);

        arm.loadPose(armPose);
        sleeve.loadPose(sleevePose);
        arm.visible = armVisible;
        sleeve.visible = sleeveVisible;
    }

    private FirstPersonClap() {}
}
