package scalaz.akkaz

import scalaz.{Monoid, Monad, Comonad, Traverse, Applicative}

import akka.dispatch.{Await, ExecutionContext, Future, Promise}
import akka.util.Duration

/**
 * @see [[scalaz.akkaz.future]] for examples
 */
trait FutureInstances {

  implicit def futureMonoid[A](implicit A: Monoid[A], ec: ExecutionContext) = new Monoid[Future[A]] {

    override def zero: Future[A] = Promise.successful(A.zero)

    override def append(f1: Future[A], f2: => Future[A]): Future[A] = (f1 zip f2) map (x => A.append(x._1, x._2))
  }

  /**Instances without blocking operations*/
  trait FutureNonblockingInstances extends Monad[Future] {
    protected implicit def executionContext: ExecutionContext

    // Functor

    override def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa map f

    // Monad

    override def point[A](a: => A): Future[A] = Future(a)

    override def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = fa flatMap f
  }

  /**All instances including those that might use blocking operations*/
  trait FutureAllInstances extends Comonad[Future] with Traverse[Future] with FutureNonblockingInstances {
    protected def atMost: Duration

    // Comonad

    override def cojoin[A](a: Future[A]): Future[Future[A]] = Promise.successful(a)

    override def cobind[A, B](fa: Future[A])(f: (Future[A]) => B): Future[B] = Promise.successful(f(fa))

    /**Blocks the current Thread until the result is available.*/
    override def copoint[A](p: Future[A]): A = Await.result(p, atMost)

    // Traverse

    /**Blocks the current Thread until the result is available.*/
    override def traverseImpl[G[_] : Applicative, A, B](fa: Future[A])(f: (A) => G[B]): G[Future[B]] =
      Applicative[G].map(f(Await.result(fa, atMost)))(Promise.successful)

    /**Blocks the current Thread until the result is available.*/
    override def foldRight[A, B](fa: Future[A], z: => B)(f: (A, => B) => B): B =
      f(Await.result(fa, atMost), z)

  }

  /**All Future instances, needs implicit timeout duration in scope for blocking operations*/
  implicit def futureInstancesAll(implicit ec: ExecutionContext, d: Duration) = new FutureAllInstances {
    override protected implicit val executionContext = ec
    override protected val atMost = d
  }

  /**Future instances without blocking operations.*/
  implicit def futureInstancesNonblocking(implicit ec: ExecutionContext) = new FutureNonblockingInstances {
    override protected implicit val executionContext = ec
  }

}

/**
 * Quickstart to import all instances (needs ExecutionContext and optionally timeout in implicit scope):
 * {{{
 * import scalaz.akkaz.future._
 * }}}
 *
 * To import typeclass instances which don't use blocking operations
 * (currently Monad and its dependencies like Functor or Applicative):
 * {{{
 * val ec: akka.dispatch.ExecutionContext = ...
 * implicit val F = scalaz.akkaz.future.futureInstancesNonblocking(ec)
 * implicit val M = scalaz.akkaz.future.futureMonoid[A]
 * }}}
 *
 * To import all typeclasses (including Comonad and Traverse):
 * {{{
 * val ec: akka.dispatch.ExecutionContext = ...
 * val atMost: akka.util.Duration = ...
 * implicit val F = scalaz.akkaz.future.futureInstancesAll(ec, atMost)
 * }}}
 */
object future extends FutureInstances
