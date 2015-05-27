package com.outr.gl.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.{Image, Label}
import com.badlogic.gdx.scenes.scene2d.{Event, EventListener, Group, InputEvent}
import com.outr.gl.TextureManager
import com.outr.gl.screen.{AbstractBaseScreen, MultiScreenApplication}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ErrorDisplay(application: MultiScreenApplication, textures: => TextureManager, style: => LabelStyle) extends Function1[Throwable, Unit] {
  private var group: Group = _

  override def apply(t: Throwable) = try {
    removeError()

    val message = s"$t\n${t.getStackTrace.mkString("\n")}"

    val label = new Label(message, style)
    label.setWrap(true)
    label.setWidth(Gdx.graphics.getWidth - 20.0f)
    label.setX(10.0f)
    label.setY(10.0f)
    label.setColor(Color.WHITE)
    val background = new Image(textures.Pixel)
    background.setColor(Color.BLACK)
    background.setWidth(Gdx.graphics.getWidth.toFloat)
    background.setHeight(label.getPrefHeight + 20.0f)
    application.screens.headOption match {
      case Some(screen) => {
        group = new Group()
        group.addActor(background)
        group.addActor(label)
        group.addListener(new EventListener {
          override def handle(event: Event) = event match {
            case evt: InputEvent if evt.getType == InputEvent.Type.touchDown => {
              removeError()
              true
            }
            case _ => false // Ignore
          }
        })
        screen.asInstanceOf[AbstractBaseScreen].stage.addActor(group)
        t.printStackTrace()
      }
      case None => throw t
    }
  } catch {
    case exc: Throwable => throw new RuntimeException(s"Error while attempting to create visual error.", exc)
  }

  private def removeError() = if (group != null) {
    group.getParent.removeActor(group)
    group = null
  }
}
