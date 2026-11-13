package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.compat.DummyActionOnHitBridge;
import info.mudbourn.mmsorigins.compat.FishOfThievesFairyFishing;
import info.mudbourn.mmsorigins.compat.HeavyNetheriteFrostResistance;
import info.mudbourn.mmsorigins.compat.SelfReviveStrainSoundBridge;
import info.mudbourn.mmsorigins.effect.MmsMobEffects;
import info.mudbourn.mmsorigins.entity.HenchmanLifecycle;
import info.mudbourn.mmsorigins.entity.MmsEntities;
import info.mudbourn.mmsorigins.fur.BodyFurAttachment;
import info.mudbourn.mmsorigins.fur.BodyTailAttachment;
import info.mudbourn.mmsorigins.fur.BodyWingAttachment;
import info.mudbourn.mmsorigins.fur.LogoutBodyFurBridge;
import info.mudbourn.mmsorigins.item.MmsItems;
import info.mudbourn.mmsorigins.power.MmsPowerFactories;
import info.mudbourn.mmsorigins.power.action.CultivateStoneAction;
import info.mudbourn.mmsorigins.power.action.LaunchAction;
import info.mudbourn.mmsorigins.power.action.SummonHenchmenAction;
import info.mudbourn.mmsorigins.sound.MmsSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsOrigins implements ModInitializer {
    public static final String MOD_ID = "mms_origins";
    public static final Logger LOGGER = LoggerFactory.getLogger("MMS Origins");

    @Override
    public void onInitialize() {
        MmsMobEffects.register();
        MmsPowerFactories.register();
        MmsEntities.register();
        HenchmanLifecycle.register();
        MmsItems.register();
        MmsSounds.register();
        VerdantGrowth.register();
        FairyScaleEnforcer.register();
        MoltenChargeCharger.register();
        ZombieMeterDeathReset.register();
        ClimateTolerance.register();
        ZombifyCommand.register();
        OriginFlush.register();
        SummonHenchmenAction.register();
        TemperatureBaseline.register();
        AvianProvocation.register();
        HazeBond.register();
        SporeHaze.register();
        CultivateStoneAction.register();
        GuardianBeam.register();
        LaunchAction.register();
        BodyFurAttachment.touch();
        BodyTailAttachment.touch();
        BodyWingAttachment.touch();
        if (FabricLoader.getInstance().isModLoaded("origins-legacy-classes") && FabricLoader.getInstance().isModLoaded("jobsplus")) {
            ClassJobs.register();
        }
        if (FabricLoader.getInstance().isModLoaded("mms_combat")) {
            LogoutBodyFurBridge.register();
            SelfReviveStrainSoundBridge.register();
            DummyActionOnHitBridge.register();
        }
        if (FabricLoader.getInstance().isModLoaded("fishofthieves")) {
            FishOfThievesFairyFishing.register();
        }
        if (FabricLoader.getInstance().isModLoaded("expanded_weaponry")) {
            HeavyNetheriteFrostResistance.register();
        }
        LOGGER.info("MMS Origins loaded.");
    }
}
