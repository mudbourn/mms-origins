package info.mudbourn.mmsorigins.client.fur;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsrendercommon.client.geo.GeoModelData;
import info.mudbourn.mmsrendercommon.client.geo.HumanoidGeoModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The table of ported furs, keyed by the origin they belong to.
 *
 * <p>One row per origin. Each row names the Bedrock geo and the texture; both are
 * loaded and baked lazily on first draw and cached, since baking touches the resource
 * manager and so cannot run before the client is up. A resource-reload does not yet
 * invalidate the cache, which is fine while the set is fixed and shipped in the jar.
 */
public final class FurModels {

    /**
     * A resolved fur: its baked geometry, the geo texture, and the body overlay.
     *
     * <p>The overlay is a vanilla-skin-layout texture drawn onto the player model
     * itself (the merling's scaled torso and tail), separate from the geo fins.
     */
    public record Fur(HumanoidGeoModel model, Identifier texture, Identifier overlay) {}

    /** Where a fur's geo and textures live, before baking. */
    private record Source(Identifier geo, Identifier texture, Identifier overlay) {}

    private static final Map<Identifier, Source> SOURCES = new HashMap<>();
    private static final Map<Identifier, Fur> BAKED = new HashMap<>();

    static {
        register("origins", "merling",
                "mms_origins:fur/geo/merling.geo.json",
                "mms_origins:textures/fur/merling.png",
                "mms_origins:textures/fur/merling_skin.png");
    }

    private FurModels() {
    }

    /** Touches the class so its table is populated; the models bake on first draw. */
    public static void init() {
    }

    /** The fur for an origin, baking it on first request, or null if none is registered. */
    public static Fur resolve(Identifier origin) {
        if (origin == null || !SOURCES.containsKey(origin)) {
            return null;
        }
        Fur baked = BAKED.get(origin);
        if (baked != null) {
            return baked;
        }
        Source source = SOURCES.get(origin);
        HumanoidGeoModel model = load(source.geo());
        if (model == null) {
            return null;
        }
        Fur fur = new Fur(model, source.texture(), source.overlay());
        BAKED.put(origin, fur);
        return fur;
    }

    private static HumanoidGeoModel load(Identifier geo) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(geo);
        if (resource.isEmpty()) {
            MmsOrigins.LOGGER.warn("Missing fur geometry {}", geo);
            return null;
        }
        try (BufferedReader reader = resource.get().openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return HumanoidGeoModel.bake(GeoModelData.parse(json));
        } catch (Exception exception) {
            MmsOrigins.LOGGER.warn("Could not read fur geometry {}", geo, exception);
            return null;
        }
    }

    private static void register(String namespace, String path,
                                 String geo, String texture, String overlay) {
        SOURCES.put(Identifier.fromNamespaceAndPath(namespace, path),
                new Source(Identifier.parse(geo), Identifier.parse(texture), Identifier.parse(overlay)));
    }
}
