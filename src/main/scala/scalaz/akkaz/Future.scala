package scalaz.akkaz

import scalaz.{Monad, Comonad, Traverse, Applicative}

import akka.util.Duration
import akka.dispatch.{ExecutionContext, Future, KeptPromise, Await}

trait FutureInstances {

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

    override def cojoin[A](a: Future[A]): Future[Future[A]] = new KeptPromise(Right(a))

    override def cobind[A, B](fa: Future[A])(f: (Future[A]) => B): Future[B] = new KeptPromise(Right(f(fa)))

    override def copoint[A](p: Future[A]): A = Await.result(p, atMost)

    // Traverse

    override def traverseImpl[G[_] : Applicative, A, B](fa: Future[A])(f: (A) => G[B]): G[Future[B]] =
      Applicative[G].map(f(Await.result(fa, atMost)))(x => new KeptPromise(Right(x)))

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
 * Quickstart to import all instances (needs ExecutionContext and possibly timeout in implicit scope):
 * {{{
 * import scalaz.akkaz.future._
 * }}}
 *
 * To import typeclass instances which don't use blocking operations
 * (currently Monad and its dependencies like Functor or Applicative):
 * {{{
 * val ec: akka.dispatch.ExecutionContext = ...
 * implicit val F = scalaz.akkaz.future.futureInstancesNonblocking(ec)
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
