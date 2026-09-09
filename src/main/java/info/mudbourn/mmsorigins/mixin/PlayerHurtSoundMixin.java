package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Silences the vanilla player hurt and death sounds for players who have taken on an origin.
 *
 * <p>Every non-human origin ships a sounds power whose {@code hurt_death} plays a species hurt
 * or death sound when the player is hit, so the vanilla {@code entity.player.hurt} and
 * {@code entity.player.death} would double up over it. Both vanilla sounds funnel through the
 * player overrides of {@code getHurtSound} and {@code getDeathSound}, on the server hurt path
 * and on the client {@code handleDamageEvent}/{@code handleEntityEvent} paths alike, so nulling
 * the two getters covers every source. A human player, or one whose origin layer has not synced,
 * keeps the vanilla sounds.
 */
@Mixin(Player.class)
public abstract class PlayerHurtSoundMixin {

    private static final Identifier HUMAN = Identifier.fromNamespaceAndPath("origins", "human");

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$silenceOriginHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        if (mmsOrigins$hasNonHumanOrigin((Player) (Object) this)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$silenceOriginDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (mmsOrigins$hasNonHumanOrigin((Player) (Object) this)) {
            cir.setReturnValue(null);
        }
    }

    private static boolean mmsOrigins$hasNonHumanOrigin(Player player) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(player).orElse(null);
        if (component == null) {
            return false;
        }
        OriginLayer layer = OriginLayers.getLayer(Identifier.fromNamespaceAndPath("origins", "origin"));
        if (layer == null || !component.hasOrigin(layer)) {
            return false;
        }
        Origin origin = component.getOrigin(layer);
        return origin != null && !HUMAN.equals(origin.getIdentifier());
    }
}
