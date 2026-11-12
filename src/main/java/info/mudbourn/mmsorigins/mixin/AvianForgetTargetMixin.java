package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Avian Peace: a hunter other than a phantom or boss already locked onto a bird-folk bearer drops it.
 *
 * <p>Blocking fresh acquisition is not enough for goal-driven hunters whose
 * {@code canContinueToUse} keeps an existing target without re-running the
 * targeting test. So after each server AI step an {@link Enemy} that is not a
 * phantom or boss clears any target that is a spared avian, unless the bearer has struck
 * it, in which case its revenge keeps it locked on.
 */
@Mixin(Mob.class)
public class AvianForgetTargetMixin {

    private static final TagKey<EntityType<?>> BOSSES =
        TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", "bosses"));

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$forgetAvian(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof Enemy) || mob instanceof Phantom || mob.getType().is(BOSSES)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (!(target instanceof Player player) || mob.getLastHurtByMob() == target) {
            return;
        }
        if (MmsOriginsPowers.AVIAN_PEACE.isActive(player)) {
            mob.setTarget(null);
        }
    }
}
