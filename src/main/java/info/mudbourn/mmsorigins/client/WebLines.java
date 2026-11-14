package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.network.WebLinePayload;
import info.mudbourn.mmsorigins.network.WebRopePayload;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Client record of every arachnid web line in view, fed only when a line changes. Each tick
 * every line is redrawn from short-lived particles between its owner's hand and its anchor,
 * so it follows both like a fishing line and sags while the rope is slack. Lines too far
 * from the camera are skipped.
 */
public final class WebLines {

    private static final Map<Integer, WebLinePayload> LINES = new HashMap<>();
    private static final int LINE_COLOR = 0xF2F2F2;
    private static final float LINE_SCALE = 0.35F;
    private static final double LINE_SPACING = 0.3;
    private static final int LINE_MAX_POINTS = 240;
    private static final int LINE_LIFETIME = 2;
    private static final double MAX_DRAW_DISTANCE = 96.0;
    private static final double HAND_FORWARD = 0.5;
    private static final double HAND_SIDE = 0.35;
    private static final double HAND_DROP = 0.4;
    private static final double BODY_SIDE = 0.35;
    private static final double BODY_DROP = 0.6;

    private WebLines() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(WebLinePayload.TYPE, (payload, context) -> {
            Minecraft client = context.client();
            if (client.player != null && payload.ownerId() == client.player.getId()) {
                WebSwing.receive(client, payload);
            }
            if (payload.attached()) {
                LINES.put(payload.ownerId(), payload);
            } else {
                LINES.remove(payload.ownerId());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(WebRopePayload.TYPE, (payload, context) -> {
            WebLinePayload line = LINES.get(payload.ownerId());
            if (line != null) {
                LINES.put(payload.ownerId(), line.withRope(payload.rope()));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> LINES.clear());
    }

    public static void tick(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) {
            LINES.clear();
            return;
        }
        if (LINES.isEmpty() || client.isPaused()) {
            return;
        }
        Vec3 camera = client.gameRenderer.getMainCamera().position();
        Iterator<Map.Entry<Integer, WebLinePayload>> lines = LINES.entrySet().iterator();
        while (lines.hasNext()) {
            WebLinePayload line = lines.next().getValue();
            if (!(level.getEntity(line.ownerId()) instanceof Player owner) || !owner.isAlive()) {
                lines.remove();
                continue;
            }
            Vec3 to = anchor(level, line);
            if (to == null) {
                continue;
            }
            Vec3 from = hand(client, owner);
            if (from.distanceTo(camera) > MAX_DRAW_DISTANCE && to.distanceTo(camera) > MAX_DRAW_DISTANCE) {
                continue;
            }
            draw(client, from, to, rope(client, owner, line));
        }
    }

    private static Vec3 anchor(ClientLevel level, WebLinePayload line) {
        if (line.entityId() == WebLinePayload.NO_ENTITY) {
            return new Vec3(line.x(), line.y(), line.z());
        }
        Entity target = level.getEntity(line.entityId());
        return target == null ? null : target.getEyePosition();
    }

    private static double rope(Minecraft client, Player owner, WebLinePayload line) {
        boolean ownBlockLine = owner == client.player && line.entityId() == WebLinePayload.NO_ENTITY;
        return ownBlockLine ? WebSwing.rope() : line.rope();
    }

    private static void draw(Minecraft client, Vec3 from, Vec3 to, double rope) {
        Vec3 span = to.subtract(from);
        double dist = span.length();
        double sag = rope > dist ? Math.sqrt(rope * rope - dist * dist) * 0.5 : 0.0;
        int points = (int) Mth.clamp(dist / LINE_SPACING, 1, LINE_MAX_POINTS);
        DustParticleOptions dust = new DustParticleOptions(LINE_COLOR, LINE_SCALE);
        for (int i = 0; i <= points; i++) {
            double t = i / (double) points;
            Vec3 at = from.add(span.scale(t)).subtract(0.0, 4.0 * t * (1.0 - t) * sag, 0.0);
            Particle particle = client.particleEngine.createParticle(dust, at.x, at.y, at.z, 0.0, 0.0, 0.0);
            if (particle != null) {
                particle.setLifetime(LINE_LIFETIME);
            }
        }
    }

    private static Vec3 hand(Minecraft client, Player owner) {
        Vec3 eye = owner.getEyePosition();
        double side = owner.getMainArm() == HumanoidArm.RIGHT ? 1.0 : -1.0;
        if (owner == client.player && client.options.getCameraType().isFirstPerson()) {
            Vec3 view = owner.getViewVector(1.0F);
            Vec3 right = view.cross(new Vec3(0.0, 1.0, 0.0));
            if (right.lengthSqr() < 1.0E-6) {
                right = new Vec3(1.0, 0.0, 0.0);
            }
            return eye.add(view.scale(HAND_FORWARD))
                .add(right.normalize().scale(side * HAND_SIDE))
                .add(0.0, -HAND_DROP, 0.0);
        }
        float yaw = owner.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
        return eye.add(right.scale(side * BODY_SIDE)).add(0.0, -BODY_DROP, 0.0);
    }
}
