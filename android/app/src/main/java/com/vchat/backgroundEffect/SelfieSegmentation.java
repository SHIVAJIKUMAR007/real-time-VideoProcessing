package com.vchat.backgroundEffect;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.util.concurrent.ExecutionException;

public class SelfieSegmentation {
    // Configure Selfie Segmenter
    private SelfieSegmenterOptions options = new SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
            .build();
    private Segmenter segmenter = Segmentation.getClient(options);
    private InputImage inputImage;
    private SegmentationMask mask;
    private Task<SegmentationMask> result;

    public SegmentationMask process(Bitmap bitmap){
        inputImage = InputImage.fromBitmap(bitmap, 0);
        // process the mask
        try {
            result = segmenter.process(inputImage);
            mask = Tasks.await(result);
            return mask;
        } catch (InterruptedException e) {
            // An interrupt occurred while waiting for the task to complete.
            return null;
        } catch (ExecutionException e) {
            // The Task failed, this is the same exception you'd get in a non-blocking
            // failure handler.
            return null;
        }
    }
}
