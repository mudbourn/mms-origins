package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.network.WebFirePayload;
import info.mudbourn.mmsorigins.network.WebLinePayload;
import info.mudbourn.mmsorigins.network.WebReelPayload;
import info.mudbourn.mmsorigins.network.WebRopePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The arachnid's web swing, run on the client that owns the player's movement. While the
 * web holds a block it acts as a rope: gravity and momentum carry the player, and the rope
 * stops them drifting past its length, so they swing. Holding use with an empty hand reels
 * the rope in without cancelling the swing, and the reel speeds up after a while; crouching
 * pays the rope out instead. Letting
 * go of the web while reeling flings the player onward and upward.
 *
 * <p>Rope length changes go to the server only in steps and at most every few ticks, so
 * other players' copies of the line sag to match without a packet every tick.
 */
public final class WebSwing {

    private static final double MIN_ROPE = 1.0;
    private static final double TAUT_PULL = 0.3;
    private static final double MAX_TAUT_CORRECTION = 1.0;
    private static final double PUMP = 0.035;
    private static final double EXTEND_SPEED = 0.35;
    private static final double FLING_LIFT = 0.35;
    private static final double FLING_BONUS_LIFT = 0.45;
    private static final double FLING_SPEED_SCALE = 1.15;
    private static final int FLING_FULL_TICKS = 50;
    private static final double ROPE_SEND_STEP = 0.5;
    private static final int ROPE_SEND_TICKS = 4;
    private static final double ROPE_SETTLE_STEP = 0.05;
    private static final int ROPE_SETTLE_TICKS = 20;
    private static final String WEB_KEY = "key.origins.primary_active";

    private static Vec3 anchor;
    private static boolean holding;
    private static double rope;
    private static float reelSpeed;
    private static float maxReelSpeed;
    private static int rampDelay;
    private static int rampTicks;
    private static double maxRope;
    private static int reelTicks;
    private static boolean sentReeling;
    private static double sentRope;
    private static int ticksSinceRopeSent;
    private static KeyMapping webKey;
    private static boolean webKeyWasDown;

    private WebSwing() {
    }

