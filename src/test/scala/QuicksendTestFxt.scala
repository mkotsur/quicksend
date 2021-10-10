package io.github.mkotsur.quicksend

import Template.Template
import conf.QuicksendConf
import utils.Fixtures

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource, Timer}
import cats.implicits._
import com.minosiants.pencil.Client
import com.minosiants.pencil.data._
import com.minosiants.pencil.protocol.{Code, Replies, Reply}
import org.mockito.{MockitoSugar, ArgumentMatchers => mm}

import scala.concurrent.duration.FiniteDuration

trait QuicksendTestFxt {
  this: AsyncIOSpec with MockitoSugar =>

  sealed trait Flow
  object Flow {
    case object Happy extends Flow
    case class DelayedSend(delay: FiniteDuration) extends Flow
  }

  type Fxt = (Template[IO, TestTemplateVars], Client[IO], Quicksend.Deps[IO])

  case class TestTemplateVars[F[_]](to: String)
  private val emailTemplate =
    Template[IO, TestTemplateVars] {
      case (conf, TestTemplateVars(to)) =>
        Email
          .text(
            from = From(conf.from),
            to = To(Mailbox.unsafeFromString(to)),
            subject = Subject(s"Test subject"),
            body = Body.Ascii("Hello World!")
          )
          .pure[IO]
    }

  private val defaultConf = QuicksendConf(from = Mailbox.unsafeFromString("my@startup.com"))
  private val successReplies = Replies(Reply(Code.`220`, "", ""))

  val fixtures: Fixtures[IO, Flow, Fxt] =
    Fixtures[IO, Flow, Fxt] {

      case Flow.`Happy` =>
        val pencilClient = mock[Client[IO]]
        when(pencilClient.send(mm.any[Email]))
          .thenReturn(successReplies.pure[IO])

        (
          emailTemplate,
          pencilClient,
          Quicksend
            .Deps[IO](defaultConf, pencilClient.pure[Resource[IO, *]])
        )

      case Flow.DelayedSend(delay) =>
        val pencilClient = mock[Client[IO]]
        when(pencilClient.send(mm.any[Email]))
          .thenReturn(Timer[IO].sleep(delay) >> successReplies.pure[IO])

        (
          emailTemplate,
          pencilClient,
          Quicksend.Deps[IO](defaultConf, pencilClient.pure[Resource[IO, *]])
        )

    }
}
