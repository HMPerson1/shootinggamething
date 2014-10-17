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

import java.util.concurrent.{ ScheduledThreadPoolExecutor, TimeUnit }
import scala.collection.mutable
import scala.language.postfixOps
import scala.swing.Reactions.Reaction
import scala.swing.event.{ Event, Key, KeyPressed, KeyReleased }
import scala.util.Random
import ShootingGameThing._

/**
 * Does physics updates.
 * This could probably be done with Box2D, but what would be the fun in that?
 *
 * @author HMPerson1
 */
class Ticker(pushState: GameState => Unit) extends Runnable {
  import Ticker._
  
  private var running = false;

  private var player1Pos = (SizeArena * 0.3, SizeArena * 0.5)
  private var player2Pos = (SizeArena * 0.7, SizeArena * 0.5)
  private var player1Vel = (0.0, 0.0)
  private var player2Vel = (0.0, 0.0)
  private var player1Acel = (0.0, 0.0)
  private var player2Acel = (0.0, 0.0)
  private var player1Rot = 0.0
  private var player2Rot = math.Pi
  private var player1RotVel = 0.0
  private var player2RotVel = 0.0
  private var player1Cd = 0
  private var player2Cd = 0
  private var player1Shoot = false
  private var player2Shoot = false
  private var player1Health = HealthMax
  private var player2Health = HealthMax

  /**
   * Bullets shot by player 1
   */
  private var bullet1s = mutable.Set[((Double, Double), (Double, Double))]()
  /**
   * Bullets shot by player 2
   */
  private var bullet2s = mutable.Set[((Double, Double), (Double, Double))]()

  private var rocks = mutable.Set[((Double, Double), (Double, Double))]()

  /**
   * Steps one step forward in time.
   */
  override def run(): Unit = {
    spawnRocks()
    doShooting()
    updatePos()
    doCollisions()
    checkDeath()

    def roundPair(x: (Double, Double)): (Int, Int) = {
      (x._1.round.toInt, x._2.round.toInt)
    }

    pushState(GameState(
      (roundPair(player1Pos), player1Rot, player1Health, player1Cd),
      (roundPair(player2Pos), player2Rot, player2Health, player2Cd),
      (bullet1s ++ bullet2s unzip)._1 map roundPair toSet,
      (rocks unzip)._1 map roundPair toSet,
      running
    ))
  }

  /**
   * Reacts to key events by accelerating and/or rotating players.
   */
  val keyReaction: Reaction = new Reaction {
    def apply(e: Event): Unit = {
      e match {
        case e: KeyPressed =>
          e.key match {
            // Player 1
            case Key.A => player1Acel = (-AccelPlayer, player1Acel._2)
            case Key.D => player1Acel = (AccelPlayer, player1Acel._2)
            case Key.W => player1Acel = (player1Acel._1, -AccelPlayer)
            case Key.S => player1Acel = (player1Acel._1, AccelPlayer)
            case Key.Q => player1RotVel = RotVelPlayer
            case Key.E => player1RotVel = -RotVelPlayer
            // Player 2
            case Key.J => player2Acel = (-AccelPlayer, player2Acel._2)
            case Key.L => player2Acel = (AccelPlayer, player2Acel._2)
            case Key.I => player2Acel = (player2Acel._1, -AccelPlayer)
            case Key.K => player2Acel = (player2Acel._1, AccelPlayer)
            case Key.U => player2RotVel = RotVelPlayer
            case Key.O => player2RotVel = -RotVelPlayer
            // Shooting
            case Key.Shift => e.location match {
              case Key.Location.Left  => if (player1Cd <= 0) player1Shoot = true
              case Key.Location.Right => if (player2Cd <= 0) player2Shoot = true
            }

            case _ => return
          }
          e.consume()
        case e: KeyReleased =>
          e.key match {
            // Player 1
            case Key.A => player1Acel = (0.0, player1Acel._2)
            case Key.D => player1Acel = (0.0, player1Acel._2)
            case Key.W => player1Acel = (player1Acel._1, 0.0)
            case Key.S => player1Acel = (player1Acel._1, 0.0)
            case Key.Q => player1RotVel = 0
            case Key.E => player1RotVel = 0
            // Player 2
            case Key.J => player2Acel = (0.0, player2Acel._2)
            case Key.L => player2Acel = (0.0, player2Acel._2)
            case Key.I => player2Acel = (player2Acel._1, 0.0)
            case Key.K => player2Acel = (player2Acel._1, 0.0)
            case Key.U => player2RotVel = 0
            case Key.O => player2RotVel = 0

            case _     => return
          }
          e.consume()
      }
    }

    def isDefinedAt(e: Event): Boolean = e.isInstanceOf[KeyPressed] || e.isInstanceOf[KeyReleased]
  }

