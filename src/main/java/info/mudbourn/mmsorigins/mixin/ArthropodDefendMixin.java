package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Arthropod Kinship: the swarm answers when an arachnid is set upon.
 *
 * <p>When something damages a bearer, every arthropod within range turns on the
 * attacker, taking it as both target and revenge mark so neutral kinds such as
 * bees commit to the fight. Arthropod attackers are left alone.
 */
@Mixin(Player.class)
public class ArthropodDefendMixin {

    @Unique
    private static final double MMS_SWARM_RANGE = 10.0;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void mmsOrigins$rallySwarm(ServerLevel level, DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!MmsOriginsPowers.ARTHROPOD_KINSHIP.isActive(player)) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)
            || attacker == player
            || attacker.getType().is(EntityTypeTags.ARTHROPOD)) {
            return;
        }
        AABB swarm = player.getBoundingBox().inflate(MMS_SWARM_RANGE);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, swarm, mob -> mob.getType().is(EntityTypeTags.ARTHROPOD))) {
            mob.setLastHurtByMob(attacker);
            mob.setTarget(attacker);
        }
    }
}
