/**
 *
 */
package hmperson1.apps.shootinggamething

import java.util.concurrent.{ ScheduledThreadPoolExecutor, TimeUnit }
import scala.collection.mutable
import scala.swing.Reactions.Reaction
import scala.swing.event.{ Event, KeyPressed, KeyReleased, Key }
import scala.util.Random
import ShootingGameThing.{ BulletRadius, PlayerRadius, RockRadius }

/**
 * Does physics updates.
 * This could probably be done with Box2D, but what would be the fun in that?
 *
 * @author HMPerson1
 */
class Ticker(pushState: GameState => Unit) extends Runnable {
  import Ticker._

  private val PlayerVelDamp = 0.90

  private var player1Pos = (150.0, 250.0)
  private var player2Pos = (350.0, 250.0)
  private var player1Vel = (0.0, 0.0)
  private var player2Vel = (0.0, 0.0)
  private var player1Acel = (0.0, 0.0)
  private var player2Acel = (0.0, 0.0)
  private var player1Rot = 0.0
  private var player2Rot = 3.1415926535897932
  private var player1RotVel = 0.0
  private var player2RotVel = 0.0
  private var player1Cd = 0
  private var player2Cd = 0
  private var player1Shoot = false
  private var player2Shoot = false
  private var player1Health = 100
  private var player2Health = 100

  private var bullet1s = mutable.Set[((Double, Double), (Double, Double))]()
  private var bullet2s = mutable.Set[((Double, Double), (Double, Double))]()

  private var rocks = mutable.Set[((Double, Double), (Double, Double))]()

  override def run(): Unit = {
    spawnRocks()
    doShooting()
    updatePos()
    doCollisions()

    def roundPair(x: (Double, Double)): (Int, Int) = {
      (x._1.round.toInt, x._2.round.toInt)
    }

    pushState(GameState(
      (roundPair(player1Pos), player1Rot, player1Health, player1Cd),
      (roundPair(player2Pos), player2Rot, player2Health, player2Cd),
      (bullet1s ++ bullet2s) map { x => roundPair(x._1) } toSet,
      rocks map { x => roundPair(x._1) } toSet
    ))
  }

