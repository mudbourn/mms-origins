package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.power.SurgingFlight;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;

/**
 * Drives the local player's flying speed from their {@link SurgingFlight} power.
 * Flight movement is simulated on the client, so the speed is set here each tick
 * and the server never pushes it.
 */
public final class FlightSurge {

    private static final float DEFAULT_FLYING_SPEED = 0.05F;

    private static int surgeTicks;
    private static float drivenSpeed = Float.NaN;

    private FlightSurge() {
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            surgeTicks = 0;
            drivenSpeed = Float.NaN;
            return;
        }
        Abilities abilities = player.getAbilities();
        SurgingFlight flight = flightOf(player);
        if (flight == null) {
            release(abilities);
            return;
        }
        boolean surging = abilities.flying && flight.surging(player);
        surgeTicks = surging ? surgeTicks + 1 : 0;
        drivenSpeed = surging
            ? flight.surgeSpeed(player, SurgingFlight.build(surgeTicks))
            : flight.cruiseSpeed(player);
        abilities.setFlyingSpeed(drivenSpeed);
    }

    private static SurgingFlight flightOf(LocalPlayer player) {
        if (player.isCreative() || player.isSpectator() || !player.getAbilities().mayfly) {
            return null;
        }
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return null;
        }
        for (Power power : component.getPowers()) {
            if (power instanceof SurgingFlight flight) {
                return flight;
            }
        }
        return null;
    }

    private static void release(Abilities abilities) {
        if (abilities.getFlyingSpeed() == drivenSpeed) {
            abilities.setFlyingSpeed(DEFAULT_FLYING_SPEED);
        }
        surgeTicks = 0;
        drivenSpeed = Float.NaN;
    }
}
