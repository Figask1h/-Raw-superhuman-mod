package dev.ashu.rsm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ashu.rsm.RawSuperhumanMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws Incandescence: a glowing shell over the head, chest and front of the arms, from a grey mask in the skin
 * layout (textures/entity/incandescence_*.png) tinted like heated metal, added on top of whatever is under it and
 * lit by nothing but itself. The shell sits just outside the outer skin layer, or just outside the armor where a
 * helmet or chestplate is worn, so it shows over armor too. Also draws it over the first-person arms.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
public final class IncandescenceRenderer {
    /** Just outside the hat layer (+0.5) and the sleeves and jacket (+0.25). */
    private static final float SKIN_SHELL_GROW = 0.6F;
    /** Just outside helmets and chestplates (+1.0). */
    private static final float ARMOR_SHELL_GROW = 1.15F;

    /** Glow colour along the heat: dark red, red-orange, orange, yellow-white. Rows of {heat, r, g, b}. */
    private static final float[][] RAMP = {
        {0.0F, 0.45F, 0.04F, 0.0F},
        {0.4F, 1.0F, 0.25F, 0.02F},
        {0.75F, 1.0F, 0.6F, 0.15F},
        {1.0F, 1.0F, 0.92F, 0.7F},
    };
    /** Brightness at the very start of the glow; rises to 1 at full heat. */
    private static final float MIN_BRIGHTNESS = 0.35F;

    private static final Map<PlayerSkin.Model, Shells> SHELLS = new EnumMap<>(PlayerSkin.Model.class);

    private record Shells(PlayerModel<AbstractClientPlayer> skin, PlayerModel<AbstractClientPlayer> armor, RenderType renderType) {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(RawSuperhumanMod.id("incandescence"), name);
    }

    private static final ModelLayerLocation SKIN_WIDE = layer("skin_wide");
    private static final ModelLayerLocation SKIN_SLIM = layer("skin_slim");
    private static final ModelLayerLocation ARMOR_WIDE = layer("armor_wide");
    private static final ModelLayerLocation ARMOR_SLIM = layer("armor_slim");

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SKIN_WIDE, () -> shell(SKIN_SHELL_GROW, false));
        event.registerLayerDefinition(SKIN_SLIM, () -> shell(SKIN_SHELL_GROW, true));
        event.registerLayerDefinition(ARMOR_WIDE, () -> shell(ARMOR_SHELL_GROW, false));
        event.registerLayerDefinition(ARMOR_SLIM, () -> shell(ARMOR_SHELL_GROW, true));
    }

    private static LayerDefinition shell(float grow, boolean slim) {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(grow), slim), 64, 64);
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer == null) continue;
            boolean slim = skin == PlayerSkin.Model.SLIM;
            Shells shells = new Shells(
                new PlayerModel<>(event.getEntityModels().bakeLayer(slim ? SKIN_SLIM : SKIN_WIDE), slim),
                new PlayerModel<>(event.getEntityModels().bakeLayer(slim ? ARMOR_SLIM : ARMOR_WIDE), slim),
                RenderType.eyes(RawSuperhumanMod.id("textures/entity/incandescence_" + (slim ? "slim" : "wide") + ".png")));
            SHELLS.put(skin, shells);
            renderer.addLayer(new Layer(renderer, shells));
        }
    }

    /** First person: the glow over an arm, in the pose it was just drawn with. */
    public static void renderArm(AbstractClientPlayer player, ModelPart arm, boolean right, PoseStack poseStack, MultiBufferSource buffers) {
        if (player.isInvisible()) return;
        float heat = IncandescenceGlow.heat(player.getId(), Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
        Shells shells = SHELLS.get(player.getSkin().model());
        if (heat <= 0.0F || shells == null) return;
        ModelPart glow = right ? shells.skin().rightArm : shells.skin().leftArm;
        glow.copyFrom(arm);
        glow.visible = true;
        glow.render(poseStack, buffers.getBuffer(shells.renderType()), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color(heat));
    }

    /** Heated-metal colour for a heat from 0 to 1, dimmer while barely hot. */
    static int color(float heat) {
        int i = 1;
        while (i < RAMP.length - 1 && heat > RAMP[i][0]) i++;
        float[] from = RAMP[i - 1];
        float[] to = RAMP[i];
        float t = Mth.clamp((heat - from[0]) / (to[0] - from[0]), 0.0F, 1.0F);
        float brightness = Mth.lerp(heat, MIN_BRIGHTNESS, 1.0F);
        int r = Math.round(Mth.lerp(t, from[1], to[1]) * brightness * 255.0F);
        int g = Math.round(Mth.lerp(t, from[2], to[2]) * brightness * 255.0F);
        int b = Math.round(Mth.lerp(t, from[3], to[3]) * brightness * 255.0F);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /** Third person, on every player renderer. Runs after the armor layer, so it is drawn over it. */
    private static final class Layer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private final Shells shells;

        Layer(PlayerRenderer renderer, Shells shells) {
            super(renderer);
            this.shells = shells;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (player.isInvisible()) return;
            float heat = IncandescenceGlow.heat(player.getId(), partialTick);
            if (heat <= 0.0F) return;
            int color = color(heat);
            VertexConsumer consumer = buffers.getBuffer(shells.renderType());
            boolean helmet = isArmor(player, EquipmentSlot.HEAD);
            boolean chestplate = isArmor(player, EquipmentSlot.CHEST);
            renderShell(shells.skin(), !helmet, !chestplate, poseStack, consumer, color);
            renderShell(shells.armor(), helmet, chestplate, poseStack, consumer, color);
        }

        /** The head and the torso with both arms each go on whichever shell clears what covers them. */
        private void renderShell(PlayerModel<AbstractClientPlayer> shell, boolean head, boolean torso,
                                 PoseStack poseStack, VertexConsumer consumer, int color) {
            if (!head && !torso) return;
            getParentModel().copyPropertiesTo(shell);
            shell.setAllVisible(false);
            shell.head.visible = head;
            shell.body.visible = torso;
            shell.rightArm.visible = torso;
            shell.leftArm.visible = torso;
            shell.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
        }

        private static boolean isArmor(AbstractClientPlayer player, EquipmentSlot slot) {
            return player.getItemBySlot(slot).getItem() instanceof ArmorItem;
        }
    }

    private IncandescenceRenderer() {}
}
