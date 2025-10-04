package ogghostjelly.colormapgenerator.image;

import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.colormap.MapColorImage;
import ogghostjelly.colormapgenerator.utils.ColorUtil;

public class NearestThresholdQuantize implements IQuantize {
    private final MapColor[] colors;

    public NearestThresholdQuantize(MapColor[] colors) {
        this.colors = colors;
    }

    public MapColor quantize(int color) {
        // TODO: This may return map colors that the player disabled in the config menu
        // we need to refactor so it can handle disabling specific colors
        return ColorUtil.getClosestMapColorFromARGB(this.colors, color);
    }

    @Override
    public MapColorImage quantize(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        var output = new MapColorImage(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                output.setPixel(x, y, this.quantize(color));
            }
        }

        return output;
    }
}
