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

import java.awt.{ Color, Dimension, Polygon }
import java.awt.event.WindowEvent
import javax.swing.JFrame
import scala.language.postfixOps
import scala.math.{ Pi, cos, sin }
import scala.swing.{ Component, Graphics2D }
import ShootingGameThing._

/**
 * Renders stuff.
 *
 * @author HMPerson1
 */
class Renderer(state: () => Option[GameState]) extends Component {
  import Renderer._

  preferredSize = new Dimension(SizeHud + SizeArena + SizeHud, SizeArena)
  minimumSize = preferredSize

  override def paint(g: Graphics2D): Unit = {
    // Constantly redraw
    peer.repaint()

    val s = state() match {
      case Some(x) => x
      case None    => return
    }

    // Close the window when the game is over
    if (!s.running) closeWindow()

    // Clear
    g.setColor(ColorClear)
    g.fillRect(0, 0, size.width, size.height)

    // Clip:
    // hud-arena-hud
    g.setClip(SizeHud, 0, size.width - SizeHud - SizeHud, size.width)
    g.translate(SizeHud, 0)

    // Paint rocks
    for (p <- s.rocks) {
      val (x, y) = p
      paintCircle(g, x, y, RadiusRock, ColorRockFill, ColorRockOutline)
    }

    // Paint players
    paintPlayer(g, s.player1, ColorPlayer1Fill)
    paintPlayer(g, s.player2, ColorPlayer2Fill)

    // Paint bullets
    for (b <- s.bullets) {
      val (x, y) = b
      paintCircle(g, x, y, RadiusBullet, ColorBulletFill, ColorBulletOutline)
    }

    // Reset clip
    g.translate(-SizeHud, 0)
    g.setClip(null)

    // Paint health bars
    g.setColor(ColorPlayer1Health)
    g.fillRect(0, size.height - s.player1._3 * SizeArena / HealthMax, SizePlayerHealth, s.player1._3 * SizeArena / HealthMax)
    g.setColor(ColorPlayer2Health)
    g.fillRect(size.width - SizePlayerHealth, size.height - s.player2._3 * SizeArena / HealthMax, SizePlayerHealth, s.player2._3 * SizeArena / HealthMax)

    // Paint shooting timer bars
    g.setColor(ColorPlayerTimer)
    g.fillRect(SizePlayerHealth, s.player1._4 * SizeArena / TimerMax, SizePlayerTimer, size.height - s.player1._4 * SizeArena / TimerMax)
    g.fillRect(size.width - SizePlayerHealth - SizePlayerTimer, s.player2._4 * SizeArena / TimerMax, SizePlayerTimer, size.height - s.player2._4 * SizeArena / TimerMax)
  }

  private def closeWindow() = {
    val window = peer.getTopLevelAncestor().asInstanceOf[JFrame]
    window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING))
  }
}

object Renderer {

  private val ColorClear = Color.WHITE

  private val ColorRockFill = Color.LIGHT_GRAY
  private val ColorRockOutline = Color.BLACK

  private val ColorBulletOutline = Color.BLACK
  private val ColorBulletFill = Color.BLACK

  private val ColorPlayerHitOutline = new Color(0x80000000, true)
  private val ColorPlayerHitFill = new Color(0x10000000, true)
  private val ColorPlayerPoint = Color.RED
  private val ColorPlayerOutline = Color.BLACK
  private val ColorPlayer1Fill = new Color(0x40FF0000, true)
  private val ColorPlayer2Fill = new Color(0x400000FF, true)

  private val ColorPlayer1Health = Color.RED
  private val ColorPlayer2Health = Color.BLUE
  private val ColorPlayerTimer = Color.BLACK

  private val SizePlayerPoint = 6

  private val SizePlayerHealth = 15
  private val SizePlayerTimer = 5
  private val SizeHud = SizePlayerHealth + SizePlayerTimer

  private def paintCircle(g: Graphics2D, cx: Int, cy: Int, r: Int, fill: Color, outline: Color) {
    val x = cx - r
    val y = cy - r
    val h = 2 * r

    g.setColor(fill)
    g.fillOval(x, y, h, h)
    g.setColor(outline)
    g.drawOval(x, y, h, h)
  }

  private def paintPlayer(g: Graphics2D, player: ((Int, Int), Double, Int, Int), color: Color) {

    val ((x, y), rot, _, _) = player
    val xs = (0 to 2) map { n: Int => (+RadiusPlayer * cos(rot + n * 2 * Pi / 3) + x).round.toInt }
    val ys = (0 to 2) map { n: Int => (-RadiusPlayer * sin(rot + n * 2 * Pi / 3) + y).round.toInt }
    val base = new Polygon(xs toArray, ys toArray, 3)

    // Collision circle
    paintCircle(g, x, y, RadiusPlayer, ColorPlayerHitFill, ColorPlayerHitOutline)

    // Base
    g.setColor(color)
    g.fillPolygon(base)

    // Point
    g.setColor(ColorPlayerPoint)
    val c = g.getClip()
    g.clip(base)
    g.fillOval(xs(0) - SizePlayerPoint, ys(0) - SizePlayerPoint, 2 * SizePlayerPoint, 2 * SizePlayerPoint)
    g.setClip(c)

    // Outline
    g.setColor(ColorPlayerOutline)
    g.drawPolygon(base)
  }
}
