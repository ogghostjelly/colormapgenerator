package ogghostjelly.colormapgenerator.utils.image;

import net.minecraft.block.Block;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.utils.ColorMap;
import ogghostjelly.colormapgenerator.utils.ColorUtil;

public class ArrayBlockImage {
    private final Block[] pixels;
    private final int width;
    private final int height;

    private ArrayBlockImage(Block[] pixels, int width) {
        this.pixels = pixels;
        this.width = width;
        this.height = pixels.length / width;
    }

    public static ArrayBlockImage fromNativeImage(NativeImage image, ColorMap colorMap) {
        int width = image.getWidth();
        int height = image.getHeight();
        Block[] pixels = new Block[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                Block block = colorMap.colorToBlock(color);
                pixels[x + y * width] = block;
            }
        }

        return new ArrayBlockImage(pixels, image.getWidth());
    }
}
