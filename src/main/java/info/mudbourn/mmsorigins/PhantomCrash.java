package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.network.PhantomCrashPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * Crash damage for a flying phantom that slams into something, on the elytra's
 * fly-into-wall scale. The holder's client flies the phantom, so it reports the speed lost
 * in the impact and the server turns that into damage.
 */
public final class PhantomCrash {

    private static final double DAMAGE_PER_SPEED = 16.0;
    private static final double SAFE_DAMAGE = 2.0;
    private static final float MAX_SPEED_LOST = 4.0F;

    private PhantomCrash() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(PhantomCrashPayload.TYPE, PhantomCrashPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PhantomCrashPayload.TYPE, (payload, context) ->
            crash(context.player(), payload.speedLost()));
    }

    /** The damage an impact losing this much speed deals; zero or less for a gentle bump. */
    public static double damage(double speedLost) {
        return speedLost * DAMAGE_PER_SPEED - SAFE_DAMAGE;
    }

    private static void crash(ServerPlayer player, float speedLost) {
        if (!Float.isFinite(speedLost) || !MmsOriginsPowers.phantomFormComplete(player)) {
            return;
        }
        float amount = (float) damage(Math.min(speedLost, MAX_SPEED_LOST));
        if (amount > 0.0F) {
            player.hurtServer(player.level(), player.damageSources().flyIntoWall(), amount);
        }
    }
}
