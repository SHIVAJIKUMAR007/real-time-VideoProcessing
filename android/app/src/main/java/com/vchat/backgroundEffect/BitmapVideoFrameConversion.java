package com.vchat.backgroundEffect;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import org.webrtc.SurfaceTextureHelper;
import org.webrtc.TextureBufferImpl;
import org.webrtc.VideoFrame;
import org.webrtc.YuvConverter;

public class BitmapVideoFrameConversion {
    private YuvFrame yuvFrame = null;
    private SurfaceTextureHelper textureHelper;
    private YuvConverter yuvConverter = new YuvConverter();
    private Matrix transform = new Matrix();
    private int[] textures;
    private TextureBufferImpl buffer;
    private VideoFrame.I420Buffer i420buffer;
    private Bitmap newBitmap;
    private Matrix matrixToFlip;
    public BitmapVideoFrameConversion(SurfaceTextureHelper textureHelper) {
        this.textureHelper = textureHelper;
        this.textures = new int[1];
    }

    public Bitmap videoFrame2Bitmap(VideoFrame frame) {
        if (yuvFrame == null) {
            yuvFrame = new YuvFrame(frame, YuvFrame.PROCESSING_NONE,
                    frame.getTimestampNs());
        } else {
            yuvFrame.fromVideoFrame(frame, YuvFrame.PROCESSING_NONE,
                    frame.getTimestampNs());
        }

        return createFlippedBitmap(yuvFrame.getBitmap(),true,false);
    }
    public Bitmap createFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {
        matrixToFlip = new Matrix();
        matrixToFlip.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrixToFlip, true);
    }

    public VideoFrame bitmap2VideoFrame(Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }
        textures = new int[1];
        long start = System.nanoTime();
        GLES20.glGenTextures(0, textures, 1);

        buffer = new TextureBufferImpl(width, height,
                VideoFrame.TextureBuffer.Type.RGB,
                textures[0],
                transform,
                textureHelper.getHandler(),
                yuvConverter,
                null);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        i420buffer = yuvConverter.convert(buffer);

        long timestamp = System.nanoTime() - start;
        VideoFrame videoFrame = new VideoFrame(i420buffer, 180, timestamp);
        if (bitmap != null) {
            bitmap.recycle();
        }
        // buffer.release();
        return videoFrame;
    }
}
