package ogghostjelly.colormapgenerator.utils.image;

import com.google.common.collect.Streams;
import net.minecraft.block.Blocks;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.utils.color.IColorMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Lossless compression using <a href="https://en.wikipedia.org/wiki/Binary_space_partitioning">binary search partitioning</a>
 */
public class BSPBlockImage implements IBlockImage {
    private class BSPSplit {
        public final BSPChunk left;
        public final BSPChunk right;

        private BSPSplit(BSPChunk left, BSPChunk right) {
            this.left = left;
            this.right = right;
        }
    }

    private class BSPChunk {
        private final Vector2i from;
        private final Vector2i to;

        private BSPChunk(Vector2i from, Vector2i to) {
            this.from = from;
            this.to = to;
        }

        public Stream<ImageChunk> getChunks(ArrayBlockImage image) {
            return this.getChunks(image, false);
        }

        public Stream<ImageChunk> getChunks(ArrayBlockImage image, boolean splitH) {
            ImageChunk imageChunk = this.tryToImageChunk(image);
            if (imageChunk != null) {
                return Stream.of(imageChunk);
            }

            BSPSplit split = this.split(splitH);
            if (split == null) {
                return this.pointsToImageChunks(image);
            }

            return Streams.concat(
                    split.left.getChunks(image, !splitH),
                    split.right.getChunks(image, !splitH)
            );
        }

        public ImageChunk tryToImageChunk(ArrayBlockImage image) {
            boolean isCohesive = this.points()
                    .map(point -> image.getBlock(point.x, point.y))
                    .distinct().limit(2).count() <= 1;

            if (!isCohesive) {
                return null;
            }

            return new ImageChunk(image.getBlock(this.from.x, this.from.y), this.from, this.to);
        }

        public Stream<ImageChunk> pointsToImageChunks(ArrayBlockImage image) {
            return this.points().map(point -> new ImageChunk(image.getBlock(point.x, point.y), point));
        }

        public Stream<Vector2i> points() {
            return IntStream.rangeClosed(this.from.y, this.to.y)
                    .mapToObj(y -> IntStream.rangeClosed(this.from.x, this.to.x)
                            .mapToObj(x -> new Vector2i(x, y)))
                    .flatMap(stream -> stream);
        }

        public Vector2d getCenter() {
            Vector2i from = new Vector2i(this.from.x, this.from.y);
            return new Vector2d(from.add(this.to)).div(2.0);
        }

        public @Nullable BSPBlockImage.BSPSplit split(boolean splitH) {
            if (splitH) {
                return this.splitHorizontal();
            } else {
                return this.splitVertical();
            }
        }

        public @Nullable BSPBlockImage.BSPSplit splitHorizontal() {
            if (this.from.y == this.to.y) {
                return null;
            }

            Vector2i center = new Vector2i(getCenter(), RoundingMode.FLOOR);

            BSPChunk top = new BSPChunk(this.from, new Vector2i(this.to.x, center.y));
            BSPChunk bottom = new BSPChunk(new Vector2i(this.from.x, center.y + 1), this.to);

            return new BSPSplit(top, bottom);
        }

        public @Nullable BSPBlockImage.BSPSplit splitVertical() {
            if (this.from.x == this.to.x) {
                return null;
            }

            Vector2i center = new Vector2i(getCenter(), RoundingMode.FLOOR);

            BSPChunk top = new BSPChunk(this.from, new Vector2i(center.x, this.to.y));
            BSPChunk bottom = new BSPChunk(new Vector2i(center.x + 1, this.from.y), this.to);

            return new BSPSplit(top, bottom);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof BSPChunk other)) {
                return super.equals(object);
            }

            return this.from.x == other.from.x && this.from.y == other.from.y &&
                    this.to.x == other.to.x && this.to.y == other.to.y;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s) to (%s, %s)",
                    this.from.x, this.from.y,
                    this.to.x, this.to.y
            );
        }
    }

    private final BSPChunk rootChunk;
    private final ArrayBlockImage image;
    private final int width;
    private final int height;

    public BSPBlockImage(NativeImage image, IColorMap colorMap) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.image = new ArrayBlockImage(image, colorMap);
        this.rootChunk = new BSPChunk(
                new Vector2i(0, 0),
                new Vector2i(this.width - 1, this.height - 1)
        );
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
        return this.rootChunk.getChunks(this.image)
                .filter(chunk -> chunk.block != Blocks.AIR);
    }
}
