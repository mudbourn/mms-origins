package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vicious Appearance: prey animals bolt from a beastfolk and refuse to carry one.
 *
 * <p>Every animal reads a beastfolk as a predator unless its type is in
 * {@code originstweaks:unafraid_of_beastfolk} (undead, aquatic, and predator
 * animals) or it has been tamed. Every half second a scared animal throws off
 * a beastfolk rider, drops its quarry, and paths to open ground away from the
 * nearest beastfolk;
 * once it is beyond the dread's reach its own goals take back over.
 */
@Mixin(Mob.class)
public abstract class ViciousAppearanceMixin {

    @Unique
    private static final TagKey<EntityType<?>> UNAFRAID = TagKey.create(
        Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath("originstweaks", "unafraid_of_beastfolk")
    );

    @Inject(method = "baseTick", at = @At("RETURN"))
    private void mmsOrigins$flee(CallbackInfo ci) {
        if (!((Object) this instanceof Animal mob) || mob.level().isClientSide()) {
            return;
        }
        if ((mob.tickCount + mob.getId()) % 10 != 0 || mob.getType().is(UNAFRAID) || mmsOrigins$isTamed(mob)) {
            return;
        }
        for (Entity passenger : mob.getPassengers()) {
            if (passenger instanceof Player rider && MmsOriginsPowers.VICIOUS_APPEARANCE.isActive(rider)) {
                mob.ejectPassengers();
                break;
            }
        }
        Player predator = mmsOrigins$nearestPredator(mob);
        if (predator == null) {
            return;
        }
        mob.setTarget(null);
        Vec3 away = DefaultRandomPos.getPosAway(mob, 16, 7, predator.position());
        if (away != null) {
            mob.getNavigation().moveTo(away.x, away.y, away.z, 1.4);
        }
    }

    private static boolean mmsOrigins$isTamed(Animal mob) {
        return mob instanceof TamableAnimal tamable && tamable.isTame()
            || mob instanceof AbstractHorse horse && horse.isTamed();
    }

    private static Player mmsOrigins$nearestPredator(PathfinderMob mob) {
        double range = MmsOriginsPowers.VICIOUS_APPEARANCE_RANGE;
        Player nearest = null;
        double nearestSqr = range * range;
        for (Player player : mob.level().players()) {
            double distanceSqr = mob.distanceToSqr(player);
            if (distanceSqr >= nearestSqr) {
                continue;
            }
            if (player.isSpectator() || player.getAbilities().instabuild || !player.isAlive()) {
                continue;
            }
            if (MmsOriginsPowers.VICIOUS_APPEARANCE.isActive(player)) {
                nearest = player;
                nearestSqr = distanceSqr;
            }
        }
        return nearest;
    }
}
