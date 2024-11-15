package ogghostjelly.colormapgenerator.utils.image;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.utils.color.ColorUtil;
import ogghostjelly.colormapgenerator.utils.color.IColorMap;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * <a href="https://en.wikipedia.org/wiki/Run-length_encoding">Run-length encoding</a>
 */
public class RLEBlockImage implements IBlockImage {
    private final ArrayList<ImageChunk> chunks;
    private final int width;
    private final int height;

    public RLEBlockImage(NativeImage image, IColorMap map) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.chunks = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            Block currentBlock = null;
            Vector2i from = new Vector2i(0, 0);
            Vector2i to = new Vector2i(0, 0);

            for (int x = 0; x < width; x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                Block block = map.colorToBlock(color);
                // The IColorMap interface used to return null instead of Blocks.AIR
                // I can't be bothered to change the code to handle that, so I just swap Blocks.AIR back to null.
                if (block == Blocks.AIR) {
                    block = null;
                }

                if (currentBlock != block) {
                    if (currentBlock != null) {
                        this.chunks.add(new ImageChunk(currentBlock, from, to));
                    }

                    currentBlock = block;
                    from = new Vector2i(x, y);
                    to = new Vector2i(x, y);
                } else {
                    to.x += 1;
                }
            }

            if (currentBlock != null) {
                this.chunks.add(new ImageChunk(currentBlock, from, to));
            }
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public @NotNull Stream<ImageChunk> getChunks() {
        return this.chunks.stream();
    }
}
