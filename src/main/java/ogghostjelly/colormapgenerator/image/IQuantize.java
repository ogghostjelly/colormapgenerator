package ogghostjelly.colormapgenerator.image;

import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.colormap.MapColorImage;

public interface IQuantize {
    /**
     * Quantize an ABGR NativeImage.
     * <p>
     * NOTE: Unlike `MapColor quantize(int)` the color format is ABGR not ARGB
     */
    MapColorImage quantize(NativeImage image);
}
