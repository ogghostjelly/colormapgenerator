package ogghostjelly.colormapgenerator.image;

import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;

public interface Image {
    MapColor getPixel(int x, int y);

    int getWidth();
    int getHeight();

    default NativeImage toNativeImage() {
        var image = new NativeImage(this.getWidth(), this.getHeight(), false);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setColor(x, y, getPixel(x, y).color);
            }
        }
        return image;
    }
}