  val keyReaction: Reaction = new Reaction {
    def apply(e: Event): Unit = {
      e match {
        case e: KeyPressed =>
          e.key match {
            // Player 1
            case Key.A => player1Acel = (-2.0, player1Acel._2)
            case Key.D => player1Acel = (2.0, player1Acel._2)
            case Key.W => player1Acel = (player1Acel._1, -2.0)
            case Key.S => player1Acel = (player1Acel._1, 2.0)
            case Key.Q => player1RotVel = math.Pi / 8
            case Key.E => player1RotVel = -math.Pi / 8
            // Player 2
            case Key.J => player2Acel = (-2.0, player2Acel._2)
            case Key.L => player2Acel = (2.0, player2Acel._2)
            case Key.I => player2Acel = (player2Acel._1, -2.0)
            case Key.K => player2Acel = (player2Acel._1, 2.0)
            case Key.U => player2RotVel = math.Pi / 8
            case Key.O => player2RotVel = -math.Pi / 8
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

    def isDefinedAt(e: Event): Boolean = {
      e.isInstanceOf[KeyPressed] || e.isInstanceOf[KeyReleased]
    }
  }

  private def spawnRocks() = {
    if (Random.nextGaussian() + rocks.size <= 0) {
      val rot = Random.nextDouble() * 2 * math.Pi
      rocks += (((250.0, 250.0), (RockVel * math.cos(rot), -RockVel * math.sin(rot))))
    }
  }

  private def doShooting() = {
    def createBullet(playerPos: (Double, Double), playerVel: (Double, Double), rot: Double): ((Double, Double), (Double, Double)) = {
      val cosRot = math.cos(rot)
      val sinRot = math.sin(rot)
      ((playerPos._1 + PlayerRadius * cosRot, playerPos._2 - PlayerRadius * sinRot), (playerVel._1 + BulletVel * cosRot, playerVel._2 - BulletVel * sinRot))
    }

    if (player1Shoot) {
      bullet1s += createBullet(player1Pos, player1Vel, player1Rot)
      player1Cd = 50
      player1Shoot = false
    }
    if (player2Shoot) {
      bullet2s += createBullet(player2Pos, player2Vel, player2Rot)
      player2Cd = 50
      player2Shoot = false
    }

    player1Cd -= 1
    player2Cd -= 1
  }

  private def updatePos() = {
    def sumPairs(a: (Double, Double), b: (Double, Double)): (Double, Double) = {
      (a._1 + b._1, a._2 + b._2)
    }
    def applyVel(x: ((Double, Double), (Double, Double))): ((Double, Double), (Double, Double)) = {
      (sumPairs(x._1, x._2), x._2)
    }
    def isOnscreen(x: (Double, Double), r: Int): Boolean = {
      (0 - r < x._1 && x._1 < 500 + r) && (0 - r < x._2 && x._2 < 500 + r)
    }

    // Players
    // Dampen Velocities
    player1Vel = (player1Vel._1 * PlayerVelDamp, player1Vel._2 * PlayerVelDamp)
    player2Vel = (player2Vel._1 * PlayerVelDamp, player2Vel._2 * PlayerVelDamp)
    // Accelerate
    player1Vel = sumPairs(player1Vel, player1Acel)
    player2Vel = sumPairs(player2Vel, player2Acel)
    player1Pos = sumPairs(player1Pos, player1Vel)
    player2Pos = sumPairs(player2Pos, player2Vel)
    player1Rot += player1RotVel
    player2Rot += player2RotVel
    // Keep Onscreen
    if (PlayerRadius > player1Pos._1) { player1Pos = (PlayerRadius, player1Pos._2); player1Vel = (0, player1Vel._2) }
    if (player1Pos._1 > 500 - PlayerRadius) { player1Pos = (500 - PlayerRadius, player1Pos._2); player1Vel = (0, player1Vel._2) }
    if (PlayerRadius > player1Pos._2) { player1Pos = (player1Pos._1, PlayerRadius); player1Vel = (player1Vel._1, 0) }
    if (player1Pos._2 > 500 - PlayerRadius) { player1Pos = (player1Pos._1, 500 - PlayerRadius); player1Vel = (player1Vel._1, 0) }

    if (PlayerRadius > player2Pos._1) { player2Pos = (PlayerRadius, player2Pos._2); player2Vel = (0, player2Vel._2) }
    if (player2Pos._1 > 500 - PlayerRadius) { player2Pos = (500 - PlayerRadius, player2Pos._2); player2Vel = (0, player2Vel._2) }
    if (PlayerRadius > player2Pos._2) { player2Pos = (player2Pos._1, PlayerRadius); player2Vel = (player2Vel._1, 0) }
    if (player2Pos._2 > 500 - PlayerRadius) { player2Pos = (player2Pos._1, 500 - PlayerRadius); player2Vel = (player2Vel._1, 0) }

    // Bullets
    bullet1s = bullet1s map applyVel filter { x => isOnscreen(x._1, BulletRadius) }
    bullet2s = bullet2s map applyVel filter { x => isOnscreen(x._1, BulletRadius) }

    // Rocks
    rocks = rocks map applyVel filter { x => isOnscreen(x._1, RockRadius) }
  }

  private def doCollisions() = {
    // ----- Bullet1 Collisions -----
    for (b1 <- bullet1s) {
      // Player2
      if (colliding(player2Pos, PlayerRadius, b1._1, BulletRadius)) {
        player2Health -= 15
        bullet1s -= b1
      }

      // Rocks
      for (r <- rocks) {
        if (colliding(r._1, RockRadius, b1._1, BulletRadius)) {
          player1Health += 10
          bullet1s -= b1
          rocks -= r
        }
      }
    }

    // ----- Bullet2 Collisions -----
    for (b2 <- bullet2s) {
      // Player1
      if (colliding(player1Pos, PlayerRadius, b2._1, BulletRadius)) {
        player1Health -= 15
        bullet2s -= b2
      }

      // Rocks
      for (r <- rocks) {
        if (colliding(r._1, RockRadius, b2._1, BulletRadius)) {
          player2Health += 10
          bullet2s -= b2
          rocks -= r
        }
      }
    }

    // ----- Rock-Player Collision -----
    for (r <- rocks) {
      if (colliding(player1Pos, PlayerRadius, r._1, RockRadius)) {
        player1Health -= 25
        rocks -= r
      }
      if (colliding(player2Pos, PlayerRadius, r._1, RockRadius)) {
        player2Health -= 25
        rocks -= r
      }
    }

    // ----- Player1-Player2 Collision -----
    if (colliding(player1Pos, PlayerRadius, player2Pos, PlayerRadius)) {
      player1Health -= 10
      player2Health -= 10
    }
  }

  var stop: () => _ = _

  def start() = Ticker.start(this)
}

object Ticker {

  private val BulletVel = 10.0
  private val RockVel = 1.0

  private val timer = new ScheduledThreadPoolExecutor(0)

  def apply(stateCallback: GameState => Unit): Ticker = new Ticker(stateCallback)

  def start(t: Ticker): Unit = {
    val future = timer.scheduleAtFixedRate(t, 0, 50, TimeUnit.MILLISECONDS)
    t.stop = () => future.cancel(false)
  }

  private def colliding(a: (Double, Double), ar: Double, b: (Double, Double), br: Double): Boolean = {
    Math.hypot(a._1 - b._1, a._2 - b._2) < ar + br
  }
}
