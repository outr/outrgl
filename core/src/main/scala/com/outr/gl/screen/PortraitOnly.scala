package com.outr.gl.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Orientation

trait PortraitOnly extends BaseScreen {
  override def render(delta: Float): Unit = {
    super.render(delta)

    if (MultiScreenApplication().orientation() == Orientation.Landscape) {
      stage.getRoot.setRotation(90.0f)
      stage.getRoot.setPosition(Gdx.graphics.getWidth, 0.0f)
    } else {
      stage.getRoot.setRotation(0.0f)
      stage.getRoot.setPosition(0.0f, 0.0f)
    }
  }
}
