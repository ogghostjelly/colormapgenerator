package ogghostjelly.colormapgenerator.colormap;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import org.jetbrains.annotations.NotNull;

public interface IColormap {
    @NotNull Block colorToBlock(MapColor color);
}
