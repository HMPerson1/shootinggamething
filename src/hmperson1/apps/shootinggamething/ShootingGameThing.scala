/**
 *
 */
package hmperson1.apps.shootinggamething

import scala.swing.Swing
import scala.swing.Frame

/**
 * A game in which you shoot things.
 *
 * @author HMPerson1
 */
object ShootingGameThing {
  def main(args: Array[String]): Unit = {
    initGui()
  }

  private def initGui() = Swing.onEDTWait(() => {
    // TODO: Initialize GUI stuff
  })
}