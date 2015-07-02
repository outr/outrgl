package com.outr.gl.actor.addon

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent
import org.powerscala.property.Property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait FocusSupport {
  this: Actor =>

  private val _focused = Property[Boolean](default = Some(false))
  val focused = _focused.readOnlyView
  def hasFocus = focused()
  def focus() = getStage.setKeyboardFocus(this)

  addListener(new FocusListener {
    override def keyboardFocusChanged(event: FocusEvent, actor: Actor, focused: Boolean) = {
      _focused := focused
    }

    override def scrollFocusChanged(event: FocusEvent, actor: Actor, focused: Boolean) = {
      _focused := focused
    }
  })
}
