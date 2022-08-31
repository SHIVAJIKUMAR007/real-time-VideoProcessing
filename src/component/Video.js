import React from 'react';
import {View, StyleSheet, Dimensions} from 'react-native';
import {RTCView} from 'react-native-webrtc';
import Button from './Button';

const {width, height} = Dimensions.get('window');
const Video = ({hangup, localStream, remoteStream}) => {
  console.log(localStream?.toURL(), remoteStream?.toURL(), width, height);
  if (localStream && !remoteStream) {
    return (
      <View style={{position: 'relative', height: '100%', width: '100%'}}>
        {/* <RTCView
          streamURL={localStream?.toURL()}
          style={{width: 300, height: 400}}
        /> */}

        <RTCView
          streamURL={localStream?.toURL()}
          style={{position: 'absolute', width: width, height: height}}
        />
        <Button
          onPress={hangup}
          title="hang up"
          style={{position: 'absolute', bottom: 50, left: 100}}
        />
      </View>
    );
  }
  if (localStream && remoteStream)
    return (
      <View style={{position: 'relative', height: '100%', width: '100%'}}>
        <RTCView
          streamURL={localStream?.toURL()}
          style={{position: 'absolute', height: '100%', width: '100%'}}
        />
        <RTCView
          streamURL={localStream?.toUrl()}
          objectFit={'cover'}
          style={{
            position: 'absolute',
            height: 45,
            width: 80,
            bottom: 50,
            right: 20,
          }}
        />
        <Button
          onPress={hangup}
          title="hang up"
          style={{position: 'absolute', bottom: 50, left: 100}}
        />
      </View>
    );
};

const styles = StyleSheet.create({});

export default Video;
