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

/**
 * Represents a state of the game. Basically it stores a bunch of numbers in a somewhat organized and unified fashion.
 *
 * @author HMPerson1
 */
class GameState(
  val player1: ((Int, Int), Double, Int, Int),
  val player2: ((Int, Int), Double, Int, Int),
  val bullets: Set[(Int, Int)],
  val rocks: Set[(Int, Int)],
  val running: Boolean)

object GameState {
  def apply(player1: ((Int, Int), Double, Int, Int),
            player2: ((Int, Int), Double, Int, Int),
            bullets: Set[(Int, Int)],
            rocks: Set[(Int, Int)],
            running: Boolean): GameState = {
    new GameState(player1, player2, bullets, rocks, running)
  }
}

/**
 * All possible ending states of a game.
 *
 * @author HMPerson1
 */
object GameResult extends Enumeration {
  type GameResult = Value
  val Tie, Won1, Won2, InProgress = Value
}