  /**
   * Randomly spawns rocks depending on how many rocks there currently are.
   */
  private def spawnRocks() = {
    if (Random.nextGaussian() + rocks.size <= 0) {
      val rot = Random.nextDouble() * 2 * math.Pi
      rocks += (((SizeArena * 0.5, SizeArena * 0.5), (VelRock * math.cos(rot), -VelRock * math.sin(rot))))
    }
  }

  /**
   * Shoots bullets and updates shooting cooldown.
   */
  private def doShooting() = {
    def createBullet(playerPos: (Double, Double), playerVel: (Double, Double), rot: Double): ((Double, Double), (Double, Double)) = {
      val cosRot = math.cos(rot)
      val sinRot = math.sin(rot)
      ((playerPos._1 + RadiusPlayer * cosRot, playerPos._2 - RadiusPlayer * sinRot),
        (playerVel._1 + VelBullet * cosRot, playerVel._2 - VelBullet * sinRot))
    }

    if (player1Shoot) {
      bullet1s += createBullet(player1Pos, player1Vel, player1Rot)
      player1Cd = TimerMax
      player1Shoot = false
    }
    if (player2Shoot) {
      bullet2s += createBullet(player2Pos, player2Vel, player2Rot)
      player2Cd = TimerMax
      player2Shoot = false
    }

    player1Cd -= 1
    player2Cd -= 1
  }

  /**
   * Updates the positions and velocities of everything.
   */
  private def updatePos() = {
    def sumPairs(a: (Double, Double), b: (Double, Double)): (Double, Double) = {
      (a._1 + b._1, a._2 + b._2)
    }
    def applyVel(x: ((Double, Double), (Double, Double))): ((Double, Double), (Double, Double)) = {
      (sumPairs(x._1, x._2), x._2)
    }
    def isOnscreen(r: Int)(x: ((Double, Double), (Double, Double))): Boolean = {
      (0 - r < x._1._1 && x._1._1 < SizeArena + r) && (0 - r < x._1._2 && x._1._2 < SizeArena + r)
    }

    // Players
    // Dampen Velocities
    player1Vel = (player1Vel._1 * VelDampPlayer, player1Vel._2 * VelDampPlayer)
    player2Vel = (player2Vel._1 * VelDampPlayer, player2Vel._2 * VelDampPlayer)
    // Accelerate
    player1Vel = sumPairs(player1Vel, player1Acel)
    player2Vel = sumPairs(player2Vel, player2Acel)
    player1Pos = sumPairs(player1Pos, player1Vel)
    player2Pos = sumPairs(player2Pos, player2Vel)
    player1Rot += player1RotVel
    player2Rot += player2RotVel
    // Keep Onscreen
    if (RadiusPlayer > player1Pos._1) { player1Pos = (RadiusPlayer, player1Pos._2); player1Vel = (0, player1Vel._2) }
    if (player1Pos._1 > SizeArena - RadiusPlayer) { player1Pos = (SizeArena - RadiusPlayer, player1Pos._2); player1Vel = (0, player1Vel._2) }
    if (RadiusPlayer > player1Pos._2) { player1Pos = (player1Pos._1, RadiusPlayer); player1Vel = (player1Vel._1, 0) }
    if (player1Pos._2 > SizeArena - RadiusPlayer) { player1Pos = (player1Pos._1, SizeArena - RadiusPlayer); player1Vel = (player1Vel._1, 0) }

    if (RadiusPlayer > player2Pos._1) { player2Pos = (RadiusPlayer, player2Pos._2); player2Vel = (0, player2Vel._2) }
    if (player2Pos._1 > SizeArena - RadiusPlayer) { player2Pos = (SizeArena - RadiusPlayer, player2Pos._2); player2Vel = (0, player2Vel._2) }
    if (RadiusPlayer > player2Pos._2) { player2Pos = (player2Pos._1, RadiusPlayer); player2Vel = (player2Vel._1, 0) }
    if (player2Pos._2 > SizeArena - RadiusPlayer) { player2Pos = (player2Pos._1, SizeArena - RadiusPlayer); player2Vel = (player2Vel._1, 0) }

    // Bullets
    bullet1s = bullet1s map applyVel filter isOnscreen(RadiusBullet)
    bullet2s = bullet2s map applyVel filter isOnscreen(RadiusBullet)

    // Rocks
    rocks = rocks map applyVel filter isOnscreen(RadiusRock)
  }

