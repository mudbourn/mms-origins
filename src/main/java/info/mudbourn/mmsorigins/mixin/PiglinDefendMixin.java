package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.PiglinKinship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Kinsmen: the pack answers when one of their own is set upon.
 *
 * <p>When something strikes a kinsman, any nearby piglin that still counts them as
 * kin, one that has not exiled them or turned traitor, rounds on the attacker.
 * A piglin already angry at the kinsman is not sparing them, so it is left out of
 * the muster and keeps its own quarrel.
 */
@Mixin(Player.class)
public class PiglinDefendMixin {

    @Unique
    private static final double MMS_RALLY_RANGE = 16.0;

    @Unique
    private static final long MMS_RALLY_TICKS = 600L;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void mmsOrigins$rallyKin(ServerLevel level, DamageSource source, float amount,
                                     CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!MmsOriginsPowers.KINSMEN.isActive(player) || MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)
            || attacker == player
            || attacker instanceof AbstractPiglin) {
            return;
        }
        AABB muster = player.getBoundingBox().inflate(MMS_RALLY_RANGE);
        for (AbstractPiglin piglin : level.getEntitiesOfClass(AbstractPiglin.class, muster)) {
            if (PiglinKinship.isSpared(piglin, player)) {
                piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, attacker.getUUID(), MMS_RALLY_TICKS);
            }
        }
    }
}
