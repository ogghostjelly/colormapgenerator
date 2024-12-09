package ogghostjelly.colormapgenerator.utils;

import net.minecraft.block.MapColor;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import org.slf4j.Logger;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

public class ColorUtil {
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public static double difference(int r1, int g1, int b1, int r2, int g2, int b2) {
        return Math.sqrt(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));
    }

    /**
     * Get the difference between two colors. This function works for both ARGB and ABGR formats as long as both of the given colors are of the same format.
     */
    public static double difference(int colorA, int colorB) {
        Color a = ColorUtil.ARGBtoColor(colorA, false);
        Color b = ColorUtil.ARGBtoColor(colorB, false);
        return difference(a.getRed(), a.getGreen(), a.getBlue(),
                b.getRed(), b.getGreen(), b.getBlue());
    }

    public static boolean IsTransparent(int color) {
        return (color & 0xFF000000) == 0;
    }

    /**
     * Converts ARGB (0xAARRGGBB) to ABGR (0xAABBGGRR) and vice versa.
     * ARGB is the format minecraft uses. ABGR is the format NativeImage uses.
     */
    public static int SwapFormat(int color) {
        int r = (color >> 16) & 0xFF;
        int g = color & 0xFF00;
        int b = (color << 16) & 0xFF0000;
        int a = color & 0xFF000000;
        return a | b | g | r;
    }

    public static Color ARGBtoColor(int color, boolean hasAlpha) {
        return new Color(color, hasAlpha);
    }

    public static Color ABGRtoColor(int color, boolean hasAlpha) {
        return ARGBtoColor(SwapFormat(color), hasAlpha);
    }

    public static MapColor[] getMapColors() {
        var colors = new MapColor[64];
        for (int i = 0; i < 64; i++) {
            colors[i] = MapColor.get(i);
        }
        return colors;
    }

    public static int mapColorToARGB(MapColor color) {
        if (color == MapColor.CLEAR) {
            return color.color;
        }
        return color.color | 0xff000000;
    }

    public static int compare(int color, int a, int b) {
        double aDif = ColorUtil.difference(a, color);
        double bDif = ColorUtil.difference(b, color);
        return Double.compare(aDif, bDif);
    }

    public static MapColor getClosestMapColorFromARGB(MapColor[] colors, int color) {
        if (ColorUtil.IsTransparent(color)) {
            return MapColor.CLEAR;
        }

        Optional<MapColor> value = Arrays.stream(colors)
                .min((a, b) -> ColorUtil.compare(color, a.color, b.color));

        if (value.isEmpty()) {
            LOGGER.error("fatal error: getMapColors is empty!");
            return MapColor.CLEAR;
        }

        return value.get();
    }
}
