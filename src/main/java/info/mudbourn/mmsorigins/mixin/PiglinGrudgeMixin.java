package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.GrudgeHolder;
import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
 * Drives the piglin grudge: a struck piglin holds one until it hits back.
 *
 * <p>When a kinsman lands a blow on a piglin, that one piglin marks the kinsman
 * as owed a hit; it then pursues until it connects, at which point the score is
 * settled and the grudge is dropped. Everything else, spreading and lifting the
 * neutrality, follows from that one flag.
 *
 * <p>A zombified kinsman holds no private grudge; instead a blow they land turns
 * the whole nearby pack, piglins and brutes alike, angry at once, as striking a
 * zombified piglin enrages every one in sight.
 */
@Mixin(LivingEntity.class)
public class PiglinGrudgeMixin {

    @Unique
    private static final double MMS_ZOMBIE_RALLY_RANGE = 16.0;

    @Unique
    private static final long MMS_ZOMBIE_RALLY_TICKS = 600L;

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void mmsOrigins$trackGrudge(ServerLevel level, DamageSource source, float amount,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        Entity aggressor = source.getEntity();
        if (self instanceof AbstractPiglin && aggressor instanceof Player striker
            && MmsOriginsPowers.ZOMBIFIED.isActive(striker)) {
            mmsOrigins$enrageNearby(level, self, striker);
            return;
        }
        if (self instanceof AbstractPiglin && self instanceof GrudgeHolder grudged
            && aggressor instanceof Player player
            && MmsOriginsPowers.KINSMEN.isActive(player) && !MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            grudged.mmsOrigins$setGrudge(player.getUUID());
            return;
        }
        if (self instanceof Player victim && aggressor instanceof GrudgeHolder settler
            && victim.getUUID().equals(settler.mmsOrigins$getGrudge())) {
            settler.mmsOrigins$setGrudge(null);
        }
    }

    // Turns every piglin and brute around the struck one angry at the zombified striker.
    @Unique
    private static void mmsOrigins$enrageNearby(ServerLevel level, LivingEntity struck, Player striker) {
        AABB muster = struck.getBoundingBox().inflate(MMS_ZOMBIE_RALLY_RANGE);
        for (AbstractPiglin piglin : level.getEntitiesOfClass(AbstractPiglin.class, muster)) {
            piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, striker.getUUID(), MMS_ZOMBIE_RALLY_TICKS);
        }
    }
}
