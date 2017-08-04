/*
 * Copyright (c) 2014-2017 by The Monix Project Developers.
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

package monix.tail

import cats.Eq
import cats.data.EitherT
import cats.laws.discipline.{CartesianTests, MonadErrorTests, MonoidKTests}
import monix.eval.Coeval

object TypeClassLawsForIterantCoevalSuite extends BaseLawsSuite {
  type F[α] = Iterant[Coeval, α]

  // Explicit instance due to weird implicit resolution problem
  implicit val iso: CartesianTests.Isomorphisms[F] =
    CartesianTests.Isomorphisms.invariant

  // Explicit instance, since Scala can't figure it out below :-(
  val eqEitherT: Eq[EitherT[F, Throwable, Int]] =
    implicitly[Eq[EitherT[F, Throwable, Int]]]

  checkAllAsync("MonadError[Iterant[Coeval]]") { implicit ec =>
    implicit val eqE = eqEitherT
    MonadErrorTests[F, Throwable].monadError[Int, Int, Int]
  }

  checkAllAsync("MonoidK[Iterant[Coeval]]") { implicit ec =>
    MonoidKTests[F].monoidK[Int]
  }
}
