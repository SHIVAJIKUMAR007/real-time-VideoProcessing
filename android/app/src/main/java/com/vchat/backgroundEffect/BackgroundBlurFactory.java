package com.vchat.backgroundEffect;

import android.graphics.Bitmap;

import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.oney.WebRTCModule.videoEffects.VideoFrameProcessor;
import com.oney.WebRTCModule.videoEffects.VideoFrameProcessorFactoryInterface;

import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;

public class BackgroundBlurFactory implements VideoFrameProcessorFactoryInterface {
    public VideoFrameProcessor build() {
        return new VideoFrameProcessor() {
            private BitmapVideoFrameConversion convertor = null;
            private SelfieSegmentation selfieSegmentation = null;
            private SegmentationMask mask;
            private int[] pixels;
            private int width;
            private int height;
            private double backgroundLikelihood;
            private Bitmap bitmap;
            private VideoFrame outputFrame;

            @Override
            public VideoFrame process(VideoFrame frame, SurfaceTextureHelper textureHelper) {
                if (convertor == null) {
                    convertor = new BitmapVideoFrameConversion(textureHelper);
                }
                if (selfieSegmentation == null) {
                    selfieSegmentation = new SelfieSegmentation();
                }
                bitmap = convertor.videoFrame2Bitmap(frame);
                mask = selfieSegmentation.process(bitmap);
                width = mask.getWidth();
                height = mask.getHeight();
                bitmap = blurBackground(bitmap, mask, width, height);

                outputFrame = convertor.bitmap2VideoFrame(bitmap, width, height);
                if (bitmap != null) {
                    bitmap.recycle();
                }

                return outputFrame;
            }

            private Bitmap blurBackground(Bitmap bitmap, SegmentationMask mask, int width, int height) {
                ByteBuffer bufferMask = mask.getBuffer();
                backgroundLikelihood = 0;
                pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                // the radius for gaussian blur
                int r = 4;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        // gets the likely hood of the background for this pixel
                        backgroundLikelihood = 1 - bufferMask.getFloat();
                        // sets the pixel if background using gaussian blur technique
                        if (backgroundLikelihood > .2) {
                            if (i >= r && j >= r && i < height - r && j < width - r) {

                                int tl = pixels[(i - r) * width + j - r];
                                int tr = pixels[(i - r) * width + j + r];
                                int tc = pixels[(i - r) * width + j];
                                int bl = pixels[(i + r) * width + j - r];
                                int br = pixels[(i + r) * width + j + r];
                                int bc = pixels[(i + r) * width + j];
                                int cl = pixels[i * width + j - r];
                                int cr = pixels[i * width + j + r];

                                pixels[(i * width) + j] = 0xFF000000 |
                                        (((tl & 0xFF) + (tr & 0xFF) + (tc & 0xFF) + (bl & 0xFF) + (br & 0xFF) + (bc &
                                                0xFF)
                                                + (cl & 0xFF) + (cr & 0xFF)) >> 3) & 0xFF
                                        |
                                        (((tl & 0xFF00) + (tr & 0xFF00) + (tc & 0xFF00) + (bl & 0xFF00) + (br &
                                                0xFF00)
                                                + (bc & 0xFF00) + (cl & 0xFF00) + (cr & 0xFF00)) >> 3) & 0xFF00
                                        |
                                        (((tl & 0xFF0000) + (tr & 0xFF0000) + (tc & 0xFF0000) + (bl & 0xFF0000)
                                                + (br & 0xFF0000)
                                                + (bc & 0xFF0000) + (cl & 0xFF0000) + (cr & 0xFF0000)) >> 3) & 0xFF0000;
                            }

                        }
                    }
                }

                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

                return bitmap;

            }
        };
    }
}
