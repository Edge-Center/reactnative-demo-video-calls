import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import React, {useEffect, useState} from 'react';

import {Dimensions, NativeModules, Pressable, StyleSheet, View} from 'react-native';

import {withAnchorPoint} from 'react-native-anchor-point';
import ECRemoteView from './ECRemoteView';
import ECLocalView from './ECLocalView';

import {CameraIcon, DropIcon, MicrophoneIcon, SwitchCameraIcon} from './Icons';
import type {RootStackParamList} from './types';
import {getCameraPermission, getMicPermission} from './helpers';

const screen = Dimensions.get('screen');
const aspectRatio = 3 / 4;
// const height = screen.height > screen.width ? screen.height - 80 : screen.width - 80;
const height = screen.height - 80;
const width = height * aspectRatio; // * screen.scale;

const getTransform = () => {
  let transform = {
    transform: [{translateX: (screen.width - width) / 2}, {translateY: 0}],
  };
  return withAnchorPoint(transform, {x: 0, y: 0}, {width, height});
};

export const RoomScreen = ({ route, navigation }: NativeStackScreenProps<RootStackParamList, 'Room'>) => {
  const [isVideoOn, onChangeVideo] = useState(route.params.isVideoOn);
  const [isAudioOn, onChangeAudio] = useState(route.params.isAudioOn);

  useEffect(() => {
    return () => {
      disconnect();
    };
  }, []);

  const disconnect = () => {
    NativeModules.ECVideoCallsService.closeConnection();
    navigation.navigate('Home');
  };

  const toggleVideo = async () => {
    const result = await getCameraPermission();
    if (result) {
      const newValue = !isVideoOn;
      if (newValue) {
        NativeModules.ECVideoCallsService.enableVideo();
      } else {
        NativeModules.ECVideoCallsService.disableVideo();
      }
      onChangeVideo(newValue);
    }
  };

  const toggleAudio = async () => {
    const result = await getMicPermission();
    if (result) {
      const newValue = !isAudioOn;
      if (newValue) {
        NativeModules.ECVideoCallsService.enableAudio();
      } else {
        NativeModules.ECVideoCallsService.disableAudio();
      }
      onChangeAudio(newValue);
    }
  };

  const switchCamera = () => {
    NativeModules.ECVideoCallsService.flipCamera();
  };

  return (
    <View style={styles.root}>
      <View style={styles.container}>
        <View>
          <View style={[styles.previewWrapper]}>
            <ECLocalView style={[styles.mirror, styles.preview]}/>
          </View>

          <View style={styles.toolbar}>
            <Pressable style={[styles.btn]} onPress={switchCamera}>
              <SwitchCameraIcon/>
            </Pressable>
            <Pressable
              style={[styles.btn, isVideoOn ? styles.on : styles.off]}
              onPress={toggleVideo}>
              <CameraIcon/>
            </Pressable>
            <Pressable
              style={[styles.btn, isAudioOn ? styles.on : styles.off]}
              onPress={toggleAudio}>
              <MicrophoneIcon/>
            </Pressable>
            <Pressable style={[styles.btn, styles.drop]} onPress={disconnect}>
              <DropIcon/>
            </Pressable>
          </View>
        </View>
      </View>
      <ECRemoteView style={[styles.remote, getTransform()]}/>
    </View>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: 'gray',
    overflow: 'hidden',
  },
  container: {
    height: '100%',
    width: '100%',
    flexDirection: 'column',
    zIndex: 1,
    elevation: 1,
    alignItems: 'stretch',
    justifyContent: 'flex-end',
    position: 'absolute',
    padding: 20,
  },
  remote: {
    width,
    height,
    zIndex: 0,
    elevation: 0,
    borderRadius: 0,
    position: 'absolute',
    backgroundColor: 'silver',
  },
  mirror: {
    transform: [{scaleX: -1}],
  },
  previewWrapper: {
    borderRadius: 8,
  },
  preview: {
    height: 160,
    borderRadius: 8,
    marginVertical: 20,
    overflow: 'hidden',
    aspectRatio: 3 / 4,
  },
  toolbar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  btn: {
    width: 60,
    height: 60,
    borderRadius: 10,
    backgroundColor: '#2e264a',
    alignItems: 'center',
    justifyContent: 'center',
  },
  drop: {
    backgroundColor: '#e74c3c',
  },
  on: {
    backgroundColor: '#69ba3c',
  },
  off: {
    backgroundColor: '#2e264a',
  },
  text: {
    color: 'white',
    fontSize: 12,
  },
});
