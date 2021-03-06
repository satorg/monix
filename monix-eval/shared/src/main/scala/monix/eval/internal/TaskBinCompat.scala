/*
 * Copyright (c) 2014-2018 by The Monix Project Developers.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.eval
package internal

import monix.eval.Task.Options
import monix.execution.annotations.UnsafeBecauseImpure
import monix.execution.{Cancelable, CancelableFuture, Scheduler}
import scala.annotation.unchecked.uncheckedVariance
import scala.util.{Failure, Success, Try}

private[eval] abstract class TaskBinCompat[+A] { self: Task[A] =>
  /**
    * DEPRECATED — switch to [[runSyncStep]] or to
    * [[runAsync(implicit* runAsync]].
    *
    * The [[runAsync(implicit* runAsync]] variant that returns
    * [[monix.execution.CancelableFuture CancelableFuture]] will
    * return already completed future values, useful for low level
    * optimizations. All this `runSyncMaybe` did was to piggyback
    * on it.
    *
    * The reason for the deprecation is to reduce the unneeded
    * "run" overloads.
    */
  @UnsafeBecauseImpure
  @deprecated("Please use `Task.runSyncStep`", since = "3.0.0")
  final def runSyncMaybe(implicit s: Scheduler): Either[CancelableFuture[A], A] = {
    // $COVERAGE-OFF$
    runSyncMaybeOptPrv(s, Task.defaultOptions)
    // $COVERAGE-ON$
  }

  /**
    * DEPRECATED — switch to [[runSyncStepOpt]] or to
    * [[runAsyncOpt(implicit* runAsync]].
    *
    * The [[runAsyncOpt(implicit* runAsyncOpt]] variant that returns
    * [[monix.execution.CancelableFuture CancelableFuture]] will
    * return already completed future values, useful for low level
    * optimizations. All this `runSyncMaybeOpt` did was to piggyback
    * on it.
    *
    * The reason for the deprecation is to reduce the unneeded
    * "run" overloads.
    */
  @UnsafeBecauseImpure
  @deprecated("Please use `Task.runAsyncOpt`", since = "3.0.0")
  final def runSyncMaybeOpt(implicit s: Scheduler, opts: Options): Either[CancelableFuture[A], A] = {
    // $COVERAGE-OFF$
    runSyncMaybeOptPrv(s, opts)
    // $COVERAGE-ON$
  }

  private[this] final def runSyncMaybeOptPrv(implicit s: Scheduler, opts: Options): Either[CancelableFuture[A], A] = {
    // $COVERAGE-OFF$
    val future = runAsyncOpt(s, opts)
    future.value match {
      case Some(value) =>
        value match {
          case Success(a) => Right(a)
          case Failure(e) => throw e
        }
      case None =>
        Left(future)
    }
    // $COVERAGE-ON$
  }

  /**
    * DEPRECATED — switch to [[runAsync(cb* runAsync]] in combination
    * with [[Callback.fromTry]] instead.
    *
    * If for example you have a `Try[A] => Unit` function, you can
    * replace usage of `runOnComplete` with:
    *
    * `task.runAsync(Callback.fromTry(f))`
    *
    * A more common usage is via Scala's `Promise`, but with
    * a `Promise` reference this construct would be even more
    * efficient:
    *
    * `task.runAsync(Callback.fromPromise(p))`
    */
  @UnsafeBecauseImpure
  @deprecated("Please use `Task.runAsync`", since = "3.0.0")
  final def runOnComplete(f: Try[A] => Unit)(implicit s: Scheduler): Cancelable = {
    // $COVERAGE-OFF$
    runAsync(Callback.fromTry(f))(s)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — use [[redeem]] instead.
    *
    * [[Task.redeem]] is the same operation, but with a different name and the
    * function parameters in an inverted order, to make it consistent with `fold`
    * on `Either` and others (i.e. the function for error recovery is at the left).
    */
  @deprecated("Please use `Task.redeem`", since = "3.0.0-RC2")
  final def transform[R](fa: A => R, fe: Throwable => R): Task[R] = {
    // $COVERAGE-OFF$
    redeem(fe, fa)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — use [[redeemWith]] instead.
    *
    * [[Task.redeemWith]] is the same operation, but with a different name and the
    * function parameters in an inverted order, to make it consistent with `fold`
    * on `Either` and others (i.e. the function for error recovery is at the left).
    */
  @deprecated("Please use `Task.redeemWith`", since = "3.0.0-RC2")
  final def transformWith[R](fa: A => Task[R], fe: Throwable => Task[R]): Task[R] = {
    // $COVERAGE-OFF$
    redeemWith(fe, fa)
    // $COVERAGE-ON$
  }

  /**
    * DEPRECATED — switch to [[Task.parZip2]], which has the same behavior.
    */
  @deprecated("Switch to Task.parZip2", since="3.0.0-RC2")
  final def zip[B](that: Task[B]): Task[(A, B)] = {
    // $COVERAGE-OFF$
    Task.mapBoth(this, that)((a,b) => (a,b))
    // $COVERAGE-ON$
  }

  /**
    * DEPRECATED — switch to [[Task.parMap2]], which has the same behavior.
    */
  @deprecated("Use Task.parMap2", since="3.0.0-RC2")
  final def zipMap[B,C](that: Task[B])(f: (A,B) => C): Task[C] =
    Task.mapBoth(this, that)(f)

  /** DEPRECATED — renamed to [[Task.executeAsync executeAsync]].
    *
    * The reason for the deprecation is the repurposing of the word "fork".
    */
  @deprecated("Renamed to Task!.executeAsync", "3.0.0")
  def executeWithFork: Task[A] = {
    // $COVERAGE-OFF$
    self.executeAsync
    // $COVERAGE-ON$
  }

  /** DEPRECATED — please use [[Task.flatMap flatMap]].
    *
    * The reason for the deprecation is that this operation is
    * redundant, as it can be expressed with `flatMap`, with the
    * same effect:
    * {{{
    *   import monix.eval.Task
    *
    *   val trigger = Task(println("do it"))
    *   val task = Task(println("must be done now"))
    *   trigger.flatMap(_ => task)
    * }}}
    *
    * The syntax provided by Cats can also help:
    * {{{
    *   import cats.syntax.all._
    *
    *   trigger *> task
    * }}}
    */
  @deprecated("Please use flatMap", "3.0.0")
  def delayExecutionWith(trigger: Task[Any]): Task[A] = {
    // $COVERAGE-OFF$
    trigger.flatMap(_ => self)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — please use [[Task.flatMap flatMap]].
    *
    * The reason for the deprecation is that this operation is
    * redundant, as it can be expressed with `flatMap` and `map`,
    * with the same effect:
    *
    * {{{
    *   import monix.eval.Task
    *
    *   val task = Task(5)
    *   val selector = (n: Int) => Task(n.toString)
    *   task.flatMap(a => selector(a).map(_ => a))
    * }}}
    */
  @deprecated("Please rewrite in terms of flatMap", "3.0.0")
  def delayResultBySelector[B](selector: A => Task[B]): Task[A] = {
    // $COVERAGE-OFF$
    self.flatMap(a => selector(a).map(_ => a))
    // $COVERAGE-OFF$
  }

  /**
    * DEPRECATED — since Monix 3.0 the `Task` implementation has switched
    * to auto-cancelable run-loops by default (which can still be turned off
    * in its configuration).
    *
    * For ensuring the old behavior, you can use [[executeWithOptions]].
    */
  @deprecated("Switch to executeWithOptions(_.enableAutoCancelableRunLoops)", "3.0.0")
  def cancelable: Task[A] = {
    // $COVERAGE-OFF$
    executeWithOptions(_.enableAutoCancelableRunLoops)
    // $COVERAGE-ON$
  }

  /**
    * DEPRECATED — subsumed by [[start]].
    *
    * To be consistent with cats-effect 1.0.0, `start` now
    * enforces an asynchronous boundary, being exactly the same
    * as `fork` from 3.0.0-RC1
    */
  @deprecated("Replaced with start", since="3.0.0-RC2")
  final def fork: Task[Fiber[A @uncheckedVariance]] = {
    // $COVERAGE-OFF$
    this.start
    // $COVERAGE-ON$
  }

  /** DEPRECATED — replace with usage of [[Task.runSyncStep]]:
    *
    * `task.coeval <-> Coeval(task.runSyncMaybe)`
    */
  @deprecated("Replaced with start", since="3.0.0-RC2")
  final def coeval(implicit s: Scheduler): Coeval[Either[CancelableFuture[A], A]] = {
    // $COVERAGE-OFF$
    Coeval.eval(runSyncMaybeOptPrv(s, Task.defaultOptions))
    // $COVERAGE-ON$
  }
}

private[eval] abstract class TaskBinCompatCompanion {

  /** DEPRECATED — renamed to [[Task.parZip2]]. */
  @deprecated("Renamed to Task.parZip2", since = "3.0.0-RC2")
  def zip2[A1,A2,R](fa1: Task[A1], fa2: Task[A2]): Task[(A1,A2)] = {
    // $COVERAGE-OFF$
    Task.parZip2(fa1, fa2)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — renamed to [[Task.parZip3]]. */
  @deprecated("Renamed to Task.parZip3", since = "3.0.0-RC2")
  def zip3[A1,A2,A3](fa1: Task[A1], fa2: Task[A2], fa3: Task[A3]): Task[(A1,A2,A3)] = {
    // $COVERAGE-OFF$
    Task.parZip3(fa1, fa2, fa3)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — renamed to [[Task.parZip4]]. */
  @deprecated("Renamed to Task.parZip4", since = "3.0.0-RC2")
  def zip4[A1,A2,A3,A4](fa1: Task[A1], fa2: Task[A2], fa3: Task[A3], fa4: Task[A4]): Task[(A1,A2,A3,A4)] = {
    // $COVERAGE-OFF$
    Task.parZip4(fa1, fa2, fa3, fa4)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — renamed to [[Task.parZip5]]. */
  @deprecated("Renamed to Task.parZip5", since = "3.0.0-RC2")
  def zip5[A1,A2,A3,A4,A5](fa1: Task[A1], fa2: Task[A2], fa3: Task[A3], fa4: Task[A4], fa5: Task[A5]): Task[(A1,A2,A3,A4,A5)] = {
    // $COVERAGE-OFF$
    Task.parZip5(fa1, fa2, fa3, fa4, fa5)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — renamed to [[Task.parZip6]]. */
  @deprecated("Renamed to Task.parZip6", since = "3.0.0-RC2")
  def zip6[A1,A2,A3,A4,A5,A6](fa1: Task[A1], fa2: Task[A2], fa3: Task[A3], fa4: Task[A4], fa5: Task[A5], fa6: Task[A6]): Task[(A1,A2,A3,A4,A5,A6)] = {
    // $COVERAGE-OFF$
    Task.parZip6(fa1, fa2, fa3, fa4, fa5, fa6)
    // $COVERAGE-ON$
  }

  /** DEPRECATED — please use [[Task!.executeAsync .executeAsync]].
    *
    * The reason for the deprecation is the repurposing of the word "fork".
    */
  @deprecated("Please use Task!.executeAsync", "3.0.0")
  def fork[A](fa: Task[A]): Task[A] = {
    // $COVERAGE-OFF$
    fa.executeAsync
    // $COVERAGE-ON$
  }

  /** DEPRECATED — please use [[Task.executeOn .executeOn]].
    *
    * The reason for the deprecation is the repurposing of the word "fork".
    */
  @deprecated("Please use Task!.executeOn", "3.0.0")
  def fork[A](fa: Task[A], s: Scheduler): Task[A] = {
    // $COVERAGE-OFF$
    fa.executeOn(s)
    // $COVERAGE-ON$
  }
}
