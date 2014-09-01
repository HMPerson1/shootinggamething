/**
 *
 */
package hmperson1.apps.shootinggamething

import scala.swing.{ Frame, Swing }
import java.awt.event.KeyListener

/**
 * A game in which you shoot things.
 *
 * @author HMPerson1
 */
class ShootingGameThing {

  private val ticker = Ticker(state_=)
  private var currentState: GameState = _

  def state_=(state: GameState): Unit = synchronized { currentState = state }
  def state: GameState = synchronized { currentState }

  // Start!
  Swing.onEDTWait {
    val frame = new Frame { override def closeOperation() = ticker.stop() }
    val renderer = new Renderer(state _)
    renderer.keys.reactions += ticker.keyReaction
    frame.contents = renderer
    frame.title = "Shooting Game Thing"
    frame.pack()
    frame.minimumSize = frame.size

    ticker.start()
    renderer.requestFocusInWindow()
    frame.visible = true
  }
}

object ShootingGameThing {
  val PlayerRadius = 20
  val BulletRadius = 4
  val RockRadius = 15

  def main(args: Array[String]): Unit = {
    new ShootingGameThing
  }
}
