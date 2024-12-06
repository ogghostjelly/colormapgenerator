package ogghostjelly.colormapgenerator.image;

import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class NearestColorImage implements Image {
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    private final MapColor[][] pixels;
    private final int width;
    private final int height;

    private NearestColorImage(MapColor[][] pixels, int width, int height) {
        if (pixels.length != width) {
            throw new RuntimeException("width does not match array length");
        }

        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    public NearestColorImage(@NotNull NativeImage image) {
        Objects.requireNonNull(image);

        this.height = image.getHeight();
        this.width = image.getWidth();

        this.pixels = new MapColor[this.height][this.width];

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                this.pixels[y][x] = getClosestMapColorFromARGB(color);
            }
        }
    }

    private static MapColor getClosestMapColorFromARGB(int color) {
        if (ColorUtil.IsTransparent(color)) {
            return MapColor.CLEAR;
        }

        Optional<MapColor> value = Arrays.stream(ColorUtil.getMapColors())
                .min((a, b) -> ColorUtil.compare(color, a.color, b.color));

        if (value.isEmpty()) {
            LOGGER.error("fatal error: getMapColors is empty!");
            return MapColor.CLEAR;
        }

        return value.get();
    }

    public MapColor getPixel(int x, int y) {
        return this.pixels[y][x];
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public void setPixel(int x, int y, @NotNull MapColor value) {
        Objects.requireNonNull(value);
        this.pixels[y][x] = value;
    }
}
