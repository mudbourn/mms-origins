package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.fur.FurResolver;
import info.mudbourn.mmsorigins.sound.MmsSounds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sounds a beastfolk's death from the pool matching their tail and collar.
 *
 * <p>Each pool carries a rare cry (the howl, or a fox-tailed beastfolk's screech) as
 * a low-weight entry, so the sound engine picks it about one death in a hundred. The
 * hook sits on {@code ServerPlayer}, whose {@code die} replaces the living-entity one
 * outright. The vanilla death sound is already silenced for origin players, and
 * {@code beastfolk_sounds} does not voice death, so this is the sole source of a
 * beastfolk's death sound.
 */
@Mixin(ServerPlayer.class)
public abstract class BeastfolkDeathCryMixin {

    @Unique
    private static final Identifier FOX_TAIL = Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_tail_fox");

    @Inject(method = "die", at = @At("HEAD"))
    private void mmsOrigins$deathCry(DamageSource source, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (!MmsOriginsPowers.BEAST_TONGUE.isActive(player)) {
            return;
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            mmsOrigins$cry(player), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Unique
    private static SoundEvent mmsOrigins$cry(ServerPlayer player) {
        boolean collared = FurResolver.wearsCollar(player);
        if (FOX_TAIL.equals(FurResolver.tailOption(player))) {
            return collared ? MmsSounds.BEASTFOLK_FOX_DEATH : MmsSounds.BEASTFOLK_FOX_COLLARLESS_DEATH;
        }
        return collared ? MmsSounds.BEASTFOLK_COLLAR_DEATH : MmsSounds.BEASTFOLK_DEATH;
    }
}
