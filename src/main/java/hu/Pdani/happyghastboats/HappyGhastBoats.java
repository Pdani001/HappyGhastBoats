package hu.Pdani.happyghastboats;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HappyGhastBoats implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("HappyGhastBoats");

    @Override
    public void onInitialize() {
        LOGGER.info("Mod initialized successfully!");
    }

}
