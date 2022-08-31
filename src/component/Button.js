import React from 'react';
import {StyleSheet, TouchableOpacity, Text} from 'react-native';

const Button = ({onPress, style, title}) => {
  return (
    <TouchableOpacity onPress={onPress} style={[styles.button, style]}>
      <Text>{title}</Text>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    width: 100,
    height: 60,
    backgroundColor: '#00FF00',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

export default Button;
