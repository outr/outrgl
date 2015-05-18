package com.outr.gl.actor

import com.badlogic.gdx.scenes.scene2d.{Group, Actor}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ActorWrapper[A <: Actor](val actor: A, padX: Float = 0.0f, padY: Float = 0.0f) extends Group {
  setX(actor.getX - padX)
  setY(actor.getY - padY)
  setWidth(actor.getWidth + (padX * 2.0f))
  setHeight(actor.getHeight + (padY * 2.0f))
  actor.setX(padX)
  actor.setY(padY)

  addActor(actor)
}