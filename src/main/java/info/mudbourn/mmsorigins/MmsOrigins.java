package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.entity.HenchmanLifecycle;
import info.mudbourn.mmsorigins.entity.MmsEntities;
import info.mudbourn.mmsorigins.item.MmsItems;
import info.mudbourn.mmsorigins.power.MmsPowerFactories;
import info.mudbourn.mmsorigins.sound.MmsSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsOrigins implements ModInitializer {
    public static final String MOD_ID = "mms_origins";
    public static final Logger LOGGER = LoggerFactory.getLogger("MMS Origins");

    @Override
    public void onInitialize() {
        MmsPowerFactories.register();
        MmsEntities.register();
        HenchmanLifecycle.register();
        MmsItems.register();
        MmsSounds.register();
        VerdantGrowth.register();
        LOGGER.info("MMS Origins loaded.");
    }
}
