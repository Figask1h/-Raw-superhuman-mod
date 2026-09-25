package dev.ashu.rsm.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.attack.AttackKind;
import dev.ashu.rsm.attack.Attacks;
import dev.ashu.rsm.power.BlockDestruction;
import dev.ashu.rsm.power.Breakthrough;
import dev.ashu.rsm.power.FlightMode;
import dev.ashu.rsm.power.FlightState;
import dev.ashu.rsm.power.PassiveEffect;
import dev.ashu.rsm.power.PassiveEffectsHandler;
import dev.ashu.rsm.registry.ModAttachments;
import dev.ashu.rsm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
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

import java.util.List;
import java.util.UUID;

/** In-world checks, run by {@code ./gradlew runGameTestServer}. Coordinates are relative to the test's empty 9x6x9 area. */
@GameTestHolder(RawSuperhumanMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RsmGameTests {
    /** Yaw that looks along +X. */
    private static final float FACING_EAST = -90.0F;
    private static final Vec3 EAST = new Vec3(1.0, 0.0, 0.0);

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

    @GameTest(template = "empty")
    public static void tunnelEndsAtObsidian(GameTestHelper helper) {
        stoneBlock(helper, 3, 7);
        helper.setBlock(new BlockPos(5, 2, 4), Blocks.OBSIDIAN);
        List<BlockPos> tunnel = Breakthrough.tunnel(helper.getLevel(), helper.absoluteVec(new Vec3(1.5, 2.5, 4.5)), EAST, 7.0);
        helper.assertTrue(tunnel.contains(helper.absolutePos(new BlockPos(3, 2, 4))), "Tunnel does not start at the wall");
        helper.assertTrue(tunnel.contains(helper.absolutePos(new BlockPos(4, 2, 4))), "Tunnel stops before the obsidian");
        helper.assertTrue(!tunnel.contains(helper.absolutePos(new BlockPos(5, 2, 4))), "Tunnel goes through obsidian");
        helper.assertTrue(!tunnel.contains(helper.absolutePos(new BlockPos(6, 2, 4))), "Tunnel continues behind obsidian");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fastBearerBreaksThrough(GameTestHelper helper) {
        stoneBlock(helper, 3, 4);
        ServerPlayer bearer = flying(bearer(helper, 1.5, 2.2, 4.5, FACING_EAST), 150.0);
        Breakthrough.breakOnServer(bearer, Breakthrough.tunnel(helper.getLevel(), helper.absoluteVec(new Vec3(1.5, 2.5, 4.5)), EAST, 9.0));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 2, 4));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 2, 4));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slowBearerCannotBreak(GameTestHelper helper) {
        stoneBlock(helper, 3, 4);
        ServerPlayer bearer = flying(bearer(helper, 1.5, 2.2, 4.5, FACING_EAST), 40.0);
        Breakthrough.breakOnServer(bearer, Breakthrough.tunnel(helper.getLevel(), helper.absoluteVec(new Vec3(1.5, 2.5, 4.5)), EAST, 9.0));
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(3, 2, 4));
        helper.succeed();
    }

    /** Server cost of flying at 150 b/s straight up through 150 blocks of solid stone, 7x7 across. */
    @GameTest(template = "empty")
    public static void breakthroughCost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(1, 1, 1));
        for (int y = 0; y < 150; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) level.setBlock(base.offset(x, y, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        ServerPlayer bearer = flying(bearer(helper, 4.5, 0.2, 4.5, FACING_EAST), 150.0);
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 center = helper.absoluteVec(new Vec3(4.5, 0.5, 4.5));
        long tunnelNanos = 0;
        long destroyNanos = 0;
        int blocks = 0;
        for (int tick = 0; tick < 20; tick++) {
            bearer.setPos(center.x, center.y - 0.3, center.z);
            long start = System.nanoTime();
            List<BlockPos> tunnel = Breakthrough.tunnel(level, center, up, 7.5 + 1.5);
            long computed = System.nanoTime();
            Breakthrough.breakOnServer(bearer, tunnel);
            destroyNanos += System.nanoTime() - computed;
            tunnelNanos += computed - start;
            blocks += tunnel.size();
            center = center.add(up.scale(7.5));
        }
        LogUtils.getLogger().info("Breakthrough cost: {} blocks in 20 ticks; tunnel {} ms/tick, destroy {} ms/tick",
            blocks, tunnelNanos / 20 / 1.0E6, destroyNanos / 20 / 1.0E6);
        helper.assertTrue(blocks > 1500, "Tunnel too small: " + blocks + " blocks");
        helper.assertTrue(destroyNanos / 20 < 25_000_000L, "Destroying a tick of tunnel takes over half a server tick");
        helper.succeed();
    }

    /** Solid stone from x = fromX to toX, 5 high and 5 wide around z = 4. */
    private static void stoneBlock(GameTestHelper helper, int fromX, int toX) {
        for (int x = fromX; x <= toX; x++) {
            for (int y = 0; y <= 4; y++) {
                for (int z = 2; z <= 6; z++) helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
            }
        }
    }

    private static ServerPlayer flying(ServerPlayer player, double speed) {
        FlightState state = player.getData(ModAttachments.FLIGHT);
        state.mode = FlightMode.BOOST;
        state.speed = speed;
        return player;
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
