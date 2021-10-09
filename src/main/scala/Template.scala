package io.github.mkotsur.quicksend

import conf.QuicksendConf

import cats.data.Kleisli
import com.minosiants.pencil.data._

import java.time.format.DateTimeFormatter

object Template {

  type Template[F[_], -V[*[_]]] = Kleisli[F, (QuicksendConf, V[F]), RdxEmail]
  type Sealed[F[_]] = Kleisli[F, QuicksendConf, RdxEmail]

  type NoVars[`?`[_]] = Unit

  def apply[F[_], V[*[_]]](tpl: ((QuicksendConf, V[F])) => F[RdxEmail]): Template[F, V] =
    Kleisli[F, (QuicksendConf, V[F]), RdxEmail](tpl)

  object syntax {
    implicit class TemplateOps[F[_], V[*[_]]](
        val template: Kleisli[F, (QuicksendConf, V[F]), RdxEmail]
    ) extends AnyVal {
      def seal(vars: V[F]): Sealed[F] =
        template.local[QuicksendConf]((_, vars))
    }
    implicit class NoVarsTemplateOps[F[_]](
        val template: Kleisli[F, (QuicksendConf, Unit), RdxEmail]
    ) extends AnyVal {
      def seal: Kleisli[F, QuicksendConf, RdxEmail] =
        template.local[QuicksendConf]((_, ()))
    }
  }

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm Z")

  /**
    * This class has mostly the same attributes as [[Email]],
    * besides [[com.minosiants.pencil.data.From]] as it's
    * filled in via DI mechanism from the email configuration.
    */
  case class RdxEmail(to: To, subject: Subject, body: Body, attachments: List[Attachment] = Nil)

}
