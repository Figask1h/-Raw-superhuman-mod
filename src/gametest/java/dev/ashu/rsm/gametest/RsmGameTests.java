package dev.ashu.rsm.gametest;

import com.mojang.authlib.GameProfile;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.attack.AttackKind;
import dev.ashu.rsm.attack.Attacks;
import dev.ashu.rsm.power.BlockDestruction;
import dev.ashu.rsm.power.PassiveEffect;
import dev.ashu.rsm.power.PassiveEffectsHandler;
import dev.ashu.rsm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/** In-world checks, run by {@code ./gradlew runGameTestServer}. Coordinates are relative to the test's empty 9x6x9 area. */
@GameTestHolder(RawSuperhumanMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RsmGameTests {
    /** Yaw that looks along +X. */
    private static final float FACING_EAST = -90.0F;

    @GameTest(template = "empty")
    public static void ringRecipeIsLoaded(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(RawSuperhumanMod.id("ring")).isPresent(),
            "rsm:ring recipe missing while the ring is switched on");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slashHitsInTheOpen(GameTestHelper helper) {
        floor(helper);
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 3.5, 1, 4.5);
        Attacks.perform(bearer, AttackKind.SLASH);
        helper.assertTrue(zombie.isDeadOrDying(), "Slash missed a zombie standing in the open");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slashStopsAtWalls(GameTestHelper helper) {
        floor(helper);
        for (int y = 1; y <= 3; y++) {
            for (int z = 3; z <= 5; z++) helper.setBlock(new BlockPos(2, y, z), Blocks.STONE);
        }
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 3.5, 1, 4.5);
        Attacks.perform(bearer, AttackKind.SLASH);
        helper.assertTrue(zombie.getHealth() == zombie.getMaxHealth(), "Slash hit a zombie behind a stone wall");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void switchingOffKeepsPotions(GameTestHelper helper) {
        // Any living wearer will do; a zombie avoids needing a connected player for effect packets.
        Zombie wearer = target(helper, 4.5, 1, 4.5);
        PassiveEffect.NIGHT_VISION.apply(wearer);
        PassiveEffectsHandler.flip(wearer, PassiveEffect.NIGHT_VISION);
        helper.assertTrue(!wearer.hasEffect(MobEffects.NIGHT_VISION), "Switching Night Vision off left the ring's effect on");

        PassiveEffectsHandler.flip(wearer, PassiveEffect.NIGHT_VISION);
        wearer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 3600));
        PassiveEffectsHandler.flip(wearer, PassiveEffect.NIGHT_VISION);
        helper.assertTrue(wearer.hasEffect(MobEffects.NIGHT_VISION), "Switching Night Vision off removed a potion's effect");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void imperviousBlocks(GameTestHelper helper) {
        Block[] impervious = {Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ANVIL, Blocks.ENCHANTING_TABLE, Blocks.BEDROCK, Blocks.CHEST, Blocks.FURNACE};
        Block[] breakable = {Blocks.STONE, Blocks.DIRT, Blocks.GLASS, Blocks.OAK_LOG, Blocks.DEEPSLATE, Blocks.IRON_BLOCK};
        int x = 0;
        for (Block block : impervious) assertImpervious(helper, new BlockPos(x++, 1, 1), block, true);
        x = 0;
        for (Block block : breakable) assertImpervious(helper, new BlockPos(x++, 1, 3), block, false);
        helper.succeed();
    }

    private static void assertImpervious(GameTestHelper helper, BlockPos relative, Block block, boolean expected) {
        helper.setBlock(relative, block);
        BlockPos pos = helper.absolutePos(relative);
        boolean actual = BlockDestruction.isImpervious(helper.getLevel(), pos, helper.getLevel().getBlockState(pos));
        helper.assertTrue(actual == expected, block + (expected ? " should" : " should not") + " be impervious");
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
        }
    }

    private static ServerPlayer bearer(GameTestHelper helper, double x, double y, double z, float yaw) {
        // Not logged in: a login would make mods (Ad Astra, us) send packets to a client that does not exist.
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            new GameProfile(UUID.randomUUID(), "rsm-test-bearer"), ClientInformation.createDefault());
        CuriosApi.getCuriosInventory(player).orElseThrow().setEquippedCurio("ring", 0, new ItemStack(ModItems.RING.get()));
        Vec3 pos = helper.absoluteVec(new Vec3(x, y, z));
        player.moveTo(pos.x, pos.y, pos.z, yaw, 0.0F);
        return player;
    }

    private static Zombie target(GameTestHelper helper, double x, double y, double z) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, (float) x, (float) y, (float) z);
        zombie.setNoAi(true);
        return zombie;
    }

    private RsmGameTests() {}
}
