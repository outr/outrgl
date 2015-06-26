package com.outr.gl.screen

import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.outr.gl._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait BaseScreen extends AbstractBaseScreen {
  lazy val stage = new Stage(new ScreenViewport)

  def onTouch(actors: Actor*)(f: => Unit) = {
    actors.foreach {
      case a => a.onTouch(f)
    }
  }
}