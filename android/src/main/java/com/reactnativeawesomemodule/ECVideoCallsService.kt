package com.reactnativeawesomemodule

import android.app.Application
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.SparseIntArray
import android.view.Surface
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import org.webrtc.VideoFrame
import world.edgecenter.videocalls.ECSession
import world.edgecenter.videocalls.localuser.LocalUserInfo
import world.edgecenter.videocalls.logger.LLog
import world.edgecenter.videocalls.model.DEFAULT_LENGTH_RANDOM_STRING
import world.edgecenter.videocalls.model.UserRole
import world.edgecenter.videocalls.network.client.VideoFrameListener
import world.edgecenter.videocalls.room.RoomParams
import world.edgecenter.videocalls.utils.Utils
import world.edgecenter.videocalls.utils.image.VideoFrameConverter
import world.edgecenter.videocalls.utils.image.VideoFrameFaceDetector


class ECVideoCallsService(
  reactContext: ReactApplicationContext,
  private val application: Application,
) : ReactContextBaseJavaModule(reactContext) {
  private val ORIENTATIONS = SparseIntArray()

  init {
    ORIENTATIONS.append(Surface.ROTATION_0, 0)
    ORIENTATIONS.append(Surface.ROTATION_90, 90)
    ORIENTATIONS.append(Surface.ROTATION_180, 180)
    ORIENTATIONS.append(Surface.ROTATION_270, 270)
  }

  /**
   * Get the angle by which an image must be rotated given the device's current
   * orientation.
   */
  @Throws(CameraAccessException::class)
  private fun getRotationCompensation(cameraId: String): Int {
    // Get the device's current rotation relative to its "native" orientation.
    // Then, from the ORIENTATIONS table, look up the angle the image must be
    // rotated to compensate for the device's rotation.
    val deviceRotation = currentActivity?.windowManager?.defaultDisplay?.rotation

    var rotationCompensation = ORIENTATIONS.get(deviceRotation ?: 0)

    // Get the device's sensor orientation.
    val cameraManager = application.getSystemService(CAMERA_SERVICE) as CameraManager
    val sensorOrientation =
      cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SENSOR_ORIENTATION)!!

    val isFrontFacing = Utils.isFrontFacingCamera(cameraId)

    rotationCompensation = if (isFrontFacing) {
      (sensorOrientation + rotationCompensation) % 360
    } else {
      (sensorOrientation - rotationCompensation + 360) % 360
    }

    LLog.d(
      "ECVideoCallsService",
      "isFrontFacing $isFrontFacing " +
        "\nsensorOrientation $sensorOrientation " +
        "\ndeviceRotation $deviceRotation " +
        "\nrotationCompensation $rotationCompensation"
    )
    return rotationCompensation
  }

  /**
   * Get the angle by which an image must be rotated given the device's current
   * orientation.
   */

  private val frameConverter = VideoFrameConverter()
  private val videoFrameFaceDetector = VideoFrameFaceDetector().also {
    it.faceDetectingFrameInterval = 30
  }
//  private val videoFrameSegmenter = VideoFrameSegmenter()

  private val videoFrameListener = object : VideoFrameListener {

    private var bgBitmap: Bitmap? = null

    private fun checkAndCreateBgBitmap(width: Int, height: Int): Bitmap {
      if (bgBitmap == null || bgBitmap!!.width != width || bgBitmap!!.height != height) {
        bgBitmap = Bitmap.createScaledBitmap(
          BitmapFactory.decodeResource(application.resources, R.drawable.bg), width, height, true
        )
      }

      return bgBitmap!!
    }

//    private fun applyBackground(frame: VideoFrame, sink: (frame: VideoFrame) -> Unit) {
//      val inputImage = frameConverter.frameToInputImage(frame, 0)
//      videoFrameSegmenter.getSegmentationMask(inputImage) { mask: SegmentationMask ->
//        val bgBitmap = checkAndCreateBgBitmap(frame.buffer.width, frame.buffer.height)
//
////      val bgFrame = frameConverter.applyBackgroundToFrame(frame, bgBitmap, mask)
//
//        val outBitmap = frameConverter.applyBackgroundToBitmap(inputImage.bitmapInternal!!, bgBitmap, mask)
//        val bgFrame = frameConverter.bitmapToFrame(outBitmap, frame.rotation, frame.timestampNs)
//
//        sink.invoke(bgFrame)
//
//        frame.buffer.release()
//      }
//    }

    override fun onFrameCaptured(frame: VideoFrame, sink: (frame: VideoFrame) -> Unit) {

      val getInputImage = getInputImage@{ ->
        val cameraName = Utils.getCameraName()
        val angle = cameraName?.let {
          getRotationCompensation(it)
        }

        return@getInputImage frameConverter.frameToInputImage(frame, angle ?: 0)
      }

      val hasFace = videoFrameFaceDetector.hasFace(getInputImage)

      val outputFrame = if (hasFace) {
        frame
      } else {
        frameConverter.blurFrame(frame, 40)
      }

      sink.invoke(outputFrame)
    }
  }

  init {
    runOnUiThread {
      ECSession.instance.init(application)
    }

    ECSession.instance.videoFrameListener = videoFrameListener
  }

  @ReactMethod
  fun closeConnection() {
    runOnUiThread {
      ECSession.instance.close()
    }
  }

  @ReactMethod
  fun disableAudio() {
    ECSession.instance.localUser?.toggleMic(false)
  }

  @ReactMethod
  fun disableVideo() {
    ECSession.instance.localUser?.toggleCam(false)
  }

  @ReactMethod
  fun enableAudio() {
    ECSession.instance.localUser?.toggleMic(true)
  }

  @ReactMethod
  fun enableVideo() {
    ECSession.instance.localUser?.toggleCam(true)
  }

  @ReactMethod
  fun flipCamera() {
    ECSession.instance.localUser?.flipCam()
  }

  @ReactMethod
  fun openConnection(options: ReadableMap) {
    runOnUiThread {

      val userRole = when (options.getString("role") ?: "") {
        "common" -> UserRole.COMMON
        "moderator" -> UserRole.MODERATOR
        "participant" -> UserRole.PARTICIPANT
        else -> UserRole.UNKNOWN
      }

      val userInfo = LocalUserInfo(
        displayName = options.getString("displayName") ?: "User${Utils.getRandomString(3)}",
        role = userRole,
        userId = options.getString("userId") ?: Utils.getRandomString(DEFAULT_LENGTH_RANDOM_STRING)
      )

      val roomParams = RoomParams(
        roomId = options.getString("roomId") ?: "",
        hostName = options.getString("clientHostName") ?: "",
        startWithCam = options.getBoolean("isVideoOn"),
        startWithMic = options.getBoolean("isAudioOn"),
        isWebinar = true,
        // apiEvent = "https://my.backend/webhook"
      )

      ECSession.instance.setConnectionParams(userInfo, roomParams)
      ECSession.instance.connect()
    }
  }

  override fun getName(): String {
    return "ECVideoCallsService"
  }

}
