package ogghostjelly.colormapgenerator.colormap;

import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.NotNull;

public interface IColormap {
    @NotNull ColormapItem map(int color);
    @NotNull MapColorImage map(NativeImage image);
}
