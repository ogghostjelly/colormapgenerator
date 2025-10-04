package ogghostjelly.colormapgenerator.colormap;

import net.minecraft.block.MapColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MapColorImage {
    private final byte[][] pixels;
    private final int width;
    private final int height;
    private boolean hasBrightnessFlag = false;

    public MapColorImage(int width, int height) {
        this.pixels = new byte[height][width];
        this.width = width;
        this.height = height;
    }

    public static byte colorToByte(MapColor color, MapColor.Brightness brightness) {
        return (byte)(color.id << 2 | brightness.id & 0b11);
    }

    public static MapColor colorOf(byte color) {
        return MapColor.get(color >> 2);
    }

    public static MapColor.Brightness brightnessOf(byte color) {
        return MapColor.Brightness.validateAndGet(color & 0b11);
    }

    public byte getPixelByte(int x, int y) {
        return this.pixels[y][x];
    }

    public ColormapItem getPixel(int x, int y) {
        byte color = this.getPixelByte(x, y);
        return new ColormapItem(colorOf(color), brightnessOf(color));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setPixel(int x, int y, @NotNull MapColor value) {
        setPixel(x, y, value, MapColor.Brightness.NORMAL);
    }

    public boolean hasBrightness() {
        return hasBrightnessFlag;
    }

    public void setPixel(int x, int y, @NotNull MapColor value, @NotNull MapColor.Brightness brightness) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(brightness);

        if (brightness != MapColor.Brightness.NORMAL) {
            this.hasBrightnessFlag = true;
        }

        this.pixels[y][x] = colorToByte(value, brightness);
    }
}

