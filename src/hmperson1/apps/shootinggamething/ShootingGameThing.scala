/*
 * Copyright (C) 2014 HMPerson1 <hmperson1@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

  private def start() = Swing.onEDT {
    val ticker = Ticker(state_=)
    val frame = new Frame { override def closeOperation() = { ticker.stop(); addToHistory(ticker) } }
    val renderer = new Renderer(state _)

    frame.contents = renderer
    frame.title = "Shooting Game Thing"
    frame.minimumSize = frame.size

    renderer.keys.reactions += ticker.keyReaction
    renderer.requestFocusInWindow()

    ticker.start()
    frame.visible = true
  }

  private var currentState: Option[GameState] = None

  private def state_=(state: GameState): Unit = synchronized { currentState = Some(state) }
  private def state: Option[GameState] = synchronized { currentState }
}

object ShootingGameThing {

  val SizeArena = 500

  val HealthMax = 100
  val TimerMax = 50

  val RadiusPlayer = 20
  val RadiusBullet = 4
  val RadiusRock = 15

  private var startPanel: StartPanel = _

  def main(args: Array[String]): Unit = Swing.onEDT {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

    val frame = new Frame { override def closeOperation() = sys.exit }
    startPanel = new StartPanel(startGame _, sys.exit _)

    frame.contents = new Component { override lazy val peer = startPanel }
    frame.title = "Shooting Game Thing"
    frame.resizable = false
    frame.visible = true
  }

  /**
   * Starts a new game.
   */
  private def startGame(): Unit = (new ShootingGameThing).start()

  /**
   * Adds the given game to the start panel's history.
   */
  private def addToHistory(game: Ticker): Unit = {
    startPanel.addToHistory(game.result match {
      case GameResult.Tie        => "Tie!"
      case GameResult.Won1       => "Player 1 Won!"
      case GameResult.Won2       => "Player 2 Won!"
      case GameResult.InProgress => "Game ended unexpectedly"
    })
  }
}
