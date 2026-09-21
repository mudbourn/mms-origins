package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.client.ZombieShakeState;
import info.mudbourn.mmsorigins.client.fur.FurState;
import info.mudbourn.mmsorigins.client.wings.ButterflyFlap;
import info.mudbourn.mmsorigins.client.wings.WingState;
import info.mudbourn.mmsorigins.fur.BodyFurAttachment;
import info.mudbourn.mmsorigins.fur.BodyWingAttachment;
import info.mudbourn.mmsorigins.fur.FurResolver;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reads the avatar's origin during extraction and stamps its fur, wing, and zombie
 * state onto the render state.
 *
 * <p>Players carry the origin on an Apoli component the fur layer never sees; a logout
 * mannequin has no such component, so it falls back to the synced fur attachment
 * mms-combat's body carries. Everything is null-guarded so an origin-less avatar simply
 * carries no fur rather than crashing the entity render.
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("RETURN"))
    private void mmsOrigins$stampOrigin(Avatar player,
                                        AvatarRenderState state,
                                        float partialTick,
                                        CallbackInfo ci) {
        Identifier fur = FurResolver.resolve(player);
        if (fur == null && player instanceof Mannequin) {
            fur = player.getAttachedOrElse(BodyFurAttachment.TYPE, null);
        }
        ((FurState) state).mmsOrigins$setFurOrigin(fur);
        Identifier wing = FurResolver.wingOption(player);
        if (wing == null && player instanceof Mannequin) {
            wing = player.getAttachedOrElse(BodyWingAttachment.TYPE, null);
        }
        ((WingState) state).mmsOrigins$setWingOption(wing);
        ((ZombieShakeState) state).mmsOrigins$setZombieShaking(mmsOrigins$zombieShaking(player));
        if (player instanceof Player concrete) {
            ((WingState) state).mmsOrigins$setWingFlapDegrees(
                    ButterflyFlap.degreesFor(concrete, partialTick));
        } else {
            ((WingState) state).mmsOrigins$setWingFlapDegrees(
                    ButterflyFlap.idleDegrees(state.ageInTicks));
        }
    }

    /** Whether the player's zombification has crept past the halfway mark. */
    private static boolean mmsOrigins$zombieShaking(Avatar player) {
        Power power = MmsOriginsPowers.ZOMBIE_METER.get(player);
        return power instanceof VariableIntPower meter
                && meter.getValue() >= MmsOriginsPowers.ZOMBIE_SHAKE_THRESHOLD;
    }
}
