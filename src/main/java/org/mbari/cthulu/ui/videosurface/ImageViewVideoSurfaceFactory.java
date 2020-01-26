package org.mbari.cthulu.ui.videosurface;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;

import static uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters.getVideoSurfaceAdapter;

/**
 * Factory used to create a {@link VideoSurface} component for an {@link ImageView}.
 */
public final class ImageViewVideoSurfaceFactory {

    private final ImageView imageView;
    private final PixelBufferBufferFormatCallback bufferFormatCallback;
    private final PixelBufferRenderCallback renderCallback;
    private final PixelBufferVideoSurface videoSurface;

    private PixelBuffer<ByteBuffer> pixelBuffer;

    /**
     * Get a {@link VideoSurface} for an {@link ImageView}.
     *
     * @param imageView image view used to render the video
     * @return video surface
     */
    public static VideoSurface getVideoSurface(ImageView imageView) {
        return new ImageViewVideoSurfaceFactory(imageView).getVideoSurface();
    }

    private ImageViewVideoSurfaceFactory(ImageView imageView) {
        this.imageView = imageView;
        this.bufferFormatCallback = new PixelBufferBufferFormatCallback();
        this.renderCallback = new PixelBufferRenderCallback();
        this.videoSurface = new PixelBufferVideoSurface();
    }

    private VideoSurface getVideoSurface() {
        return videoSurface;
    }

    private class PixelBufferBufferFormatCallback implements BufferFormatCallback {

        private int sourceWidth;
        private int sourceHeight;

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
            PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
            pixelBuffer = new PixelBuffer<>(sourceWidth, sourceHeight, buffers[0], pixelFormat);
            imageView.setImage(new WritableImage(pixelBuffer));
        }
    }

    private class PixelBufferRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            Platform.runLater(() -> pixelBuffer.updateBuffer(pb -> null));
        }
    }

    private class PixelBufferVideoSurface extends CallbackVideoSurface {
        private PixelBufferVideoSurface() {
            super(
                ImageViewVideoSurfaceFactory.this.bufferFormatCallback,
                ImageViewVideoSurfaceFactory.this.renderCallback,
                true,
                getVideoSurfaceAdapter()
            );
        }
    }
}
