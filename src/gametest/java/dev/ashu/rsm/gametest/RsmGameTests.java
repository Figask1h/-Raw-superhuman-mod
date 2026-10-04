package dev.ashu.rsm.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import dev.ashu.rsm.attack.AttackKind;
import dev.ashu.rsm.attack.Attacks;
import dev.ashu.rsm.grab.GrabPose;
import dev.ashu.rsm.grab.Grabs;
import dev.ashu.rsm.power.Barrier;
import dev.ashu.rsm.power.BlockDestruction;
import dev.ashu.rsm.power.Breakthrough;
import dev.ashu.rsm.power.FlightMode;
import dev.ashu.rsm.power.FlightState;
import dev.ashu.rsm.power.Incandescence;
import dev.ashu.rsm.power.PassiveEffect;
import dev.ashu.rsm.power.PassiveEffectsHandler;
import dev.ashu.rsm.registry.ModAttachments;
import dev.ashu.rsm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

    /** The ring's Regeneration I, kept up every tick like curioTick does, heals 1 HP per 50 ticks: 8 HP in 400 ticks. */
    @GameTest(template = "empty", timeoutTicks = 500)
    public static void regenerationHealsAtVanillaRate(GameTestHelper helper) {
        floor(helper);
        // Undead ignore Regeneration, so not a zombie.
        Villager wearer = helper.spawn(EntityType.VILLAGER, 4.5F, 1.0F, 4.5F);
        wearer.setNoAi(true);
        wearer.setHealth(1.0F);
        helper.onEachTick(() -> PassiveEffect.REGENERATION.apply(wearer));
        helper.runAfterDelay(400, () -> {
            helper.assertTrue(wearer.getHealth() == 9.0F, "Ring Regeneration healed " + (wearer.getHealth() - 1.0F) + " HP in 400 ticks instead of 8");
            helper.succeed();
        });
    }

    /** Off the ground and under water a Bearer digs as fast as standing on dry land; without the ring vanilla slows it. */
    @GameTest(template = "empty")
    @SuppressWarnings("deprecation") // isEyeInFluid: the check Player.getDigSpeed uses
    public static void bearerDigsAtGroundSpeedAnywhere(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, 4.5, 1, 4.5, FACING_EAST);
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockPos pos = helper.absolutePos(new BlockPos(5, 1, 4));
        player.setOnGround(true);
        float onGround = player.getDigSpeed(stone, pos);

        player.setOnGround(false);
        assertSameSpeed(helper, player.getDigSpeed(stone, pos), onGround, "A Bearer in the air digs at a different speed than on the ground");

        helper.setBlock(new BlockPos(4, 1, 4), Blocks.WATER);
        helper.setBlock(new BlockPos(4, 2, 4), Blocks.WATER);
        player.baseTick();
        helper.assertTrue(player.isEyeInFluid(FluidTags.WATER), "Test setup: the player's eyes are not under water");
        assertSameSpeed(helper, player.getDigSpeed(stone, pos), onGround, "A Bearer under water digs at a different speed than on dry ground");

        ServerPlayer plain = player(helper, 4.5, 1, 4.5, FACING_EAST);
        plain.setOnGround(false);
        plain.baseTick();
        helper.assertTrue(plain.getDigSpeed(stone, pos) < onGround / 20.0F, "Test setup: without the ring digging in the air under water is not slowed");
        helper.succeed();
    }

    /** Undoing x0.2 and /5 in floats can be off in the last bit; a real penalty is off by a factor of 5. */
    private static void assertSameSpeed(GameTestHelper helper, float actual, float expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) <= expected * 1.0E-5F, message + ": " + actual + " vs " + expected);
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

    @GameTest(template = "empty")
    public static void bearerSurvivesSpace(GameTestHelper helper) {
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 4.5, 1, 4.5);
        helper.assertTrue(AdAstraEvents.EntityOxygenEvent.fire(bearer, false), "Ad Astra thinks a Bearer has no oxygen");
        helper.assertTrue(!AdAstraEvents.EntityOxygenEvent.fire(zombie, false), "Oxygen immunity leaked to a zombie");
        helper.assertTrue(!AdAstraEvents.ColdTemperatureTickEvent.fire(helper.getLevel(), bearer), "Ad Astra still freezes a Bearer");
        helper.assertTrue(AdAstraEvents.ColdTemperatureTickEvent.fire(helper.getLevel(), zombie), "Cold immunity leaked to a zombie");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void strikeHitsTheTargetInSight(GameTestHelper helper) {
        floor(helper);
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 3.5, 1, 4.5);
        Attacks.perform(bearer, AttackKind.STRIKE);
        helper.assertTrue(zombie.isDeadOrDying(), "Strike missed a zombie two blocks ahead");
        helper.succeed();
    }

    /** One burst on the way up through the Barrier; the next only after Speed fell REARM_MARGIN below it. */
    @GameTest(template = "empty")
    public static void barrierBurstsOncePerCrossing(GameTestHelper helper) {
        ServerPlayer bearer = flying(bearer(helper, 4.5, 6, 4.5, FACING_EAST), 0.0);
        FlightState state = bearer.getData(ModAttachments.FLIGHT);
        double barrier = Barrier.speed();
        double[] speeds = {barrier - 1, barrier + 1, barrier + 40, barrier - 5, barrier + 1, barrier - Barrier.REARM_MARGIN - 1, barrier};
        boolean[] bursts = {false, true, false, false, false, false, true};
        for (int i = 0; i < speeds.length; i++) {
            state.speed = speeds[i];
            helper.assertTrue(Barrier.track(bearer, state) == bursts[i],
                "Report " + i + " at " + speeds[i] + " b/s: expected burst = " + bursts[i]);
        }
        helper.succeed();
    }

    /** Heat runs from 0 at incandescence.minSpeed to full at Max Speed, and goes cold once fast flight ends. */
    @GameTest(template = "empty")
    public static void incandescenceFollowsSpeed(GameTestHelper helper) {
        double min = RsmConfig.INCANDESCENCE_MIN_SPEED.get();
        double max = RsmConfig.MAX_SPEED.get();
        ServerPlayer bearer = flying(bearer(helper, 4.5, 6, 4.5, FACING_EAST), max);
        FlightState state = bearer.getData(ModAttachments.FLIGHT);
        Incandescence.sync(bearer, state);
        helper.assertTrue(state.incandescence == Incandescence.LEVELS, "Not at full heat at Max Speed: " + state.incandescence);

        state.speed = (min + max) / 2.0;
        Incandescence.sync(bearer, state);
        helper.assertTrue(Math.abs(state.incandescence - Incandescence.LEVELS / 2.0) <= 1.0, "Not at half heat halfway: " + state.incandescence);

        state.speed = min;
        Incandescence.sync(bearer, state);
        helper.assertTrue(state.incandescence == 0, "Glowing at the threshold itself: " + state.incandescence);

        state.speed = max;
        Incandescence.sync(bearer, state);
        state.reset();
        Incandescence.sync(bearer, state);
        helper.assertTrue(state.incandescence == 0, "Still hot after fast flight ended: " + state.incandescence);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void clapHitsWhatIsAhead(GameTestHelper helper) {
        floor(helper);
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 4.5, 1, 4.5);
        Attacks.perform(bearer, AttackKind.CLAP);
        helper.assertTrue(zombie.isDeadOrDying(), "Clap missed a zombie three blocks ahead");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void clapStopsAtObsidian(GameTestHelper helper) {
        floor(helper);
        for (int x = 3; x <= 7; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = 2; z <= 6; z++) helper.setBlock(new BlockPos(x, y, z), x == 4 ? Blocks.OBSIDIAN : Blocks.DIRT);
            }
        }
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Attacks.perform(bearer, AttackKind.CLAP);
        int dugInFront = 0;
        for (int y = 1; y <= 3; y++) {
            for (int z = 2; z <= 6; z++) {
                if (helper.getBlockState(new BlockPos(3, y, z)).isAir()) dugInFront++;
                for (int x = 4; x <= 7; x++) {
                    helper.assertTrue(!helper.getBlockState(new BlockPos(x, y, z)).isAir(), "Clap broke through obsidian at " + x + " " + y + " " + z);
                }
            }
        }
        helper.assertTrue(dugInFront > 0, "Clap did not touch the dirt in front of the obsidian");
        helper.succeed();
    }

    // ------------------------------------------------------------------ Grab

    @GameTest(template = "empty")
    public static void grabRules(GameTestHelper helper) {
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 3.5, 1, 4.5);
        helper.assertTrue(Grabs.canGrab(bearer, zombie), "A zombie is not grabbable");
        ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, 5.5F, 1.0F, 4.5F);
        helper.assertTrue(!Grabs.canGrab(bearer, stand), "An armor stand (rsm:ungrabbable) is grabbable");
        Zombie heavy = target(helper, 6.5, 1, 4.5);
        heavy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(RsmConfig.GRAB_MAX_HEALTH.get() + 1.0);
        helper.assertTrue(!Grabs.canGrab(bearer, heavy), "A creature over grab.maxHealth is grabbable");
        helper.assertTrue(!Grabs.canGrab(bearer, player(helper, 3.5, 1, 6.5, FACING_EAST)), "A player is grabbable with grab.players off");

        Grabs.tryGrab(bearer, zombie);
        ServerPlayer thief = bearer(helper, 5.5, 1, 4.5, -FACING_EAST);
        helper.assertTrue(!Grabs.canGrab(thief, zombie), "Someone else's Captive can be stolen");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void grabHoldsInFrontUntilTheCaptiveDies(GameTestHelper helper) {
        floor(helper);
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 3.5, 1, 4.5);
        Grabs.tryGrab(bearer, zombie);
        helper.assertTrue(Grabs.isHolding(bearer) && Grabs.isCaptive(zombie), "Grab missed a zombie two blocks ahead");
        helper.runAfterDelay(2, () -> {
            Vec3 hold = GrabPose.holdPosition(bearer, zombie, null);
            helper.assertTrue(zombie.position().distanceTo(hold) < 0.01, "Captive at " + zombie.position() + ", not at the hold point " + hold);
            zombie.kill();
        });
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(!Grabs.isHolding(bearer), "Still holding a dead Captive");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void grabStopsAtWalls(GameTestHelper helper) {
        floor(helper);
        for (int y = 1; y <= 3; y++) {
            for (int z = 3; z <= 5; z++) helper.setBlock(new BlockPos(3, y, z), Blocks.STONE);
        }
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 4.5, 1, 4.5);
        Grabs.tryGrab(bearer, zombie);
        helper.assertTrue(!Grabs.isHolding(bearer), "Grabbed a zombie through a stone wall");
        helper.succeed();
    }

    /** No suffocation while held inside blocks (an iron golem: no burning in daylight, no AI wandering off). */
    @GameTest(template = "empty")
    public static void captiveIsSafeInsideBlocks(GameTestHelper helper) {
        floor(helper);
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, 3.5F, 1.0F, 4.5F);
        Grabs.tryGrab(bearer, golem);
        for (int x = 2; x <= 4; x++) {
            for (int y = 1; y <= 4; y++) {
                for (int z = 3; z <= 5; z++) helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
            }
        }
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(Grabs.isCaptive(golem), "The Captive was let go");
            helper.assertTrue(golem.getHealth() == golem.getMaxHealth(), "A Captive held inside stone took damage: " + golem.getHealth());
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void thrownCaptiveIsHurtByTheWall(GameTestHelper helper) {
        floor(helper);
        for (int y = 1; y <= 4; y++) {
            for (int z = 2; z <= 6; z++) helper.setBlock(new BlockPos(7, y, z), Blocks.STONE);
        }
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, 3.5F, 1.0F, 4.5F);
        Grabs.tryGrab(bearer, golem);
        Grabs.throwCaptive(bearer);
        helper.assertTrue(!Grabs.isCaptive(golem), "Still held after the Throw");
        float hurt = golem.getMaxHealth() - RsmConfig.THROW_IMPACT_DAMAGE.get().floatValue();
        helper.succeedWhen(() -> helper.assertTrue(golem.getHealth() <= hurt, "The thrown golem has not hit the wall: " + golem.getHealth()));
    }

    @GameTest(template = "empty")
    public static void clapNeedsBothHands(GameTestHelper helper) {
        floor(helper);
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, 3.5F, 1.0F, 4.5F);
        Grabs.tryGrab(bearer, golem);
        Attacks.perform(bearer, AttackKind.CLAP);
        helper.assertTrue(bearer.getData(ModAttachments.ATTACKS).isReady(AttackKind.CLAP, bearer.tickCount), "Clap went on cooldown while holding");
        helper.assertTrue(golem.getHealth() == golem.getMaxHealth(), "Clap hit while holding");
        helper.succeed();
    }

    // Player Captives need grab.players on; their own batch switches it on and back, so no other test sees it.

    private static boolean grabPlayersBefore;

    @BeforeBatch(batch = "grab_players")
    public static void allowGrabbingPlayers(ServerLevel level) {
        grabPlayersBefore = RsmConfig.GRAB_PLAYERS.get();
        RsmConfig.GRAB_PLAYERS.set(true);
    }

    @AfterBatch(batch = "grab_players")
    public static void restoreGrabbingPlayers(ServerLevel level) {
        RsmConfig.GRAB_PLAYERS.set(grabPlayersBefore);
    }

    /** Ten jumps within three seconds free a held player; the ninth does not. Jumps are spaced like real ones. */
    @GameTest(template = "empty", batch = "grab_players")
    public static void heldPlayerBreaksFreeByJumping(GameTestHelper helper) {
        ServerPlayer bearer = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        ServerPlayer captive = player(helper, 3.0, 1, 4.5, -FACING_EAST);
        Grabs.tryGrab(bearer, captive);
        helper.assertTrue(Grabs.isCaptive(captive), "Could not grab a player with grab.players on");
        int jumps = RsmConfig.BREAK_FREE_JUMPS.get();
        for (int i = 0; i < jumps; i++) {
            int jump = i + 1;
            helper.runAfterDelay(3L * i, () -> {
                Grabs.struggle(captive);
                boolean free = !Grabs.isCaptive(captive);
                helper.assertTrue(free == (jump == jumps), "After jump " + jump + " of " + jumps + " free = " + free);
                if (free) helper.succeed();
            });
        }
    }

    /** A held Bearer's attack throws the Holder off: the Grab ends, the attack goes on cooldown, nobody is hurt. */
    @GameTest(template = "empty", batch = "grab_players")
    public static void heldBearerCountersWithAnAttack(GameTestHelper helper) {
        ServerPlayer holder = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        ServerPlayer captive = bearer(helper, 3.0, 1, 4.5, -FACING_EAST);
        Grabs.tryGrab(holder, captive);
        helper.assertTrue(Grabs.isCaptive(captive), "Could not grab a Bearer with grab.players on");
        Attacks.perform(captive, AttackKind.SLASH);
        helper.assertTrue(!Grabs.isCaptive(captive), "The counterattack did not break the Grab");
        helper.assertTrue(!captive.getData(ModAttachments.ATTACKS).isReady(AttackKind.SLASH, captive.tickCount), "The counterattack is not on cooldown");
        helper.assertTrue(holder.getHealth() == holder.getMaxHealth(), "The counterattack hurt the Holder");
        helper.succeed();
    }

    /** A Holder who gets grabbed lets go of their own Captive. */
    @GameTest(template = "empty", batch = "grab_players")
    public static void grabbedHolderLetsGo(GameTestHelper helper) {
        ServerPlayer holder = bearer(helper, 3.5, 1, 4.5, FACING_EAST);
        Zombie zombie = target(helper, 5.5, 1, 4.5);
        Grabs.tryGrab(holder, zombie);
        ServerPlayer other = bearer(helper, 1.5, 1, 4.5, FACING_EAST);
        Grabs.tryGrab(other, holder);
        helper.assertTrue(Grabs.isCaptive(holder), "Could not grab the Holder");
        helper.assertTrue(!Grabs.isCaptive(zombie), "A grabbed Holder kept their own Captive");
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
        ServerPlayer player = player(helper, x, y, z, yaw);
        CuriosApi.getCuriosInventory(player).orElseThrow().setEquippedCurio("ring", 0, new ItemStack(ModItems.RING.get()));
        return player;
    }

    /** Not logged in: a login would make mods (Ad Astra, us) send packets to a client that does not exist. */
    private static ServerPlayer player(GameTestHelper helper, double x, double y, double z, float yaw) {
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            new GameProfile(UUID.randomUUID(), "rsm-test-player"), ClientInformation.createDefault());
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
