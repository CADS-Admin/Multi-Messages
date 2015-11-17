package play.api.i18n

import java.io.File
import java.text.MessageFormat
import javax.inject.{Singleton, Inject}
import play.api.mvc._
import play.api._
import play.api.i18n.Messages.UrlMessageSource
import play.mvc.Http
import scala.util.{Success, Try}
import scala.collection.JavaConverters._

trait MMessages extends MessagesApi

/**
 * Using play structure in order to re-use all the original MessagesPlugin private[i18n] internal classes
 * Created by Ruben Lozano Díaz on 11/02/2014.
 */
@Singleton
class MMessagesImpl @Inject() (environment: Environment, configuration: Configuration, langs: Langs) extends MMessages {
  private val config = PlayConfig(configuration)

  val DEFAULT_FILENAME = "messagelisting.properties"
  lazy val messageListingName = Try(configuration.getString("i18n.messagelisting").getOrElse(DEFAULT_FILENAME)) match {
    case Success(s) => s case _ => DEFAULT_FILENAME
  }
  val messages: Map[String, Map[String, String]] = allMessages

  override def preferred(candidates: Seq[Lang]) = Messages(langs.preferred(candidates), this)

  override def preferred(request: mvc.RequestHeader) = {
    val maybeLangFromCookie = request.cookies.get(langCookieName)
      .flatMap(c => Lang.get(c.value))
    val lang = langs.preferred(maybeLangFromCookie.toSeq ++ request.acceptLanguages)
    Messages(lang, this)
  }

  override def preferred(request: Http.RequestHeader) = preferred(request._underlyingHeader())

  override def setLang(result: Result, lang: Lang) = result.withCookies(Cookie(langCookieName, lang.code, path = Session.path, domain = Session.domain,
    secure = langCookieSecure, httpOnly = langCookieHttpOnly))

  override def clearLang(result: Result) = result.discardingCookies(DiscardingCookie(langCookieName, path = Session.path, domain = Session.domain,
    secure = langCookieSecure))

  override def apply(key: String, args: Any*)(implicit lang: Lang): String = {
    translate(key, args).getOrElse(noMatch(key, args))
  }

  override def apply(keys: Seq[String], args: Any*)(implicit lang: Lang): String = {
    keys.foldLeft[Option[String]](None) {
      case (None, key) => translate(key, args)
      case (acc, _) => acc
    }.getOrElse(noMatch(keys.last, args))
  }

  override def translate(key: String, args: Seq[Any])(implicit lang: Lang): Option[String] = {
    val langsToTry: List[Lang] =
      List(lang, Lang(lang.language, ""), Lang("default", ""), Lang("default.play", ""))
    val pattern: Option[String] =
      langsToTry.foldLeft[Option[String]](None)((res, lang) =>
        res.orElse(messages.get(lang.code).flatMap(_.get(key))))
    pattern.map(pattern =>
      new MessageFormat(pattern, lang.toLocale).format(args.map(_.asInstanceOf[java.lang.Object]).toArray))
  }

  override def isDefinedAt(key: String)(implicit lang: Lang): Boolean = {
    val langsToTry: List[Lang] = List(lang, Lang(lang.language, ""), Lang("default", ""), Lang("default.play", ""))

    langsToTry.foldLeft[Boolean](false)({ (acc, lang) =>
      acc || messages.get(lang.code).map(_.isDefinedAt(key)).getOrElse(false)
    })
  }

  lazy val langCookieName = config.getDeprecated[String]("play.i18n.langCookieName", "application.lang.cookie")

  lazy val langCookieSecure = config.get[Boolean]("play.i18n.langCookieSecure")

  lazy val langCookieHttpOnly = config.get[Boolean]("play.i18n.langCookieHttpOnly")

  private def noMatch(key: String, args: Seq[Any]) = key

  private def loadMessagesStr(lang:String): Map[String,String] =
    scala.io.Source.fromInputStream(environment.classLoader.getResourceAsStream(messageListingName)).getLines().map{ messageFile =>
      environment.classLoader.getResources(lang+File.separator+messageFile).asScala.toList.reverse.map{ messageUrl =>
        Messages.parse(UrlMessageSource(messageUrl), messageFile).fold(e => throw e, identity)
      }.foldLeft(Map.empty[String, String]) { _ ++ _ }
    }.foldLeft(Map.empty[String, String]) { _ ++ _ }

  private def allMessages = {
    langs.availables.map(_.code).map { lang =>
      (lang, loadMessagesStr(lang))
    }.toMap
  }
}
