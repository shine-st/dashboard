package shine.st.dashboard.generate

import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ApplicationLifecycle, DefaultApplicationLifecycle, bind}

/**
  * Created by shinest on 17/07/2017.
  */
trait PlayInjector {

  val fakeApp = new GuiceApplicationBuilder()
    .in(Mode.Dev)
    .bindings(bind[ApplicationLifecycle].to[DefaultApplicationLifecycle])
    .build

  val injector = fakeApp.injector

  def closeInjector = injector.instanceOf[DefaultApplicationLifecycle].stop
}
