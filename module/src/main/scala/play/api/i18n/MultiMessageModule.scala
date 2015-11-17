package play.api.i18n

import javax.inject.Singleton
import play.api.{Environment, Configuration}
import play.api.inject.{Binding, Module}

/**
 * Created by peterwhitehead on 30/10/2015.
 */
@Singleton
class MultiMessageModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[MMessages].to[MMessagesImpl].eagerly
    )
  }
}
