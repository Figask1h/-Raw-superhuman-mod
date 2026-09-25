package dev.ashu.rsm.power;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Which blocks Breakthrough and Clap may destroy. Impervious Blocks never break; everything else breaks only
 * where the Bearer could break it by hand (claims, spawn protection, adventure mode) and while the server
 * config allows destruction at all.
 */
public final class BlockDestruction {
    /** Datapack additions to the Impervious Blocks. */
    public static final TagKey<Block> IMPERVIOUS = TagKey.create(Registries.BLOCK, RawSuperhumanMod.id("impervious"));
    /** Datapack exceptions: breakable even though the rules below would make them impervious. */
    public static final TagKey<Block> BREAKABLE = TagKey.create(Registries.BLOCK, RawSuperhumanMod.id("breakable"));

    // The block-level value (set through Block.Properties). The state-aware NeoForge variant wants an Explosion we do not have.
    @SuppressWarnings("deprecation")
    private static final float OBSIDIAN_BLAST_RESISTANCE = Blocks.OBSIDIAN.getExplosionResistance();

    /** Works on both sides: the client uses it to predict a tunnel, the server to decide. */
    @SuppressWarnings("deprecation")
    public static boolean isImpervious(Level level, BlockPos pos, BlockState state) {
        if (state.is(IMPERVIOUS)) return true;
        if (state.is(BREAKABLE)) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return true;
        if (state.getBlock().getExplosionResistance() >= OBSIDIAN_BLAST_RESISTANCE) return true;
        return holdsItems(level, pos, state);
    }

    /** Chests, furnaces, shulker boxes, modded storage: breaking them would spill their contents. */
    private static boolean holdsItems(Level level, BlockPos pos, BlockState state) {
        if (!state.hasBlockEntity()) return false;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container) return true;
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, blockEntity, null) != null;
    }

    /** The server's final word for one block, as if the Bearer broke it by hand (so claim mods can refuse). */
    public static boolean mayDestroy(ServerPlayer player, BlockPos pos, BlockState state) {
        ServerLevel level = player.serverLevel();
        if (!RsmConfig.DESTROY_BLOCKS.get() || isImpervious(level, pos, state)) return false;
        if (!level.mayInteract(player, pos) || player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) return false;
        return !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled();
    }

    private BlockDestruction() {}
}
