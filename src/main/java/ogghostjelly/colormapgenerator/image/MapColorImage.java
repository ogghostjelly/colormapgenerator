package ogghostjelly.colormapgenerator.image;

import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

public class MapColorImage {
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    private final MapColor[][] pixels;
    private final int width;
    private final int height;

    public MapColorImage(int width, int height) {
        this.pixels = new MapColor[height][width];
        this.width = width;
        this.height = height;
    }

    @Deprecated
    public MapColorImage(@NotNull NativeImage image) {
        Objects.requireNonNull(image);

        this.height = image.getHeight();
        this.width = image.getWidth();

        this.pixels = new MapColor[this.height][this.width];

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                this.pixels[y][x] = ColorUtil.getClosestMapColorFromARGB(ColorUtil.getMapColors(), color);
            }
        }
    }

    public @NotNull MapColor getPixel(int x, int y) {
        return this.pixels[y][x];
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setPixel(int x, int y, @NotNull MapColor value) {
        Objects.requireNonNull(value);
        this.pixels[y][x] = value;
    }
}

