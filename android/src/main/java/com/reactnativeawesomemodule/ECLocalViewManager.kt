package com.reactnativeawesomemodule

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import world.edgecenter.videocalls.ui.view.me.LocalVideoView


class ECLocalViewManager(var mCallerContext: ReactApplicationContext) :
  SimpleViewManager<LocalVideoView>() {

  override fun getName(): String {
    return "ECLocalView"
  }

  override fun onDropViewInstance(view: LocalVideoView) {
    super.onDropViewInstance(view)
  }


  override fun createViewInstance(reactContext: ThemedReactContext): LocalVideoView {
    Log.d("Local", "createViewInstance")
    return LocalVideoView(reactContext.baseContext)
  }
}
