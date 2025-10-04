package ogghostjelly.colormapgenerator.blockmap;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import org.slf4j.Logger;

public record BlockmapItem(Block block, BlockmapItem.Elevation elevation) {
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;
    public static final BlockmapItem AIR = new BlockmapItem(Blocks.AIR, Elevation.NORMAL);

    public enum Elevation {
        NORMAL,
        UP,
        DOWN;

        public static Elevation fromBrightness(MapColor.Brightness brightness) {
            switch (brightness) {
                case LOW -> {
                    return Elevation.DOWN;
                }
                case NORMAL -> {
                    return Elevation.NORMAL;
                }
                case HIGH -> {
                    return Elevation.UP;
                }
            }
            LOGGER.error("invalid brightness level");
            return Elevation.NORMAL;
        }
    }
}
