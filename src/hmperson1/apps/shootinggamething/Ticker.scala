/**
 *
 */
package hmperson1.apps.shootinggamething

import java.util.concurrent.{ ScheduledThreadPoolExecutor, TimeUnit }
import scala.collection.mutable
import scala.swing.Reactions.Reaction
import scala.swing.event.{ Event, Key, KeyPressed, KeyReleased }
import scala.util.Random
import ShootingGameThing.{ ArenaSize, BulletRadius, MaxHealth, MaxTimer, PlayerRadius, RockRadius }

/**
 * Does physics updates.
 * This could probably be done with Box2D, but what would be the fun in that?
 *
 * @author HMPerson1
 */
class Ticker(pushState: GameState => Unit) extends Runnable {
  import Ticker._

  private val PlayerVelDamp = 0.90

  private var player1Pos = (ArenaSize * 0.3, ArenaSize * 0.5)
  private var player2Pos = (ArenaSize * 0.7, ArenaSize * 0.5)
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
  private var player1Health = MaxHealth
  private var player2Health = MaxHealth

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
            case Key.A => player1Acel = (-PlayerAccel, player1Acel._2)
            case Key.D => player1Acel = (PlayerAccel, player1Acel._2)
            case Key.W => player1Acel = (player1Acel._1, -PlayerAccel)
            case Key.S => player1Acel = (player1Acel._1, PlayerAccel)
            case Key.Q => player1RotVel = PlayerRotVel
            case Key.E => player1RotVel = -PlayerRotVel
            // Player 2
            case Key.J => player2Acel = (-PlayerAccel, player2Acel._2)
            case Key.L => player2Acel = (PlayerAccel, player2Acel._2)
            case Key.I => player2Acel = (player2Acel._1, -PlayerAccel)
            case Key.K => player2Acel = (player2Acel._1, PlayerAccel)
            case Key.U => player2RotVel = PlayerRotVel
            case Key.O => player2RotVel = -PlayerRotVel
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
      rocks += (((ArenaSize * 0.5, ArenaSize * 0.5), (RockVel * math.cos(rot), -RockVel * math.sin(rot))))
    }
  }

  private def doShooting() = {
    def createBullet(playerPos: (Double, Double), playerVel: (Double, Double), rot: Double): ((Double, Double), (Double, Double)) = {
      val cosRot = math.cos(rot)
      val sinRot = math.sin(rot)
      ((playerPos._1 + PlayerRadius * cosRot, playerPos._2 - PlayerRadius * sinRot),
        (playerVel._1 + BulletVel * cosRot, playerVel._2 - BulletVel * sinRot))
    }

    if (player1Shoot) {
      bullet1s += createBullet(player1Pos, player1Vel, player1Rot)
      player1Cd = MaxTimer
      player1Shoot = false
    }
    if (player2Shoot) {
      bullet2s += createBullet(player2Pos, player2Vel, player2Rot)
      player2Cd = MaxTimer
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
      (0 - r < x._1 && x._1 < ArenaSize + r) && (0 - r < x._2 && x._2 < ArenaSize + r)
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
    if (PlayerRadius > player1Pos._1)             { player1Pos = (PlayerRadius, player1Pos._2);             player1Vel = (0, player1Vel._2) }
    if (player1Pos._1 > ArenaSize - PlayerRadius) { player1Pos = (ArenaSize - PlayerRadius, player1Pos._2); player1Vel = (0, player1Vel._2) }
    if (PlayerRadius > player1Pos._2)             { player1Pos = (player1Pos._1, PlayerRadius);             player1Vel = (player1Vel._1, 0) }
    if (player1Pos._2 > ArenaSize - PlayerRadius) { player1Pos = (player1Pos._1, ArenaSize - PlayerRadius); player1Vel = (player1Vel._1, 0) }

    if (PlayerRadius > player2Pos._1)             { player2Pos = (PlayerRadius, player2Pos._2);             player2Vel = (0, player2Vel._2) }
    if (player2Pos._1 > ArenaSize - PlayerRadius) { player2Pos = (ArenaSize - PlayerRadius, player2Pos._2); player2Vel = (0, player2Vel._2) }
    if (PlayerRadius > player2Pos._2)             { player2Pos = (player2Pos._1, PlayerRadius);             player2Vel = (player2Vel._1, 0) }
    if (player2Pos._2 > ArenaSize - PlayerRadius) { player2Pos = (player2Pos._1, ArenaSize - PlayerRadius); player2Vel = (player2Vel._1, 0) }

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
        player2Health -= DamageBullet
        bullet1s -= b1
      }

      // Rocks
      for (r <- rocks) {
        if (colliding(r._1, RockRadius, b1._1, BulletRadius)) {
          player1Health += HealRock
          bullet1s -= b1
          rocks -= r
        }
      }
    }

    // ----- Bullet2 Collisions -----
    for (b2 <- bullet2s) {
      // Player1
      if (colliding(player1Pos, PlayerRadius, b2._1, BulletRadius)) {
        player1Health -= DamageBullet
        bullet2s -= b2
      }

      // Rocks
      for (r <- rocks) {
        if (colliding(r._1, RockRadius, b2._1, BulletRadius)) {
          player2Health += HealRock
          bullet2s -= b2
          rocks -= r
        }
      }
    }

    // ----- Rock-Player Collision -----
    for (r <- rocks) {
      if (colliding(player1Pos, PlayerRadius, r._1, RockRadius)) {
        player1Health -= DamageRock
        rocks -= r
      }
      if (colliding(player2Pos, PlayerRadius, r._1, RockRadius)) {
        player2Health -= DamageRock
        rocks -= r
      }
    }

    // ----- Player1-Player2 Collision -----
    if (colliding(player1Pos, PlayerRadius, player2Pos, PlayerRadius)) {
      player1Health -= DamagePlayer
      player2Health -= DamagePlayer
    }
  }

  var stop: () => _ = _

  def start() = Ticker.start(this)
}

object Ticker {

  private val BulletVel = 10.0
  private val RockVel = 1.0

  private val PlayerAccel = 2.0
  private val PlayerRotVel = math.Pi / 8

  private val DamagePlayer = 10
  private val DamageRock = 25
  private val DamageBullet = 15
  private val HealRock = 10

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
