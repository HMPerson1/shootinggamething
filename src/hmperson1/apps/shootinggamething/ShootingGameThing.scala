/**
 *
 */
package hmperson1.apps.shootinggamething

import scala.swing.{ Component, Frame, Swing }
import javax.swing.UIManager

/**
 * A game in which you shoot things.
 *
 * @author HMPerson1
 */
class ShootingGameThing {
  import ShootingGameThing._

  private val ticker = Ticker(state_=)
  private var currentState: GameState = _

  def state_=(state: GameState): Unit = synchronized { currentState = state }
  def state: GameState = synchronized { currentState }

  def start() = Swing.onEDT {
    val frame = new Frame { override def closeOperation() = { ticker.stop(); endGame(ticker.stateString) } }
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

  private var startPanel: StartPanel = _

  def main(args: Array[String]): Unit = Swing.onEDT {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

    startPanel = new StartPanel(() => (new ShootingGameThing).start, sys.exit _)
    val frame = new Frame { override def closeOperation() = sys.exit }
    frame.contents = new Component { override lazy val peer = startPanel }
    frame.title = "Shooting Game Thing"
    frame.pack()
    frame.resizable = false
    frame.visible = true
  }

  private def endGame(result: String): Unit = {
    startPanel.addToHistory(result)
  }
}
