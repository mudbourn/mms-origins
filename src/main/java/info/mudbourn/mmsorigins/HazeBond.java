package info.mudbourn.mmsorigins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * The persistent link between a mob and the truffles whose spore haze reached it.
 *
 * <p>A mob holds at most one active master, whose soldier it is until {@code until}
 * game time, and a list of truffles it is permanently neutral to. When the service
 * ends the master moves into the neutral list. A truffle that strikes a neutral mob
 * is dropped from that list, so the mob may turn on them again.
 */
public final class HazeBond {

    public static final int LOYAL_TICKS = 600;

    public static final AttachmentType<State> TYPE = AttachmentRegistry.<State>builder()
        .persistent(State.CODEC)
        .buildAndRegister(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "haze_bond"));

    private HazeBond() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damage, blocked) -> {
            if (entity instanceof Mob mob && source.getEntity() instanceof ServerPlayer player) {
                provoke(mob, player.getUUID());
            }
        });
    }

    public static State of(Mob mob) {
        State state = mob.getAttached(TYPE);
        return state == null ? State.EMPTY : state;
    }

    public static boolean hasBond(Mob mob) {
        return mob.hasAttached(TYPE);
    }

    public static UUID activeMaster(Mob mob) {
        State state = of(mob);
        if (state.master().isEmpty() || mob.level().getGameTime() >= state.until()) {
            return null;
        }
        return state.master().get();
    }

    public static boolean isSoldierOf(Mob mob, UUID owner) {
        return owner.equals(activeMaster(mob));
    }

    public static void enlist(Mob mob, UUID owner) {
        State state = of(mob);
        mob.setAttached(TYPE, new State(state.neutral(), Optional.of(owner), mob.level().getGameTime() + LOYAL_TICKS));
    }

    public static void retire(Mob mob) {
        State state = of(mob);
        if (state.master().isEmpty()) {
            return;
        }
        List<UUID> neutral = new ArrayList<>(state.neutral());
        UUID master = state.master().get();
        if (!neutral.contains(master)) {
            neutral.add(master);
        }
        mob.setAttached(TYPE, new State(neutral, Optional.empty(), 0L));
    }

    public static void provoke(Mob mob, UUID player) {
        State state = of(mob);
        if (!state.neutral().contains(player)) {
            return;
        }
        List<UUID> neutral = new ArrayList<>(state.neutral());
        neutral.remove(player);
        mob.setAttached(TYPE, new State(neutral, state.master(), state.until()));
    }

    public static boolean isNeutralTo(Mob mob, UUID player) {
        return of(mob).neutral().contains(player);
    }

    public static boolean spares(Mob mob, LivingEntity target) {
        if (!hasBond(mob)) {
            return false;
        }
        if (target instanceof Player && isNeutralTo(mob, target.getUUID())) {
            return true;
        }
        UUID master = activeMaster(mob);
        if (master == null) {
            return false;
        }
        return master.equals(target.getUUID()) || target instanceof Mob other && isSoldierOf(other, master);
    }

    public record State(List<UUID> neutral, Optional<UUID> master, long until) {

        public static final State EMPTY = new State(List.of(), Optional.empty(), 0L);

        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.listOf().optionalFieldOf("neutral", List.of()).forGetter(State::neutral),
            UUIDUtil.CODEC.optionalFieldOf("master").forGetter(State::master),
            Codec.LONG.optionalFieldOf("until", 0L).forGetter(State::until)
        ).apply(instance, State::new));
    }
}
