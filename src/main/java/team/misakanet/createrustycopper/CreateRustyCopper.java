package team.misakanet.createrustycopper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Mod constants and common initialization. */
public final class CreateRustyCopper {
    public static final String MOD_ID = "create_rusty_copper";
    public static final String NAME = "Create: Rusty Copper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private CreateRustyCopper() {
    }

    public static void init() {
        LOGGER.info("{} initialized", NAME);
    }
}

