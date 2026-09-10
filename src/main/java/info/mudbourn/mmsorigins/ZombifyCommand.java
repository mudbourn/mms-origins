package info.mudbourn.mmsorigins;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.VariableIntPower;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

/**
 * A testing command that drives the zombification meter on chosen players.
 *
 * <p>{@code /zombify} caps the caller's own meter, turning them at once;
 * {@code /zombify <targets>} does the same to others, and a trailing value sets
 * the meter part way instead, for watching the creep and nether recovery. It is
 * an operator command and only touches players who carry the piglin meter.
 */
public final class ZombifyCommand {

    private static final int METER_CAP = 600;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("zombify")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> zombify(
                        context.getSource(),
                        List.of(context.getSource().getPlayerOrException()),
                        METER_CAP))
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(context -> zombify(
                            context.getSource(),
                            EntityArgument.getPlayers(context, "targets"),
                            METER_CAP))
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, METER_CAP))
                        .executes(ZombifyCommand::zombifyToValue)))));
    }

    private static int zombifyToValue(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return zombify(
                context.getSource(),
                EntityArgument.getPlayers(context, "targets"),
                IntegerArgumentType.getInteger(context, "value"));
    }

    private static int zombify(CommandSourceStack source, Collection<ServerPlayer> targets, int value) {
        int affected = 0;
        for (ServerPlayer target : targets) {
            Power power = MmsOriginsPowers.ZOMBIE_METER.get(target);
            if (power instanceof VariableIntPower meter) {
                meter.setValue(value);
                PowerHolderComponent.syncPower(target, MmsOriginsPowers.ZOMBIE_METER);
                affected++;
            }
        }
        int set = affected;
        source.sendSuccess(() -> Component.literal("Set zombification to " + value + " on " + set + " player(s)."), true);
        return affected;
    }

    private ZombifyCommand() {
    }
}
