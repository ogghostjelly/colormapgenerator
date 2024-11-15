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
 * <a href="https://en.wikipedia.org/wiki/Quadtree">Lossless Quadtree compression</a>
 */
public class QTBlockImage implements IBlockImage {
    private class QTSplit {
        public final QTChunk topLeft;
        public final QTChunk topRight;
        public final QTChunk bottomLeft;
        public final QTChunk bottomRight;

        private QTSplit(QTChunk topLeft, QTChunk topRight, QTChunk bottomLeft, QTChunk bottomRight) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
        }
    }

    private class QTChunk {
        private final Vector2i from;
        private final Vector2i to;

        private QTChunk(Vector2i from, Vector2i to) {
            this.from = from;
            this.to = to;
        }

        public Stream<ImageChunk> getChunks(ArrayBlockImage image) {
            ColorMapGenerator.LOGGER.info(String.valueOf(this));

            ImageChunk imageChunk = this.tryToImageChunk(image);
            if (imageChunk != null) {
                ColorMapGenerator.LOGGER.info("Cohesive chunk found! Returning.");
                return Stream.of(imageChunk);
            }

            QTSplit split = this.split();
            if (split == null) {
                ColorMapGenerator.LOGGER.info("Can't split further! Returning points.");
                return this.pointsToImageChunks(image);
            }

            ColorMapGenerator.LOGGER.info("Chunk splitting.");

            return Streams.concat(
                    split.topLeft.getChunks(image),
                    split.topRight.getChunks(image),
                    split.bottomLeft.getChunks(image),
                    split.bottomRight.getChunks(image)
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

        public @Nullable QTBlockImage.QTSplit split() {
            if (this.from.x == this.to.x) {
                return null;
            } else if (this.from.y == this.to.y) {
                return null;
            }

            ColorMapGenerator.LOGGER.info(String.format("%s %s | %s %s", this.from.x, this.from.y, this.to.x, this.to.y));
            ColorMapGenerator.LOGGER.info(String.valueOf(this));

            Vector2d center = this.getCenter();

            ColorMapGenerator.LOGGER.info(String.format("%s %s | %s %s", this.from.x, this.from.y, this.to.x, this.to.y));
            ColorMapGenerator.LOGGER.info(String.valueOf(this));

            Vector2i topCorner = new Vector2i(center, RoundingMode.FLOOR);
            Vector2i bottomCorner = new Vector2i(center, RoundingMode.CEILING);

            ColorMapGenerator.LOGGER.info(String.format("%s %s | %s %s", this.from.x, this.from.y, this.to.x, this.to.y));
            ColorMapGenerator.LOGGER.info(String.valueOf(this));

            QTChunk topLeft = new QTChunk(this.from, topCorner);
            QTChunk topRight = new QTChunk(new Vector2i(bottomCorner.x, this.from.y), new Vector2i(this.to.x, topCorner.y));
            QTChunk bottomLeft = new QTChunk(new Vector2i(this.from.x, bottomCorner.y), new Vector2i(topCorner.x, this.to.y));
            QTChunk bottomRight = new QTChunk(bottomCorner, this.to);

            ColorMapGenerator.LOGGER.info(String.format("%s %s | %s %s", this.from.x, this.from.y, this.to.x, this.to.y));
            ColorMapGenerator.LOGGER.info(String.valueOf(this));

            ColorMapGenerator.LOGGER.info("topLeft = " + topLeft);
            ColorMapGenerator.LOGGER.info("topRight = " + topRight);
            ColorMapGenerator.LOGGER.info("bottomLeft = " + bottomLeft);
            ColorMapGenerator.LOGGER.info("bottomRight = " + bottomRight);

            return new QTSplit(topLeft, topRight, bottomLeft, bottomRight);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof QTChunk other)) {
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

    private final QTChunk rootChunk;
    private final ArrayBlockImage image;
    private final int width;
    private final int height;

    public QTBlockImage(NativeImage image, IColorMap colorMap) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.image = new ArrayBlockImage(image, colorMap);
        this.rootChunk = new QTChunk(
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
