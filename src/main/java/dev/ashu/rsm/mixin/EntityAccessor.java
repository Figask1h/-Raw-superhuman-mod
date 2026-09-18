package dev.ashu.rsm.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    /** Shared flag 7 is "fall flying" (elytra): it drives the horizontal body pose in the renderer. */
    @Invoker("setSharedFlag")
    void rsm$setSharedFlag(int flag, boolean set);
}
