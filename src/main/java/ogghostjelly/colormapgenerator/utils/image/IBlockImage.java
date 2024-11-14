package ogghostjelly.colormapgenerator.utils.image;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface IBlockImage {
    int getWidth();
    int getHeight();
    @NotNull Stream<ImageChunk> getChunks();
}
