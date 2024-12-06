package ogghostjelly.colormapgenerator;

import net.fabricmc.api.ClientModInitializer;
import ogghostjelly.colormapgenerator.command.ModCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: add world edit/schematic support
// TODO: add staircasing
// TODO: can add dithering for better visuals?

/**
 * Colormap pipeline
 * 1. An image gets converted into MapColors using the Image interface
 * 2. The MapColors get converted into blocks using the IColormap interface
 */

public class ColorMapGenerator implements ClientModInitializer {
    public static final String MOD_ID = "color-map-generator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModCommands.registerCommandsClient();
    }
}
