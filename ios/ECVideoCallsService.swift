import Foundation
import EdgeCenterVideoCallsSDK
import WebRTC
import React

struct ConnectionOptions {
    var isVideoOn = false
    var isAudioOn = false
    var roomId = ""
    var displayName = ""
    var clientHostName = ""
}


@objc(ECVideoCallsService)
class ECVideoCallsService: RCTEventEmitter {

    private let bufferHandler = ECBufferHandler()
    private var client = ECSession.shared
    private var joinOptions: ConnectionOptions!

    override func supportedEvents() -> [String]! {
        return ["onConnectionChanged"]
    }

    @objc
    func openConnection(_ options: NSDictionary) {
        ECRoomLogger.activateLogger()

        client.cameraParams = ECCameraParams(cameraPosition: .front)

        let localUserParams = ECLocalUserParams(
            name: options["displayName"] as! String, isParticipant: true)

        let roomParams = ECRoomParams(
            id: options["roomId"] as! String,
            host: options["clientHostName"] as! String,
            // isWebinar: true,
            startWithCam: options["isVideoOn"] as! Bool,
            startWithMic: options["isAudioOn"] as! Bool
            // apiEvent: "https://my.domen/webhook"
        )

        client.connectionParams = (localUserParams, roomParams)

        try? client.startConnection()
        client.audioSessionActivate()
        client.roomListener = self

        bufferHandler.setBlurRadius(35)
        bufferHandler.mode = .detectFaceAndBlur
        client.webrtcBufferDelegate = self
    }

     @objc
     func enableBlur() {
         client.webrtcBufferDelegate = self
         print("blur on")
     }

     @objc
     func disableBlur() {
         client.webrtcBufferDelegate = nil
         print("blur off")
     }

    @objc
    func closeConnection() {
        client.close()
    }

    @objc
    func enableVideo() {
        toggleVideo(true)
    }

    @objc
    func disableVideo() {
        toggleVideo(false)
    }

    @objc
    func toggleVideo(_ isOn: Bool) {
        client.localUser?.toggleCam(isOn: isOn)
        print("toggleVideo: ", isOn)
    }

    @objc
    func enableAudio() {
        toggleAudio(true)
    }

    @objc
    func disableAudio() {
        toggleAudio(false)
    }

    @objc
    func toggleAudio(_ isOn: Bool) {
        client.localUser?.toggleMic(isOn: isOn)
        print("toggleAudio: ", isOn)
    }

    @objc
    func flipCamera() {
        client.localUser?.flipCam(completion: { error in
            if let error = error {
                debugPrint(error)
            }
        })
    }

    @objc
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [:]
    }


    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
}

extension ECVideoCallsService: MediaCapturerBufferDelegate {
 func mediaCapturerDidBuffer(_ pixelBuffer: CVPixelBuffer) {
   bufferHandler.processBuffer(pixelBuffer)
 }
}


extension ECVideoCallsService: ECRoomListener {
    func roomClient(_ client: ECRoomClient, waitingRoomIsActive: Bool) {

    }

    func roomClient(_ client: ECRoomClient, captureSession: AVCaptureSession, captureDevice: AVCaptureDevice) {
        guard let videoOutput = captureSession.outputs.first(where: { $0 is AVCaptureVideoDataOutput }) as? AVCaptureVideoDataOutput else {
          return
        }
        videoOutput.alwaysDiscardsLateVideoFrames = true
    }

    func roomClientHandle(error: ECError) {
//        if case .fatalError(let error) = error {
//          switch error {
//          case HTTPUpgradeError.notAnUpgrade(502):
//            try? client.startConnection()
//          default: break
//          }
//        }
      }

    func roomClientHandle(_ client: ECRoomClient, forAllRoles joinData: ECJoinData) {
        switch joinData {

        case .permissions(mediaStreams: let mediaStreams):
            print("permissions: ", mediaStreams)
        case .othersInRoom(remoteUsers: let remoteUsers):
            print("othersInRoom: ", remoteUsers)
        case .localUser(info: let info):
            print("localUser: ", info)
        default:
            print("default")
        }
    }

