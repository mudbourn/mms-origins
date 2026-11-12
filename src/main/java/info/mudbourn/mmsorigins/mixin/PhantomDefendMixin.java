package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phantom kinship: the flock answers when a bearer is set upon.
 *
 * <p>When something damages a bearer, every phantom within range turns on the
 * attacker, taking it as both target and revenge mark. {@code PhantomKinshipMixin}
 * reads that mark to hold the phantom on the attacker when its own player scan
 * picks the bearer again. Phantoms and fellow bearers are left alone.
 */
@Mixin(Player.class)
public class PhantomDefendMixin {

    @Unique
    private static final double MMS_FLOCK_RANGE = 32.0;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void mmsOrigins$rallyFlock(ServerLevel level, DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!MmsOriginsPowers.PHANTOM_KINSHIP.isActive(player)) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)
            || attacker == player
            || attacker instanceof Phantom
            || MmsOriginsPowers.PHANTOM_KINSHIP.isActive(attacker)) {
            return;
        }
        AABB flock = player.getBoundingBox().inflate(MMS_FLOCK_RANGE);
        for (Phantom phantom : level.getEntitiesOfClass(Phantom.class, flock)) {
            phantom.setLastHurtByMob(attacker);
            phantom.setTarget(attacker);
        }
    }
}