    /** The local rope's current length, or a negative value when no block web is held. */
    public static double rope() {
        return anchor == null ? -1.0 : rope;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !player.isAlive() || player.isSpectator()) {
            anchor = null;
            holding = false;
            reelTicks = 0;
            sentReeling = false;
            return;
        }
        reportFreshPress(client);
        boolean reeling = holding
            && client.options.keyUse.isDown()
            && player.getMainHandItem().isEmpty();
        reportReeling(reeling);
        if (anchor == null) {
            return;
        }
        Vec3 toAnchor = anchor.subtract(player.getEyePosition());
        double dist = toAnchor.length();
        if (dist < 1.0E-4) {
            return;
        }
        Vec3 dir = toAnchor.scale(1.0 / dist);
        Vec3 velocity = player.getDeltaMovement();
        double inward = velocity.dot(dir);
        if (reeling) {
            reelTicks++;
            double speed = currentReelSpeed();
            if (inward < speed) {
                velocity = velocity.add(dir.scale(speed - inward));
            }
            rope = Math.max(MIN_ROPE, Math.min(rope, dist - speed));
        } else {
            reelTicks = 0;
            if (client.options.keyShift.isDown()) {
                rope = Math.min(maxRope, Math.max(rope, dist) + EXTEND_SPEED);
            }
            if (dist > rope) {
                if (inward < 0.0) {
                    velocity = velocity.subtract(dir.scale(inward));
                }
                double stretch = Math.min(dist - rope, MAX_TAUT_CORRECTION);
                velocity = velocity.add(dir.scale(stretch * TAUT_PULL));
                velocity = velocity.add(pump(client, player, dir));
            }
        }
        player.setDeltaMovement(velocity);
        player.resetFallDistance();
        reportRope();
    }


    /** Takes a line change for the local player's own web. */
    public static void receive(Minecraft client, WebLinePayload payload) {
        LocalPlayer player = client.player;
        holding = payload.attached();
        if (!payload.attached()) {
            if (anchor != null && player != null && reelTicks > 0) {
                fling(player);
            }
            anchor = null;
            reelTicks = 0;
            return;
        }
        if (payload.entityId() != WebLinePayload.NO_ENTITY) {
            anchor = null;
            return;
        }
        anchor = new Vec3(payload.x(), payload.y(), payload.z());
        reelSpeed = payload.reelSpeed();
        maxReelSpeed = payload.maxReelSpeed();
        rampDelay = payload.rampDelay();
        rampTicks = Math.max(1, payload.rampTicks());
        maxRope = payload.maxRope();
        reelTicks = 0;
        rope = Math.max(MIN_ROPE, payload.rope());
        sentRope = rope;
        ticksSinceRopeSent = 0;
    }

    private static double currentReelSpeed() {
        if (reelTicks <= rampDelay) {
            return reelSpeed;
        }
        double t = Mth.clamp((reelTicks - rampDelay) / (double) rampTicks, 0.0, 1.0);
        double eased = t * t * (3.0 - 2.0 * t);
        return Mth.lerp(eased, reelSpeed, maxReelSpeed);
    }

    private static Vec3 pump(Minecraft client, LocalPlayer player, Vec3 dir) {
        if (!client.options.keyUp.isDown()) {
            return Vec3.ZERO;
        }
        Vec3 look = player.getViewVector(1.0F);
        Vec3 flat = new Vec3(look.x, 0.0, look.z);
        if (flat.lengthSqr() < 1.0E-4) {
            return Vec3.ZERO;
        }
        Vec3 push = flat.normalize();
        Vec3 tangent = push.subtract(dir.scale(push.dot(dir)));
        return tangent.scale(PUMP);
    }

    private static void fling(LocalPlayer player) {
        double charge = Math.min(1.0, reelTicks / (double) FLING_FULL_TICKS);
        Vec3 velocity = player.getDeltaMovement();
        Vec3 flung = new Vec3(
            velocity.x * FLING_SPEED_SCALE,
            Math.max(velocity.y, 0.0) + FLING_LIFT + FLING_BONUS_LIFT * charge,
            velocity.z * FLING_SPEED_SCALE);
        player.setDeltaMovement(flung);
    }

    private static void reportFreshPress(Minecraft client) {
        if (webKey == null) {
            for (KeyMapping mapping : client.options.keyMappings) {
                if (WEB_KEY.equals(mapping.getName())) {
                    webKey = mapping;
                }
            }
            if (webKey == null) {
                return;
            }
        }
        boolean down = webKey.isDown();
        if (down && !webKeyWasDown && ClientPlayNetworking.canSend(WebFirePayload.TYPE)) {
            ClientPlayNetworking.send(WebFirePayload.INSTANCE);
        }
        webKeyWasDown = down;
    }

    private static void reportRope() {
        ticksSinceRopeSent++;
        double change = Math.abs(rope - sentRope);
        boolean due = change >= ROPE_SEND_STEP && ticksSinceRopeSent >= ROPE_SEND_TICKS;
        boolean settle = change > ROPE_SETTLE_STEP && ticksSinceRopeSent >= ROPE_SETTLE_TICKS;
        if ((due || settle) && ClientPlayNetworking.canSend(WebRopePayload.TYPE)) {
            sentRope = rope;
            ticksSinceRopeSent = 0;
            ClientPlayNetworking.send(new WebRopePayload(0, (float) rope));
        }
    }

    private static void reportReeling(boolean reeling) {
        if (reeling != sentReeling && ClientPlayNetworking.canSend(WebReelPayload.TYPE)) {
            sentReeling = reeling;
            ClientPlayNetworking.send(new WebReelPayload(reeling));
        }
    }
}
