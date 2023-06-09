#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(ECVideoCallsService, RCTEventEmitter)
    RCT_EXTERN_METHOD(openConnection: (NSDictionary*)roomOptions)
    RCT_EXTERN_METHOD(closeConnection)
    RCT_EXTERN_METHOD(toggleVideo: BOOL)
    RCT_EXTERN_METHOD(toggleAudio: BOOL)
    RCT_EXTERN_METHOD(enableAudio)
    RCT_EXTERN_METHOD(disableAudio)
    RCT_EXTERN_METHOD(enableVideo)
    RCT_EXTERN_METHOD(disableVideo)
    RCT_EXTERN_METHOD(flipCamera)
    RCT_EXTERN_METHOD(enableBlur)
    RCT_EXTERN_METHOD(disableBlur)

@end
