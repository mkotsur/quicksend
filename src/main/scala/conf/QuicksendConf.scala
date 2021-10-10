package io.github.mkotsur.quicksend
package conf

import cats.effect.{Blocker, ContextShift, Sync}
import com.minosiants.pencil.data.{Credentials, From, Mailbox}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect2.syntax.CatsEffectConfigSource

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object QuicksendConf {
  private val src = ConfigSource.default.at("quicksend")

  def loadF[F[_]: Sync: ContextShift]: F[QuicksendConf] = {
    import pureconfig.generic.auto._
    Blocker[F].use(src.loadF[F, QuicksendConf])
  }
}

case class QuicksendConf(
    host: String = "localhost",
    port: Int = 25,
    credentials: Option[Credentials] = None,
    readTimeout: FiniteDuration = 5.minutes,
    writeTimeout: FiniteDuration = 5.minutes,
    from: Mailbox
)
