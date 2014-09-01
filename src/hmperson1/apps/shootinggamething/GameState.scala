/**
 *
 */
package hmperson1.apps.shootinggamething

/**
 * Represents a state of the game. Basically it stores a bunch of numbers in a somewhat organized and unified fashion.
 *
 * @author HMPerson1
 */
class GameState(
  val player1: ((Int, Int), Double, Int, Int),
  val player2: ((Int, Int), Double, Int, Int),
  val bullets: Set[(Int, Int)],
  val rocks: Set[(Int, Int)])

object GameState {
  def apply(player1: ((Int, Int), Double, Int, Int), player2: ((Int, Int), Double, Int, Int), bullets: Set[(Int, Int)], rocks: Set[(Int, Int)]): GameState = {
    new GameState(player1, player2, bullets, rocks)
  }
}
