package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.PhantomCrash;
import info.mudbourn.mmsorigins.network.PhantomCrashPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

/**
 * Winged flight for the local player in phantom form. It replaces vanilla creative flight
 * with momentum: thrust eases the velocity toward where the player looks, and with no
 * thrust the body glides on, slowly turning its heading toward the look direction. Top
 * speed follows the flying speed that {@link FlightSurge} drives, so the cruise and surge
 * curve is unchanged. Braking sheds speed gradually, and slamming into something hurts
 * through {@link PhantomCrash}.
 */
public final class PhantomWings {

    private static final double SPEED_PER_FLYING_SPEED = 1.0 / 0.09;
    private static final double THRUST_GAIN = 0.07;
    private static final double STRAFE_SHARE = 0.25;
    private static final double CLIMB_SHARE = 0.6;
    private static final double GLIDE_DRAG = 0.985;
    private static final double GLIDE_STEER = 0.05;
    private static final double BRAKE_DRAG = 0.96;
    private static final double REST_SPEED = 0.01;
    private static final double IMPULSE_THRESHOLD = 0.4;

    private static Vec3 velocity = Vec3.ZERO;
    private static Vec3 lastSet = Vec3.ZERO;
    private static boolean winged;

    private PhantomWings() {
    }

    /** Runs one tick of winged flight, returning whether it replaced vanilla travel. */
    public static boolean travel(LocalPlayer player, Vec3 input) {
        Abilities abilities = player.getAbilities();
        if (!abilities.mayfly || player.isPassenger() || !MmsOriginsPowers.phantomFormComplete(player)) {
            winged = false;
            return false;
        }
        if (!abilities.flying) {
            abilities.flying = true;
            player.onUpdateAbilities();
        }
        Vec3 current = player.getDeltaMovement();
        if (!winged || current.subtract(lastSet).length() > IMPULSE_THRESHOLD) {
            velocity = current;
            winged = true;
        }
        double limit = abilities.getFlyingSpeed() * (surging(player) ? 2.0 : 1.0) * SPEED_PER_FLYING_SPEED;
        velocity = steer(player, input, limit);
        double before = velocity.length();
        player.setDeltaMovement(velocity);
        player.move(MoverType.SELF, velocity);
        velocity = player.getDeltaMovement();
        if (player.horizontalCollision || player.verticalCollision) {
            crash(before - velocity.length());
        }
        lastSet = velocity;
        player.resetFallDistance();
        player.calculateEntityAnimation(true);
        return true;
    }

    private static Vec3 steer(LocalPlayer player, Vec3 input, double limit) {
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
        Input keys = player.input.keyPresses;
        double forward = Math.clamp(input.z, -1.0, 1.0);
        double strafe = Math.clamp(input.x, -1.0, 1.0);
        double climb = (keys.jump() ? 1.0 : 0.0) - (keys.shift() ? 1.0 : 0.0);
        Vec3 wish = right.scale(-strafe * STRAFE_SHARE).add(0.0, climb * CLIMB_SHARE, 0.0);
        if (forward > 0.0) {
            wish = wish.add(look.scale(forward));
        }
        Vec3 next = velocity;
        if (wish.lengthSqr() > 1.0E-4) {
            if (wish.lengthSqr() > 1.0) {
                wish = wish.normalize();
            }
            next = next.lerp(wish.scale(limit), THRUST_GAIN);
        } else {
            next = glide(next, look);
        }
        return forward < 0.0 ? next.scale(BRAKE_DRAG) : next;
    }

    /** Reports an impact hard enough to hurt, so the server deals the crash damage. */
    private static void crash(double speedLost) {
        if (PhantomCrash.damage(speedLost) > 0.0) {
            ClientPlayNetworking.send(new PhantomCrashPayload((float) speedLost));
        }
    }

    private static boolean surging(LocalPlayer player) {
        return player.isSprinting() || player.input.keyPresses.sprint();
    }

    private static Vec3 glide(Vec3 velocity, Vec3 look) {
        double speed = velocity.length();
        if (speed < REST_SPEED) {
            return Vec3.ZERO;
        }
        Vec3 heading = velocity.scale(1.0 / speed).lerp(look, GLIDE_STEER).normalize();
        return heading.scale(speed * GLIDE_DRAG);
    }
}
