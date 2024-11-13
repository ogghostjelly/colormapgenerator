package ogghostjelly.colormapgenerator;

import net.fabricmc.api.ClientModInitializer;
import ogghostjelly.colormapgenerator.command.ModCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorMapGenerator implements ClientModInitializer {
    public static final String MOD_ID = "color-map-generator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModCommands.registerCommandsClient();
    }
}
