package play.api.i18n

import play.api.i18n.Messages.UrlMessageSource
import play.api.{Play, Application}
import java.io.File
import scala.util.{Try, Success}

/**
 * Using play structure in order to re-use all the original MessagesPlugin private[i18n] internal classes
 * Created by Ruben Lozano Díaz on 11/02/2014.
 */
object MMessages {

  /**
   * Translates a message.
   *
   * Uses `java.text.MessageFormat` internally to format the message.
   *
   * @param key the message key
   * @param args the message arguments
   * @return the formatted message or a default rendering if the key wasn’t defined
   */
  def apply(key: String, args: Any*)(implicit lang: Lang): String = {
    Play.maybeApplication.flatMap { app =>
      app.plugin[MultipleMessagesPlugin].map(_.api.translate(key, args)).getOrElse(throw new Exception("this plugin was not registered or disabled"))
    }.getOrElse(noMatch(key, args))
  }



  /**
   * Check if a message key is defined.
   * @param key the message key
   * @return a boolean
   */
  def isDefinedAt(key: String)(implicit lang: Lang): Boolean = {
    Play.maybeApplication.map { app =>
      app.plugin[MultipleMessagesPlugin].map(_.api.isDefinedAt(key)).getOrElse(throw new Exception("this plugin was not registered or disabled"))
    }.getOrElse(false)
  }



  /**
   * Retrieves all messages defined in this application.
   */
  def messages(implicit app: Application): Map[String, Map[String, String]] = {
    app.plugin[MultipleMessagesPlugin].map(_.api.messages).getOrElse(throw new Exception("this plugin was not registered or disabled"))
  }

  private def noMatch(key: String, args: Seq[Any]) = key
}


class MultipleMessagesPlugin(app: Application) extends DefaultMessagesPlugin(app) {

  val DEFAULT_FILENAME = "messagelisting.properties"
  lazy val messageListingName = Try(app.configuration.getString("i18n.messagelisting").getOrElse(DEFAULT_FILENAME)) match { case Success(s) => s case _ => DEFAULT_FILENAME}
  import scala.collection.JavaConverters._

  private def loadMessagesStr(lang:String): Map[String,String] =
    scala.io.Source.fromInputStream(app.classloader.getResourceAsStream(messageListingName)).getLines().map{ messageFile =>
      app.classloader.getResources(lang+File.separator+messageFile).asScala.toList.reverse.map{ messageUrl =>
         Messages.messages(UrlMessageSource(messageUrl), messageFile).fold(e => throw e, identity)
      }.foldLeft(Map.empty[String, String]) { _ ++ _ }
    }.foldLeft(Map.empty[String, String]) { _ ++ _ }



  override protected lazy val messages = {
      Lang.availables(app).map(_.code).map { lang =>
        (lang, loadMessagesStr(lang))
      }.toMap
  }


  /**
   * The underlying internationalisation API.
   */
  override lazy val api = MessagesApi(messages)

  /**
   * Loads all configuration and message files defined in the classpath.
   */
  override def onStart() {
    messages
  }
}

