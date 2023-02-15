package com.reactnativeawesomemodule

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.lifecycle.Observer
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import world.edgecenter.videocalls.ECSession
import world.edgecenter.videocalls.logger.LLog
import world.edgecenter.videocalls.remoteuser.RemoteUsers
import world.edgecenter.videocalls.ui.view.remoteuser.RemoteVideoSurfaceView

class ECRemoteViewManager(var mCallerContext: ReactApplicationContext) :
  SimpleViewManager<RemoteVideoSurfaceView>() {

  private var viewInstance: RemoteVideoSurfaceView? = null
  private var viewUserId: String? = null

  private val remoteUsersObserver = Observer { remoteUsers: RemoteUsers ->
    remoteUsers.list.firstOrNull()?.id?.let {
      viewUserId = it
      tryConnectView()
      LLog.d("ReactRemoteViewManager", "connected remote user: $viewUserId")
    } ?: run {
      viewUserId = null
    }
  }

  override fun createViewInstance(reactContext: ThemedReactContext): RemoteVideoSurfaceView {
    LLog.d("ReactRemoteViewManager", "createViewInstance")
    UiThreadUtil.runOnUiThread {
      ECSession.roomState.remoteUsers.observeForever(remoteUsersObserver)
    }

    viewInstance = RemoteVideoSurfaceView(reactContext.baseContext)
    viewInstance!!.setBorderRadius(30f)

    tryConnectView()

    return viewInstance!!
  }

  override fun getName(): String {
    return "ECRemoteView"
  }

  override fun onDropViewInstance(view: RemoteVideoSurfaceView) {
    LLog.d("ReactRemoteViewManager", "onDropViewInstance")
    viewInstance = null

    UiThreadUtil.runOnUiThread {
      ECSession.roomState.remoteUsers.removeObserver(remoteUsersObserver)
    }

    super.onDropViewInstance(view)
  }

  private fun tryConnectView() {
    viewUserId?.let { viewInstance?.connect(it) }
  }

}
