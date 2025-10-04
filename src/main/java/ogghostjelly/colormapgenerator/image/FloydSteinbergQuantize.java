package ogghostjelly.colormapgenerator.image;

import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.colormap.MapColorImage;
import ogghostjelly.colormapgenerator.utils.ColorUtil;

/**
 * NOT YET IMPLEMENTED
 */
public class FloydSteinbergQuantize implements IQuantize {
    /**
     * for each y from top to bottom do
     *     for each x from left to right do
     *         oldpixel := pixels[x][y]
     *         newpixel := find_closest_palette_color(oldpixel)
     *         pixels[x][y] := newpixel
     *         quant_error := oldpixel - newpixel
     *         pixels[x + 1][y    ] := pixels[x + 1][y    ] + quant_error × 7 / 16
     *         pixels[x - 1][y + 1] := pixels[x - 1][y + 1] + quant_error × 3 / 16
     *         pixels[x    ][y + 1] := pixels[x    ][y + 1] + quant_error × 5 / 16
     *         pixels[x + 1][y + 1] := pixels[x + 1][y + 1] + quant_error × 1 / 16
     */

    private final NearestThresholdQuantize quantizer;

    private FloydSteinbergQuantize(MapColor[] colors) {
        this.quantizer = new NearestThresholdQuantize(colors);
    }

    @Override
    public MapColorImage quantize(NativeImage image) {
        // needs to be linearized first

        int width = image.getWidth();
        int height = image.getHeight();

        var outputImage = new MapColorImage(width, height);

        NativeImage inputImage = new NativeImage(width, height, false);
        inputImage.copyFrom(image);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int old_pixel = ColorUtil.SwapFormat(inputImage.getColor(x, y));
                MapColor new_pixel = this.quantizer.quantize(old_pixel);

                outputImage.setPixel(x, y, new_pixel);

                int quant_error = old_pixel - ColorUtil.mapColorToARGB(new_pixel);
                addColor(inputImage, x+1, y, (int) (quant_error * (7.0/16.0)));
                addColor(inputImage, x-1, y+1, (int) (quant_error * (3.0/16.0)));
                addColor(inputImage, x, y+1, (int) (quant_error * (5.0/16.0)));
                addColor(inputImage, x+1, y+1, (int) (quant_error * (1.0/16.0)));
            }
        }

        inputImage.close();

        return outputImage;
        //throw new NotImplementedException("floyd-steinberg dither is not yet implemented, for now use NearestThresholdQuantize instead.");
    }

    private static void addColor(NativeImage image, int x, int y, int value) {
        image.setColor(x, y, image.getColor(x, y) + value);
    }
}
