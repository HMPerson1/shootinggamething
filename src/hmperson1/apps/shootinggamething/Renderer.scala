/**
 *
 */
package hmperson1.apps.shootinggamething

import java.awt.{Color, Dimension, Polygon}
import scala.math.{Pi, cos, sin}
import scala.swing.{Component, Graphics2D}
import ShootingGameThing.{ ArenaSize, BulletRadius, MaxHealth, MaxTimer, PlayerRadius, RockRadius }

/**
 * Renders stuff.
 *
 * @author HMPerson1
 */
class Renderer(state: () => GameState) extends Component {
  import Renderer._

  preferredSize = new Dimension(SizeHud + ArenaSize + SizeHud, ArenaSize)
  minimumSize = preferredSize

  override def paint(g: Graphics2D): Unit = {
    val s = state()

    // Clear
    g.setColor(ColorClear)
    g.fillRect(0, 0, size.width, size.height)

    // Clip:
    // 20 - 500 - 20
    // hud-arena-hud
    g.setClip(SizeHud, 0, size.width - SizeHud - SizeHud, size.width)
    g.translate(SizeHud, 0)

    // Paint rocks
    for (p <- s.rocks) {
      val (x, y) = p
      paintCircle(g, x, y, RockRadius, ColorRockFill, ColorRockOutline)
    }

    // Paint players
    paintPlayer(g, s.player1, ColorPlayer1Fill)
    paintPlayer(g, s.player2, ColorPlayer2Fill)

    // Paint bullets
    for (b <- s.bullets) {
      val (x, y) = b
      paintCircle(g, x, y, BulletRadius, ColorBulletFill, ColorBulletOutline)
    }

    // Reset clip
    g.translate(-SizeHud, 0)
    g.setClip(null)

    // Paint health bars
    g.setColor(ColorPlayer1Health)
    g.fillRect(0, size.height - s.player1._3 * ArenaSize / MaxHealth, SizePlayerHealth, s.player1._3 * ArenaSize / MaxHealth)
    g.setColor(ColorPlayer2Health)
    g.fillRect(size.width - SizePlayerHealth, size.height - s.player2._3 * ArenaSize / MaxHealth, SizePlayerHealth, s.player2._3 * ArenaSize / MaxHealth)

    // Paint shooting timer bars
    g.setColor(ColorPlayerTimer)
    g.fillRect(SizePlayerHealth, s.player1._4 * ArenaSize / MaxTimer, SizePlayerTimer, size.height - s.player1._4 * ArenaSize / MaxTimer)
    g.fillRect(size.width - SizePlayerHealth - SizePlayerTimer, s.player2._4 * ArenaSize / MaxTimer, SizePlayerTimer, size.height - s.player2._4 * ArenaSize / MaxTimer)

    peer.repaint()
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
    val xs = (0 to 2) map { n: Int => (+PlayerRadius * cos(rot + n * 2 * Pi / 3) + x).round.toInt }
    val ys = (0 to 2) map { n: Int => (-PlayerRadius * sin(rot + n * 2 * Pi / 3) + y).round.toInt }
    val base = new Polygon(xs toArray, ys toArray, 3)

    // Collision circle
    paintCircle(g, x, y, PlayerRadius, ColorPlayerHitFill, ColorPlayerHitOutline)

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
