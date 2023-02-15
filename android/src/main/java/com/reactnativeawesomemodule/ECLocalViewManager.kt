package com.reactnativeawesomemodule

import android.graphics.Outline
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import world.edgecenter.videocalls.ui.view.me.LocalVideoSurfaceView


class ECLocalViewManager(var mCallerContext: ReactApplicationContext) :
  SimpleViewManager<LocalVideoSurfaceView>() {

  override fun getName(): String {
    return "ECLocalView"
  }

  override fun onDropViewInstance(view: LocalVideoSurfaceView) {
    super.onDropViewInstance(view)
  }


  override fun createViewInstance(reactContext: ThemedReactContext): LocalVideoSurfaceView {
    Log.d("Local", "createViewInstance")
    val view = LocalVideoSurfaceView(reactContext.baseContext)

    view.setBorderRadius(30f)

    return view
  }
}
