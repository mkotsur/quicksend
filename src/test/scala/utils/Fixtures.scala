package io.github.mkotsur.quicksend
package utils

import cats.FlatMap
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._

object Fixtures {

  def apply[F[_]: FlatMap, L, X](fxtFor: L => F[X]) = new Fixtures[F, L, X](fxtFor)
  def apply[F[_]: FlatMap: Sync, L, X](fxtFor: L => X) = new Fixtures[F, L, X](fxtFor(_).pure[F])

}

class Fixtures[F[_]: FlatMap, L, X] private (val fxtFor: L => F[X]) {

  def withFxt[B](flow: L)(thunk: X => F[B]): F[B] = {
    FlatMap[F].flatMap(fxtFor(flow))(thunk)
  }
  def runWithFxt[B](flow: L)(thunk: Kleisli[F, X, B]): F[B] = {
    FlatMap[F].flatMap(fxtFor(flow))(thunk.run)
  }

}
