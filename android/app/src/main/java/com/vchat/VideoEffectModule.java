package com.vchat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.oney.WebRTCModule.videoEffects.ProcessorProvider;

import com.vchat.backgroundEffect.BackgroundBlurFactory;
import com.vchat.backgroundEffect.ChangeBackgroundFactory;

public class VideoEffectModule extends ReactContextBaseJavaModule {
    VideoEffectModule(ReactApplicationContext context) {
        super(context);
    }

    // add to VideoEffectModule.java
    @Override
    public String getName() {
        return "VideoEffectModule";
    }

    @ReactMethod
    public String makeGreat(String name) {
        return name + " is great";
    }

    @ReactMethod
    public void addMethods() {
        Rotate90Factory rotate90 = new Rotate90Factory();
        ProcessorProvider.addProcessor("rotate90", rotate90);
    }

    @ReactMethod
    public void registerBackgroundBlurMethod() {
        BackgroundBlurFactory backgroundBlurFactory = new BackgroundBlurFactory();
        ProcessorProvider.addProcessor("blurBg", backgroundBlurFactory);
    }

    @ReactMethod
    public void registerChangeBackgroundMethod(String bgImageUrl) {
        ChangeBackgroundFactory changeBackgroundFactory = new ChangeBackgroundFactory(bgImageUrl);
        ProcessorProvider.addProcessor("changeBg", changeBackgroundFactory);
    }
}