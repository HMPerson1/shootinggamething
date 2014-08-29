/**
 *
 */
package hmperson1.apps.shootinggamething

import java.awt.Graphics
import javax.swing.JComponent
import java.awt.Graphics2D

/**
 * Renders stuff.
 *
 * @author HMPerson1
 */
class Renderer extends JComponent {
  override def paint(graphics: Graphics): Unit = {
    val g = graphics.asInstanceOf[Graphics2D]
    
    // TODO: Render stuff
  }
}