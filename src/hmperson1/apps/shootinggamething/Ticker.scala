/**
 *
 */
package hmperson1.apps.shootinggamething

import java.util.concurrent.{ ScheduledThreadPoolExecutor, TimeUnit }
import scala.collection.immutable

/**
 * Does physics updates.
 *
 * @author HMPerson1
 */
class Ticker(pushState: GameState => Unit) extends Runnable {
  override def run(): Unit = {
    // TODO: Physics updates'n'stuff

    pushState(GameState(
      ((150, 300), 3.1415926535897932, 50),
      ((100, 300), 0, 75),
      immutable.Set[(Int, Int)]((25, 25), (100, 100)),
      immutable.Set[(Int, Int)]((10, 10), (50, 50))))
  }

  var stop: () => _ = _

  def start() = Ticker.start(this)
}

object Ticker {
  private val timer = new ScheduledThreadPoolExecutor(0)

  def apply(stateCallback: GameState => Unit): Ticker = new Ticker(stateCallback)

  def start(t: Ticker) = {
    val future = timer.scheduleAtFixedRate(t, 0, 50, TimeUnit.MILLISECONDS)
    t.stop = () => future.cancel(false)
  }
}
