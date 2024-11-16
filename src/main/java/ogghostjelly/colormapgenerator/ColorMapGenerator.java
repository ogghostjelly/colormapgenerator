package ogghostjelly.colormapgenerator;

import net.fabricmc.api.ClientModInitializer;
import ogghostjelly.colormapgenerator.command.ModCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: add world edit/schematic support
// TODO: separate chunk generators/encoding schemes into separate class maybe?
// TODO: add block position to imagefill command
// TODO: add staircasing
// TODO: maybe colormap can be renamed to colorspace

public class ColorMapGenerator implements ClientModInitializer {
    public static final String MOD_ID = "color-map-generator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModCommands.registerCommandsClient();
    }
}
