package com.vchat.backgroundEffect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.oney.WebRTCModule.videoEffects.VideoFrameProcessor;
import com.oney.WebRTCModule.videoEffects.VideoFrameProcessorFactoryInterface;

import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoFrame;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

public class ChangeBackgroundFactory implements VideoFrameProcessorFactoryInterface {
    private Bitmap bgImage;

    public ChangeBackgroundFactory(String bgImageUrl) {
        if (bgImageUrl != null) {
            bgImage = getBitmapFromUrl(bgImageUrl);
        }
        else{
            bgImage = null;
        }
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return image;
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    public VideoFrameProcessor build() {

        return new VideoFrameProcessor() {
            private BitmapVideoFrameConversion convertor = null;
            private SelfieSegmentation selfieSegmentation = null;
            private SegmentationMask mask;
            private int[] pixels;
            private int[] bgImagePixels;
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
                bitmap = changeBackground(bitmap, mask, width, height);

                outputFrame = convertor.bitmap2VideoFrame(bitmap, width, height);
                if (bitmap != null) {
                    bitmap.recycle();
                }

                return outputFrame;
            }

            private Bitmap changeBackground(Bitmap bitmap, SegmentationMask mask, int width, int height) {
                if (bgImage == null) {
                    return bitmap;
                }
                ByteBuffer bufferMask = mask.getBuffer();
                backgroundLikelihood = 0;
                int length = width * height;
                pixels = new int[length];
                bgImagePixels = new int[length];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bgImage.getPixels(bgImagePixels, 0, width, 0, 0, width, height);

                for (int i = 0; i < length; i++) {
                    // gets the likely hood of the background for this pixel
                    backgroundLikelihood = 1 - bufferMask.getFloat();
                    // sets the pixel to bgImage pixel value if background it is background
                    if (backgroundLikelihood > .2) {
                        pixels[i] = bgImagePixels[i];
                    }

                }

                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                return bitmap;
            }
        };
    }

}
