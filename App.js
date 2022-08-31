import {
  Text,
  ScrollView,
  View,
  TouchableOpacity,
  Image,
  NativeModules,
} from 'react-native';
import React, {useEffect, useState} from 'react';
import {RTCView, mediaDevices} from 'react-native-webrtc';

import {launchImageLibrary} from 'react-native-image-picker';

const {VideoEffectModule} = NativeModules;

function App() {
  const [stream, setstream] = useState(null);
  const [isVideoEffect, setisVideoEffect] = useState(null);
  const [backgroundImage, setbackgroundImage] = useState(null);
  let isFront = true;

  useEffect(() => {
    VideoEffectModule.registerBackgroundBlurMethod();

    mediaDevices.enumerateDevices().then(sourceInfos => {
      // console.log(sourceInfos);
      let videoSourceId;
      for (let i = 0; i < sourceInfos.length; i++) {
        const sourceInfo = sourceInfos[i];
        if (
          sourceInfo.kind == 'videoinput' &&
          sourceInfo.facing == (isFront ? 'front' : 'environment')
        ) {
          videoSourceId = sourceInfo.deviceId;
        }
      }
      mediaDevices
        .getUserMedia({
          audio: true,
          video: {
            width: 640,
            height: 480,
            frameRate: 30,
            facingMode: isFront ? 'user' : 'environment',
            deviceId: videoSourceId,
          },
        })
        .then(stream => {
          VideoEffectModule.addMethods();
          setstream(stream);
        })
        .catch(error => {
          // Log error
          console.log(error, ' line no 71');
        });
    });
  }, []);
  const loadImageLibrary = async setter => {
    return await launchImageLibrary(
      {
        mediaType: 'photo',
      },
      result => {
        const {assets} = result;
        if (assets && assets.length > 0) {
          const {uri} = assets[0];
          setter(uri);
        }
      },
    );
  };
  const onSwitchCameras = () => {
    stream.getVideoTracks().forEach(track => {
      track._switchCamera();
    });
  };

  const onRegisterVideoEffect = () => {
    try {
      stream.getVideoTracks().forEach(track => {
        track._setVideoEffect('blurBg');
        // track._setVideoEffect('rotate90');
      });
      setisVideoEffect('blur');
    } catch (error) {
      console.log(error);
    }
  };

  const onRegisterBgImageVideoEffect = () => {
    try {
      VideoEffectModule.registerChangeBackgroundMethod(backgroundImage);

      stream.getVideoTracks().forEach(track => {
        track._setVideoEffect('changeBg');
      });
      setisVideoEffect('changeBg');
    } catch (error) {
      console.log(error);
    }
  };

  const onDiscardVideoEffect = () => {
    try {
      stream.getVideoTracks().forEach(track => {
        track._setVideoEffect();
      });
      setisVideoEffect(null);
    } catch (error) {
      console.log(error);
    }
  };
  return (
    <ScrollView>
      <RTCView
        streamURL={stream?.toURL()}
        style={{width: 300, height: 400, marginTop: 20}}
      />
      <View style={{height: 20}}></View>
      <View
        style={{
          flexDirection: 'column',
          marginTop: 20,
        }}>
        <TouchableOpacity
          style={{backgroundColor: 'green', padding: 10, marginBottom: 10}}
          onPress={onSwitchCameras}>
          <Text style={{color: 'white'}}>Switch Camera</Text>
        </TouchableOpacity>

        {isVideoEffect == 'blur' ? null : (
          <TouchableOpacity
            style={{backgroundColor: 'green', padding: 10, marginBottom: 10}}
            onPress={onRegisterVideoEffect}>
            <Text style={{color: 'white'}}>add blur effect</Text>
          </TouchableOpacity>
        )}

        {!backgroundImage ? null : (
          <TouchableOpacity
            style={{backgroundColor: 'green', padding: 10, marginBottom: 10}}
            onPress={() => {
              if (backgroundImage) onRegisterBgImageVideoEffect();
            }}>
            <Text style={{color: 'white'}}>add background Image effect</Text>
          </TouchableOpacity>
        )}

        {isVideoEffect == null ? null : (
          <TouchableOpacity
            style={{backgroundColor: 'green', padding: 10}}
            onPress={onDiscardVideoEffect}>
            <Text style={{color: 'white'}}>remove effect</Text>
          </TouchableOpacity>
        )}
      </View>

      <View
        style={{
          flexDirection: 'row',
          justifyContent: 'space-between',
          margin: 20,
        }}>
        <Image
          source={{
            uri: backgroundImage,
          }}
          style={{width: 100, height: 100}}
        />

        <TouchableOpacity
          style={{
            backgroundColor: 'green',
            padding: 10,
            marginBottom: 10,
            height: 40,
          }}
          onPress={() => {
            loadImageLibrary(setbackgroundImage);
          }}>
          <Text style={{color: 'white'}}>select background image</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

export default App;
