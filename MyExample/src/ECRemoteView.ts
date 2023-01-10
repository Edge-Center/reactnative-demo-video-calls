import { requireNativeComponent, StyleProp, ViewStyle } from 'react-native';
import type { PropsWithChildren } from 'react';

interface ViewProps extends PropsWithChildren<any> {
  style: StyleProp<ViewStyle>;
}
const ECRemoteView = requireNativeComponent<ViewProps>('ECRemoteView');
export default ECRemoteView;
