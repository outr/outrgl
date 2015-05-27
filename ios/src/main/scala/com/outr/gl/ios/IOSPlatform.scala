package com.outr.gl.ios

import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.backends.iosrobovm.{IOSApplicationConfiguration, IOSApplication}
import com.outr.gl.Platform

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait IOSPlatform extends IOSApplication.Delegate with Platform[IOSApplicationConfiguration] {
  override protected def createApplication(): IOSApplication = {
    val config = new IOSApplicationConfiguration
    init(config)
    new IOSApplication(application, config)
  }

  override def orientation(orientation: Orientation) = {}
}
