package io.github.mkotsur.quicksend

import Quicksend.Deps
import Template.Sealed
import conf.QuicksendConf

import cats.Parallel
import cats.data.Kleisli
import cats.effect.{Blocker, Concurrent, ContextShift, Resource, Sync}
import cats.implicits._
import com.minosiants.pencil.Client
import com.minosiants.pencil.protocol.Replies
import fs2.io.tcp.SocketGroup
import fs2.io.tls.TLSContext
import org.typelevel.log4cats.Logger

object Quicksend {
  case class Deps[F[_]](conf: QuicksendConf, clientR: Resource[F, Client[F]])

  object Deps {
    def clientR[F[_]: Sync: ContextShift: Logger: Concurrent](
        conf: QuicksendConf
    ): Resource[F, Client[F]] =
      for {
        blocker <- Blocker[F]
        sGroup <- SocketGroup[F](blocker)
        system <- Resource.eval(TLSContext.system[F](blocker))
      } yield Client[F](
        conf.host,
        conf.port,
        conf.credentials
      )(
        blocker,
        sGroup,
        system,
        Logger[F]
      )
  }

  def apply[F[_]: ContextShift: Logger: Sync: Concurrent: Parallel]()
      : Kleisli[F, QuicksendConf, Quicksend[F]] =
    Kleisli { conf => new Quicksend[F](Deps(conf, Deps.clientR[F](conf))).pure[F] }

  def apply[F[_]: ContextShift: Logger: Sync: Concurrent: Parallel](
      conf: QuicksendConf
  ): Quicksend[F] =
    new Quicksend[F](Deps(conf, Deps.clientR[F](conf))).pure[F]
}

class Quicksend[F[_]: ContextShift: Logger: Sync: Concurrent: Parallel](deps: Deps[F]) {

  def sendMany(templates: List[Sealed[F]]): F[List[Replies]] =
    templates.map(send).parSequence

  def send(template: Sealed[F]): F[Replies] =
    deps.clientR.use { client =>
      for {
        email <- template.run(deps.conf)
        res <- client.send(email)
      } yield res
    }
}
