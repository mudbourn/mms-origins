package info.mudbourn.mmsorigins.client;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;

/**
 * Smooths a phantom-form body's heading, pitch and bank across frames. In the air the body
 * follows the way the avatar is moving and banks into turns; on the ground it faces the
 * avatar's body. Angles are in the vanilla phantom's convention, where a positive pitch
 * lifts the nose.
 */
public final class PhantomFlightPose {

    /** A phantom body's orientation in degrees; a positive roll banks to the right. */
    public record Angles(float yaw, float pitch, float roll) {
    }

    private static final float MIN_FLIGHT_SPEED = 0.05F;
    private static final float FULL_FLIGHT_SPEED = 0.3F;
    private static final float HOVER_PITCH_SHARE = 0.5F;
    private static final float YAW_SETTLE_TICKS = 3.0F;
    private static final float PITCH_SETTLE_TICKS = 4.0F;
    private static final float ROLL_SETTLE_TICKS = 4.0F;
    private static final float TURN_RATE_SETTLE_TICKS = 4.0F;
    private static final float ROLL_PER_TURN_DEGREE = 6.0F;
    private static final float MAX_ROLL = 40.0F;
    private static final float STALE_TICKS = 20.0F;

    private static final Map<Avatar, Track> TRACKS = new WeakHashMap<>();

    private PhantomFlightPose() {
    }

    /** The body orientation to draw for the avatar at the given partial tick. */
    public static Angles of(Avatar avatar, float partialTick, float bodyYaw) {
        float now = avatar.tickCount + partialTick;
        Track track = TRACKS.get(avatar);
        if (track == null || now - track.time > STALE_TICKS || now < track.time) {
            track = new Track(now, bodyYaw);
            TRACKS.put(avatar, track);
        }
        float dt = now - track.time;
        if (dt > 0.0F) {
            advance(track, avatar, partialTick, bodyYaw, dt);
            track.time = now;
        }
        return new Angles(track.yaw, track.pitch, track.roll);
    }

    private static void advance(Track track, Avatar avatar, float partialTick, float bodyYaw, float dt) {
        float targetYaw = bodyYaw;
        float targetPitch = 0.0F;
        if (!avatar.onGround()) {
            double vx = avatar.getX() - avatar.xo;
            double vy = avatar.getY() - avatar.yo;
            double vz = avatar.getZ() - avatar.zo;
            double flat = Math.sqrt(vx * vx + vz * vz);
            float speed = (float) Math.sqrt(flat * flat + vy * vy);
            float share = Mth.clamp((speed - MIN_FLIGHT_SPEED) / (FULL_FLIGHT_SPEED - MIN_FLIGHT_SPEED), 0.0F, 1.0F);
            float lookYaw = Mth.rotLerp(partialTick, avatar.yRotO, avatar.getYRot());
            float velocityYaw = (float) (Mth.atan2(-vx, vz) * Mth.RAD_TO_DEG);
            targetYaw = lookYaw + Mth.wrapDegrees(velocityYaw - lookYaw) * share;
            float lookPitch = -Mth.lerp(partialTick, avatar.xRotO, avatar.getXRot()) * HOVER_PITCH_SHARE;
            float velocityPitch = (float) (Mth.atan2(vy, flat) * Mth.RAD_TO_DEG);
            targetPitch = Mth.lerp(share, lookPitch, velocityPitch);
        }
        float yawStep = Mth.wrapDegrees(targetYaw - track.yaw) * settle(dt, YAW_SETTLE_TICKS);
        track.yaw += yawStep;
        track.pitch += (targetPitch - track.pitch) * settle(dt, PITCH_SETTLE_TICKS);
        track.turnRate += (yawStep / dt - track.turnRate) * settle(dt, TURN_RATE_SETTLE_TICKS);
        float targetRoll = avatar.onGround()
            ? 0.0F
            : Mth.clamp(track.turnRate * ROLL_PER_TURN_DEGREE, -MAX_ROLL, MAX_ROLL);
        track.roll += (targetRoll - track.roll) * settle(dt, ROLL_SETTLE_TICKS);
    }

    private static float settle(float dt, float ticks) {
        return 1.0F - (float) Math.exp(-dt / ticks);
    }

    private static final class Track {
        float time;
        float yaw;
        float pitch;
        float roll;
        float turnRate;

        Track(float time, float yaw) {
            this.time = time;
            this.yaw = yaw;
        }
    }
}