  /**
   * Detects collisions between things and performs the appropreate actions.
   */
  private def doCollisions() = {
    // ----- Bullet1 Collisions -----
    for (b1 <- bullet1s) {
      // Player2
      if (colliding(player2Pos, RadiusPlayer, b1._1, RadiusBullet)) {
        player2Health -= DamageBullet
        bullet1s -= b1
      }

      // Rocks
      for (r <- rocks) {
        if (colliding(r._1, RadiusRock, b1._1, RadiusBullet)) {
          player1Health += HealRock
          bullet1s -= b1
          rocks -= r
        }
      }
    }

    // ----- Bullet2 Collisions -----
    for (b2 <- bullet2s) {
      // Player1
      if (colliding(player1Pos, RadiusPlayer, b2._1, RadiusBullet)) {
        player1Health -= DamageBullet
        bullet2s -= b2
      }

      // Rocks
      for (r <- rocks) {
        if (colliding(r._1, RadiusRock, b2._1, RadiusBullet)) {
          player2Health += HealRock
          bullet2s -= b2
          rocks -= r
        }
      }
    }

    // ----- Rock-Player Collision -----
    for (r <- rocks) {
      if (colliding(player1Pos, RadiusPlayer, r._1, RadiusRock)) {
        player1Health -= DamageRock
        rocks -= r
      }
      if (colliding(player2Pos, RadiusPlayer, r._1, RadiusRock)) {
        player2Health -= DamageRock
        rocks -= r
      }
    }

    // ----- Player1-Player2 Collision -----
    if (colliding(player1Pos, RadiusPlayer, player2Pos, RadiusPlayer)) {
      player1Health -= DamagePlayer
      player2Health -= DamagePlayer
    }
  }

  /**
   * Stops this {@code Ticker} if either of the players are dead.
   */
  private def checkDeath() = {
    if (player1Health <= 0 || player2Health <= 0) stop()
  }

  /**
   * The function called to stop this Ticker. It is initialized by starting this Ticker.
   */
  private var _stop: (() => Unit) = () => throw new IllegalStateException("Game has not been started")

  /**
   * Stops the game. Throws an exception if the game has not been started.
   */
  def stop() = {
    _stop()
    running = false;
  }

  /**
   * The function called to start this Ticker. It self-destructs after its first call.
   */
  private var _start: (() => Unit) = () => {
    _start = () => throw new IllegalStateException("Game has already been started")
    Ticker.start(this)
  }
  
  /**
   * Starts the game. Throws an exception if the game has already been started.
   */
  def start() = {
    _start()
    running = true;
  }

  /**
   * The result of this game.
   */
  def result: GameResult.GameResult = {
    if (player1Health <= 0 && player2Health <= 0) return GameResult.Tie
    if (player2Health <= 0) return GameResult.Won1
    if (player1Health <= 0) return GameResult.Won2
    return GameResult.InProgress
  }
}

object Ticker {

  private val VelBullet = 10.0
  private val VelRock = 1.0

  private val AccelPlayer = 2.0
  private val RotVelPlayer = math.Pi / 8
  private val VelDampPlayer = 0.90

  private val DamagePlayer = 10
  private val DamageRock = 25
  private val DamageBullet = 15
  private val HealRock = 10

  private val timer = new ScheduledThreadPoolExecutor(0)

  def apply(stateCallback: GameState => Unit): Ticker = new Ticker(stateCallback)

  /**
   * Sets the given ticker to be executed every 50ms and sets the stopping function.
   */
  private def start(t: Ticker): Unit = {
    val future = timer.scheduleAtFixedRate(t, 0, 50, TimeUnit.MILLISECONDS)
    t._stop = () => future.cancel(false)
  }

  /**
   * Returns whether or not two circular objects are colliding with eachother.
   */
  private def colliding(a: (Double, Double), ar: Double, b: (Double, Double), br: Double): Boolean = {
    Math.hypot(a._1 - b._1, a._2 - b._2) < ar + br
  }
}
