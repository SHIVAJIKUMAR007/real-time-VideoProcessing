package com.vchat;

import org.webrtc.*;
import com.oney.WebRTCModule.videoEffects.VideoFrameProcessor;
import com.oney.WebRTCModule.videoEffects.VideoFrameProcessorFactoryInterface;

public class Rotate90Factory implements VideoFrameProcessorFactoryInterface {

    public VideoFrameProcessor build() {
        return new VideoFrameProcessor() {
            @Override
            public VideoFrame process(VideoFrame frame, SurfaceTextureHelper textureHelper) {
                return new VideoFrame(frame.getBuffer().toI420(), 0, frame.getTimestampNs());
            }
        };
    }
}
