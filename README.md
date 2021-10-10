#Qucksend

Functional email templates for Cats Effect programs. 

## Define, resolve, send
Anywhere in your code, independent, pure.

```scala
import com.minosiants.pencil.data.Mailbox
import io.github.mkotsur.quicksend.conf.QuicksendConf
import io.github.mkotsur.quicksend.Template, Template.syntax._
import cats.effect.IO

// Define variables...
case class NameAge[F[_]](name: String, age: Int)

// ... and template.
val happyBirthdayTpl = Template[IO, NameAge] {
  case (conf, NameAge(name, age)) =>
    Email
      .text(
        // Choose between default or customized per email "From" 
        from = From(conf.from),
        to = To(Mailbox.unsafeFromString("neo@matrix.com")),
        subject = Subject(s"Happy Birthday!"),
        body = Body.Ascii(s"Congratulations, $name! You are $age years old now.")
      )
      .pure[IO]
}
// Resolve template into sendable email, or emails
val emails = List(NameAge("Sofie", 3), NameAge("John", 4)).map(happyBirthdayTpl.seal)

// Send email and receive SMTP replies
val conf = QuicksendConf(
  from = Mailbox.unsafeFromString("greeter@birthday.net"), 
  host = ???, port = ???, credentials = ???, // Your SMTP settings
  readTimeout = ???, writeTimeout = ??? // ... or trust defaults
)

val replies: IO[Replies] = Quicksend[IO](conf).send(happyBirthdayEml)
```

## About Quicksend

Quicksend came out of my desire to avoid boilerplate when sending emails from backend services. Unlike other email libraries, it adds the notion of template and variables that can be leveraged to effortlessly and cleanly add the email sending into any pure functional program. 

## Do we still need emails?

Emails have been with us for many years, and they are here to stay, it seems. When making a new application, there are many good reasons to choose to send emails over building custom pages or screens, especially at the MVP stage of application development:
You hook into the user's pre-configured notifications pattern, fewer chances to come across as annoying;
Users can choose their favourite device to open them;
Provided the wording is good, one can find emails after many years;
However, one should be aware of the [inherent security risks](https://en.wikipedia.org/wiki/Email_encryption) of sending secrets in emails; there are techniques to mitigate those risks. And, of course, unsolicited or spammy emails is never a good idea. 

