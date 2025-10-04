package ogghostjelly.colormapgenerator.blockmap;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import ogghostjelly.colormapgenerator.colormap.ColormapItem;
import ogghostjelly.colormapgenerator.colormap.MapColorImage;
import org.jetbrains.annotations.NotNull;

public interface IBlockmap {
    @NotNull Block map(MapColor color, MapColor.Brightness brightness);
    default @NotNull BlockImage map(MapColorImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        var output = new BlockImage(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ColormapItem pixel = image.getPixel(x, y);
                Block block = this.map(pixel.color(), pixel.brightness());
                BlockmapItem.Elevation elevation = BlockmapItem.Elevation.fromBrightness(pixel.brightness());
                output.setPixel(x, y, block, elevation);
            }
        }

        return output;
    }
}
