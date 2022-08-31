import React from 'react';
import {View, StyleSheet} from 'react-native';
import Button from './Button';

const GettingCall = ({pickup, hangup}) => {
  return (
    <View style={{flexDirection: 'row', justifyContent: 'space-evenly'}}>
      <Button title={'pick up'} onPress={pickup} />
      <Button title={'hang up'} onPress={hangup} />
    </View>
  );
};

export default GettingCall;
