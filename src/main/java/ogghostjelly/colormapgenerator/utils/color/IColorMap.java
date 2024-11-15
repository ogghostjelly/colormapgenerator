package ogghostjelly.colormapgenerator.utils.color;

import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

public interface IColorMap {
    /** Converts an ARGB (0xAARRGGBB) Color to a block
     */
    @NotNull Block colorToBlock(int color);
}
