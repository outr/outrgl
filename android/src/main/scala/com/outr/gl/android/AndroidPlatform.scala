package com.outr.gl.android

import android.content.pm.ActivityInfo
import android.os.Bundle
import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.backends.android.{AndroidApplication, AndroidApplicationConfiguration}
import com.outr.gl.Platform

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait AndroidPlatform extends AndroidApplication with Platform[AndroidApplicationConfiguration] {
  override def platformId: String = "android"

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    val config = new AndroidApplicationConfiguration
    config.useAccelerometer = true
    config.useCompass = true
    config.useWakelock = true
    config.hideStatusBar = true
    init(config)
    initialize(application, config)
  }

  override def orientation(orientation: Orientation) = {
    val o = orientation match {
      case Orientation.Portrait => ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      case Orientation.Landscape => ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    setRequestedOrientation(o)
  }
}
