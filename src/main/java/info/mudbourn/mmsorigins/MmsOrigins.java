package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.item.MmsItems;
import info.mudbourn.mmsorigins.power.MmsPowerFactories;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsOrigins implements ModInitializer {
    public static final String MOD_ID = "mms_origins";
    public static final Logger LOGGER = LoggerFactory.getLogger("MMS Origins");

    @Override
    public void onInitialize() {
        MmsPowerFactories.register();
        MmsItems.register();
        LOGGER.info("MMS Origins loaded.");
    }
}