    func roomClientHandle(_ client: ECRoomClient, remoteUsersEvent: ECRemoteUsersEvent) {
        switch remoteUsersEvent {

        case .handleRemote(user: let user):
            print("handleRemote: ", user)
        case .closedRemote(userId: let userId):
            print("closedRemote: ", userId)
        case .activeSpeaker(remoteUserIds: let remoteUserIds):
            print("activeSpeaker: ", remoteUserIds)
        case .userSleep(id: let id, isSleeping: let isSleeping):
            print("userSleep: ", id, isSleeping)
        case .changeName(userId: let userId, new: let new, old: let old):
            print("changeName: ", userId, old, new)
        default:
            print("default")
        }
    }

    func roomClientHandle(_ client: ECRoomClient, mediaEvent: ECMediaEvent) {
        switch mediaEvent {

        case .produceLocalVideo(track: let track):
            print("RoomListener1: produceLocalVideoTrack:", track )
            DispatchQueue.main.async {
                track.add(ECViewsEnum.local)
            }
        case .produceLocalAudio(track: let track):
            print("RoomListener1: produceLocalAudio: ", track )
        case .didCloseLocalVideo(track: let track):
            print("RoomListener1: didCloseLocalVideo: ", track ?? "nil")
            DispatchQueue.main.async {
                track?.remove(ECViewsEnum.local)
            }
        case .didCloseLocalAudio(track: let track):
            print("RoomListener1: didCloseLocalAudio: ", track ?? "nil")
        case .handledRemoteVideo(videoObject: let videoObject):
            print("RoomListener1: handledRemoteVideoTrack:", videoObject)
            DispatchQueue.main.async {
                videoObject.rtcVideoTrack.add(ECViewsEnum.remote)
            }
        case .produceRemoteAudio(audioObject: let audioObject):
            print("RoomListener1: produceRemoteAudio: ", audioObject)
        case .didCloseRemoteVideo(byModerator: let byModerator, videoObject: let videoObject):
            print("RoomListener1: didCloseRemoteVideo: ", byModerator, videoObject)
            DispatchQueue.main.async {
                videoObject.rtcVideoTrack.remove(ECViewsEnum.remote)
            }
        case .didCloseRemoteAudio(byModerator: let byModerator, audioObject: let audioObject):
            print("RoomListener1: didCloseRemoteAudio: ", byModerator, audioObject)
        case .togglePermissionsByModerator(kind: let kind, status: let status):
            print("RoomListener1: togglePermissionsByModerator: ", kind, status)
        case .acceptedPermission(kind: let kind):
            print("RoomListener1: acceptedPermission: ", kind)
        case .disableProducerByModerator(media: let media):
            print("RoomListener1: disableProducerByModerator: ", media)
        default:
            print("RoomListener1: default")
        }
    }

    func roomClientHandle(_ client: ECRoomClient, connectionEvent: ECRoomConnectionEvent) {

        sendEvent(withName: "onConnectionChanged", body: ["connection": String(describing: connectionEvent)])
        switch connectionEvent {

        case .startToConnectWithServices:
            print("RoomListener2: startToConnectWithServices")
        case .successfullyConnectWithServices:
            print("RoomListener2: successfullyConnectWithServices")
        case .didConnected:
            print("RoomListener2: roomClient didConnected")
        case .reconnecting:
            print("RoomListener2: reconnecting")
        case .reconnectingFailed:
            print("RoomListener2: reconnectingFailed")
        case .socketDidDisconnected:
            print("RoomListener2: socketDidDisconnected")
        case .waitingForModeratorJoinAccept:
            print("RoomListener2: waitingForModeratorJoinAccept")
        case .moderatorRejectedLocalJoinRequest:
            print("RoomListener2: moderatorRejectedLocalJoinRequest")
        case .removedByModerator:
            print("RoomListener2: removedByModerator")
        default:
            print("RoomListener2: default")
        }
    }
}
