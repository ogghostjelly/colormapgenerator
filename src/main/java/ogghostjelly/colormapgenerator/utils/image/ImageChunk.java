package ogghostjelly.colormapgenerator.utils.image;

import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class ImageChunk {
    public final @NotNull Block block;
    public final @NotNull Vector2i from;
    public final @NotNull Vector2i to;

    public ImageChunk(@NotNull Block block, @NotNull Vector2i from, @NotNull Vector2i to) {
        this.block = block;
        this.from = from;
        this.to = to;
    }

    public ImageChunk(@NotNull Block block, @NotNull Vector2i point) {
        this(block, point, point);
    }

    @Override
    public String toString() {
        return String.format("[ImageChunk: (%s, %s) to (%s, %s) with %s]", this.from.x, this.from.y, this.to.x, this.to.y, this.block);
    }
}
