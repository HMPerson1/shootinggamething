/**
 *
 */
package hmperson1.apps.shootinggamething

import scala.swing.{ Frame, Swing }

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

  def start() = Swing.onEDT {
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

  val ArenaSize = 500

  val MaxHealth = 100
  val MaxTimer = 50

  val PlayerRadius = 20
  val BulletRadius = 4
  val RockRadius = 15

  def main(args: Array[String]): Unit = {
    (new ShootingGameThing).start
  }
}
