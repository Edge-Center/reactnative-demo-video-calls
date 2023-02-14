package com.reactnativeawesomemodule

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import world.edgecenter.videocalls.ui.view.me.LocalUserVideoViewRenderer

class ECLocalViewManager(var mCallerContext: ReactApplicationContext) :
  SimpleViewManager<LocalUserVideoViewRenderer>() {

  override fun getName(): String {
    return "ECLocalView"
  }

  override fun onDropViewInstance(view: LocalUserVideoViewRenderer) {
    super.onDropViewInstance(view)
  }

  override fun createViewInstance(reactContext: ThemedReactContext): LocalUserVideoViewRenderer {

    return LocalUserVideoViewRenderer(reactContext.baseContext)
  }
}
