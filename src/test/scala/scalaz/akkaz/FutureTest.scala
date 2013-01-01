package scalaz.akkaz

import scalaz.std.AllInstances._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalazArbitrary._

import org.scalacheck.Prop._
import org.scalacheck.{Gen, Arbitrary}

import scala.concurrent.{Future, Await, Promise, ExecutionContext}
import scala.concurrent.duration._

import java.util.concurrent.Executors

/**Blocking equal for law testing only*/
trait BlockingFutureEqual[T] extends scalaz.Equal[Future[T]] {
  protected implicit def T: scalaz.Equal[T]

  def equal(a1: Future[T], a2: Future[T]): Boolean = {
    // TODO could account for exceptions too
    val r1 = Await.result(a1, 5.seconds)
    val r2 = Await.result(a2, 5.seconds)
    T.equal(r1, r2)
  }

}

object BlockingFutureEqual {
  implicit def instance[T](implicit ET: scalaz.Equal[T]) = new BlockingFutureEqual[T] {
    override protected implicit val T = ET
  }
}


class FutureTest extends scalaz.Spec {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  implicit val atMost: Duration = 5.seconds

  implicit def futureArbitrary[T](implicit T: Arbitrary[T]): Arbitrary[Future[T]] =
    Arbitrary(Gen.frequency(
      8 -> Gen.resultOf((t: T) => busyFuture(t)),
      2 -> Gen.resultOf((t: T) => Promise.successful(t).future) ))

  {
    implicit val F = scalaz.akkaz.future.futureInstancesAll(ec, atMost)
    import BlockingFutureEqual.instance
    checkAll("Future", functor.laws[Future])
    checkAll("Future", monad.laws[Future])
    checkAll("Future", comonad.laws[Future])
    checkAll("Future", applicative.laws[Future])
    checkAll("Future", traverse.laws[Future])

    import scalaz.akkaz.future.futureMonoid
    checkAll("Future", monoid.laws[Future[Int]])
  }

  private def busyFuture[T](t: => T): Future[T] =
    Future(scala.concurrent.blocking { // signal to reschedule queued stuff to other threads
      Thread.sleep(scala.util.Random.nextInt(30))
      t
    })

}
