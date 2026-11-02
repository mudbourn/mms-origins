package info.mudbourn.mmsorigins.mixin.originsclasses;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the Origins Classes mixins on that mod, and the class/job mixins on Jobs+ too.
 */
public class OriginsClassesMixinGate implements IMixinConfigPlugin {

    private final boolean classes = FabricLoader.getInstance().isModLoaded("origins-legacy-classes");

    private final boolean jobs = FabricLoader.getInstance().isModLoaded("jobsplus");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("TreeFellingLeafGuardMixin")) {
            return classes;
        }
        return classes && jobs;
    }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
