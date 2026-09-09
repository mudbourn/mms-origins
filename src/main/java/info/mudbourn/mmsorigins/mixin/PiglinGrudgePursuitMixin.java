package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.GrudgeHolder;
import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Keeps a grudge-holding piglin locked on until it lands its hit.
 *
 * <p>Vanilla anger fades on a timer, so a kinsman could simply outlast a piglin
 * it wronged. While a grudge stands, this refreshes the piglin's anger at its
 * mark each step, so it keeps coming until it connects; the grudge is dropped if
 * the mark dies, leaves, or is no longer a kinsman it could hold one against.
 */
@Mixin(Mob.class)
public class PiglinGrudgePursuitMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$pursueGrudge(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof AbstractPiglin) || !(mob instanceof GrudgeHolder holder)) {
            return;
        }
        UUID grudge = holder.mmsOrigins$getGrudge();
        if (grudge == null || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        Player mark = level.getPlayerByUUID(grudge);
        if (mark == null || !mark.isAlive()
            || !MmsOriginsPowers.KINSMEN.isActive(mark) || MmsOriginsPowers.ZOMBIFIED.isActive(mark)) {
            holder.mmsOrigins$setGrudge(null);
            return;
        }
        mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, grudge, 200L);
    }
}
