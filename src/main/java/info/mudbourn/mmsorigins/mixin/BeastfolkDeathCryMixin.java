package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.fur.FurResolver;
import info.mudbourn.mmsorigins.sound.MmsSounds;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sounds a beastfolk's death: the drawn-out howl for a fall in worthy battle, the
 * plain death cry otherwise.
 *
 * <p>The beastfolk sell their strength, so their howl is an acknowledgement of a
 * worthy end, not a reflex on any death. It plays only when a single player dealt
 * the bulk of the killing damage, or when the fatal blow itself was a boss-tier
 * hit; ordinary chip kills and accidents such as falling, drowning, or lava draw
 * the plain death cry instead. The vanilla death sound is already silenced for
 * origin players, and {@code beastfolk_sounds} no longer voices death, so this is
 * the sole source of a beastfolk's death sound.
 */
@Mixin(LivingEntity.class)
public abstract class BeastfolkDeathCryMixin {

    @Unique
    private static final Identifier FOX_TAIL = Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_tail_fox");

    @Inject(method = "die", at = @At("HEAD"))
    private void mmsOrigins$deathCry(DamageSource source, CallbackInfo ci) {
        if (!(((Object) this) instanceof Player player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!MmsOriginsPowers.BEAST_TONGUE.isActive(player)) {
            return;
        }
        SoundEvent cry = mmsOrigins$cry(player, source);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            cry, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static SoundEvent mmsOrigins$cry(Player player, DamageSource source) {
        boolean collared = FurResolver.wearsCollar(player);
        boolean fox = FOX_TAIL.equals(FurResolver.tailOption(player));
        if (mmsOrigins$isWorthyDeath(player, source)) {
            return fox ? MmsSounds.BEASTFOLK_FOX_SCREECH : MmsSounds.BEASTFOLK_DEATH_LONG;
        }
        if (fox) {
            return collared ? MmsSounds.BEASTFOLK_FOX_DEATH : MmsSounds.BEASTFOLK_FOX_COLLARLESS_DEATH;
        }
        return collared ? MmsSounds.BEASTFOLK_COLLAR_DEATH : MmsSounds.BEASTFOLK_DEATH;
    }

    private static boolean mmsOrigins$isWorthyDeath(Player player, DamageSource source) {
        if (mmsOrigins$playerDamageShare(player) >= MmsOriginsPowers.WORTHY_KILL_DAMAGE_SHARE) {
            return true;
        }
        return source.getEntity() instanceof LivingEntity
            && mmsOrigins$fatalBlow(player) >= MmsOriginsPowers.WORTHY_HEAVY_BLOW;
    }

    private static float mmsOrigins$fatalBlow(Player player) {
        List<CombatEntry> entries = ((CombatTrackerAccessor) player.getCombatTracker()).mmsOrigins$getEntries();
        return entries.isEmpty() ? 0.0F : entries.get(entries.size() - 1).damage();
    }

    private static float mmsOrigins$playerDamageShare(Player player) {
        List<CombatEntry> entries = ((CombatTrackerAccessor) player.getCombatTracker()).mmsOrigins$getEntries();
        float total = 0.0F;
        float byPlayers = 0.0F;
        for (CombatEntry entry : entries) {
            total += entry.damage();
            if (entry.source().getEntity() instanceof Player) {
                byPlayers += entry.damage();
            }
        }
        return total <= 0.0F ? 0.0F : byPlayers / total;
    }
}
