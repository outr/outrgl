package com.outr.gl.screen

import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.utils.viewport.ScreenViewport

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait BaseScreen extends AbstractBaseScreen {
  lazy val stage = new Stage(new ScreenViewport)
}