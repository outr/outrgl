package com.outr.gl.actor

import com.badlogic.gdx.scenes.scene2d.{Actor, Group}
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.outr.gl._
import com.outr.gl.screen.AbstractBaseScreen
import org.powerscala.concurrent.AtomicBoolean
import org.powerscala.property.Property
import scala.collection.JavaConverters._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SnapScrollPane[T <: Actor](screen: AbstractBaseScreen, widget: Group, itemHeight: Float, adjustmentMultiplier: Float = 1.0f) extends ScrollPane(widget) {
  implicit def abstractBaseScreen: AbstractBaseScreen = screen

  private val firstOffset = (itemHeight / 2.0f) - itemHeight * 0.3f

  val selectedIndex = Property[Int](default = Some(0))
  def selected: T = widget.getChildren.get(selectedIndex() + 1).asInstanceOf[T]
  def entries: Vector[T] = widget.getChildren.asScala.toVector.collect {
    case child if child != paddingTop && child != paddingBottom => child.asInstanceOf[T]
  }
  def entryCount = widget.getChildren.size - 2
  private val changingSelected = new AtomicBoolean

  private var targetY = -1.0f

  private val paddingTop = createPadding()
  private val paddingBottom = createPadding()

  private def paddingHeight = itemHeight * 2.2f

  setScrollingDisabled(true, false)

  selectedIndex.change.on { evt =>
    if (!changingSelected.get()) {
      val y = firstOffset + (evt.newValue * itemHeight)
      scrollY(y)
    }
  }

  clear()

  private def createPadding() = {
    val padding = new Group
    padding.setSize(getWidth, paddingHeight)
    padding
  }

  def add(actor: T) = {
    widget.addActorBefore(paddingBottom, actor)
  }

  override def clear(): Unit = {
    widget.clear()
    widget.addActor(paddingTop)
    widget.addActor(paddingBottom)
  }

  override def act(delta: Float) = {
    super.act(delta)

    if (screen.input.touching.nonEmpty || isFlinging || isDragging || getVelocityY != 0.0f) {
      targetY = -1.0f
    } else if (targetY == -1.0f) {
      targetY = getScrollY % itemHeight
      val adjusted = (getScrollY - targetY + itemHeight / 2.0f) - itemHeight * 0.3f
      scrollY(adjusted)
      val index = math.round((adjusted - firstOffset) / itemHeight)
      changingSelected.set(true)
      try {
        selectedIndex := index
      } finally {
        changingSelected.set(false)
      }
    }
  }
}