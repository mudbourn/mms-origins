package info.mudbourn.mmsorigins.compat;

import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

/**
 * Sets Crawl on Demand's crawl state from the server, through reflection because the mod is
 * not a dependency. The server's change is synced to the owner and to everyone tracking them,
 * so no client-side hand-off is needed. Both the {@code me.talilon} and
 * {@code me.thecraftycoder} package roots are tried.
 */
public final class CrawlOnDemandBridge {

    private static final String[] ROOTS = {
        "me.talilon.minecraft.crawlondemand",
        "me.thecraftycoder.minecraft.crawlondemand"
    };

    private static boolean resolved;
    private static Method setCrawling;

    public static boolean present() {
        return resolve();
    }

    public static void setCrawling(Player player, boolean crawling) {
        if (!resolve()) {
            return;
        }
        try {
            setCrawling.invoke(null, player, crawling);
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
                setCrawling = manager.getMethod("setCrawling", Player.class, boolean.class);
                return true;
            } catch (ReflectiveOperationException ignored) {
                setCrawling = null;
            }
        }
        return false;
    }

    private CrawlOnDemandBridge() {
    }
}
