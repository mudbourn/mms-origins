package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

/**
 * Puts a floran into Crawl on Demand's crawl when they leave leaf form somewhere too low
 * to stand, so they stay prone after crawling out of the gap.
 *
 * <p>Crawl on Demand is reached by reflection because it is not a dependency. It keeps the
 * crawl flag on both sides and only the client reports changes, so the local flag is set
 * and the same packet the mod's own key handler sends goes to the server. Both the
 * {@code me.talilon} and {@code me.thecraftycoder} package roots are tried.
 */
public final class LeafCrawlHandoff {

    private static final String[] ROOTS = {
        "me.talilon.minecraft.crawlondemand",
        "me.thecraftycoder.minecraft.crawlondemand"
    };

    private static boolean wasLeafForm;
    private static boolean resolved;
    private static Method setCrawling;
    private static Object network;
    private static Method sendToServer;
    private static Class<?> packetClass;

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            wasLeafForm = false;
            return;
        }
        boolean leafForm = MmsOriginsPowers.leafFormComplete(player);
        if (wasLeafForm && !leafForm && !player.isInWater() && !canStand(player)) {
            startCrawl(player);
        }
        wasLeafForm = leafForm;
    }

    private static boolean canStand(LocalPlayer player) {
        return player.level().noCollision(
            player,
            player.getDimensions(Pose.STANDING).makeBoundingBox(player.position()).deflate(1.0E-7));
    }

    private static void startCrawl(Player player) {
        if (!resolve()) {
            return;
        }
        try {
            setCrawling.invoke(null, player, true);
            sendToServer.invoke(network, packetClass.getConstructor(boolean.class).newInstance(true));
        } catch (ReflectiveOperationException ignored) {
            setCrawling = null;
        }
    }

    private static boolean resolve() {
        if (resolved) {
            return setCrawling != null;
        }
        resolved = true;
        if (!FabricLoader.getInstance().isModLoaded("crawlondemand")) {
            return false;
        }
        for (String root : ROOTS) {
            try {
                Class<?> manager = Class.forName(root + ".platform.CrawlStateManager");
                Class<?> services = Class.forName(root + ".platform.Services");
                Class<?> helper = Class.forName(root + ".platform.services.INetworkHelper");
                packetClass = Class.forName(root + ".network.CrawlStatePacket");
                network = services.getField("NETWORK").get(null);
                sendToServer = helper.getMethod("sendToServer", packetClass);
                setCrawling = manager.getMethod("setCrawling", Player.class, boolean.class);
                return true;
            } catch (ReflectiveOperationException ignored) {
                setCrawling = null;
            }
        }
        return false;
    }

    private LeafCrawlHandoff() {
    }
}
