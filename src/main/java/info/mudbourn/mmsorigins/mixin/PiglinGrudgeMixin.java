package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.GrudgeHolder;
import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
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
 */
@Mixin(LivingEntity.class)
public class PiglinGrudgeMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void mmsOrigins$trackGrudge(ServerLevel level, DamageSource source, float amount,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        Entity aggressor = source.getEntity();
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
}
