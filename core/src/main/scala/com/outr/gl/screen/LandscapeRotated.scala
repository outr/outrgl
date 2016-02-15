package com.outr.gl.screen

import com.badlogic.gdx.Gdx

trait LandscapeRotated extends BaseScreen {
  override def render(delta: Float): Unit = {
    super.render(delta)

    stage.getRoot.setRotation(-90.0f)
    stage.getRoot.setPosition(0.0f, Gdx.graphics.getHeight)
  }
}
