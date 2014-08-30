/**
 *
 */
package hmperson1.apps.shootinggamething

import java.awt.{ Color, Dimension }
import scala.swing.{ Component, Graphics2D }

/**
 * Renders stuff.
 *
 * @author HMPerson1
 */
class Renderer(state: () => GameState) extends Component {

  preferredSize = new Dimension(500, 500)
  minimumSize = preferredSize

  override def paint(g: Graphics2D): Unit = {
    val s = state()

    g.setColor(Color.WHITE)
    g.fillRect(0, 0, size.width, size.height)
    // TODO: Render stuff

    peer.repaint()
  }
}
