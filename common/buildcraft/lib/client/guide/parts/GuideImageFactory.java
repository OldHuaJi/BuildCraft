package buildcraft.lib.client.guide.parts;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.guide.GuiGuide;

public class GuideImageFactory implements GuidePartFactory {
    private final ResourceLocation location;
    private final int imageWidth, imageHeight;
    private final int width, height;

    public GuideImageFactory(ResourceLocation location, int imageWidth, int imageHeight, int width, int height) {
        this.location = location;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.width = width;
        this.height = height;
    }

    @Override
    public GuideImage createNew(GuiGuide gui) {
        return new GuideImage(gui, location, imageWidth, imageHeight, width, height);
    }
}
