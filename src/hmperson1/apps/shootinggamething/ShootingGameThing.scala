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

  // Start!
  Swing.onEDTWait {
    val frame = new Frame { override def closeOperation() = ticker.stop() }
    frame.contents = new Renderer(state _)
    frame.title = "Shooting Game Thing"
    frame.pack()

    ticker.start()
    frame.visible = true
  }
}

object ShootingGameThing {
  def main(args: Array[String]): Unit = {
    new ShootingGameThing
  }
}
