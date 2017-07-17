package shine.st.dashboard.controllers

import javax.inject.{Inject, _}

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Configuration
import play.api.cache.SyncCacheApi
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import shine.st.dashboard.actors.SummarizeActor
import shine.st.dashboard.data.Model.Tag
import shine.st.dashboard.utils.RandomUtils

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class DashBoardController @Inject()(config: Configuration, cache: SyncCacheApi, cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */

  def dashboard() = Action { implicit request: Request[AnyContent] =>
    Ok(shine.st.dashboard.views.html.dashboard())
  }

  def customerInsight() = Action { implicit request: Request[AnyContent] =>
    val (x, y) = cache.get[(List[Tag], List[Tag])]("member.tag") match {
      case Some(data) => data
      case None =>
        val (nx, ny, ni) = generateRandomTwoDimArr(config.get[Seq[String]]("tag.x").toList, config.get[Seq[String]]("tag.y").toList, 30000)
        cache.set("member.tag", (nx, ny))
        cache.set("member.tag.intersection", ni)

        (nx, ny)
    }

    Ok(shine.st.dashboard.views.html.customer_insight(x, y))
  }

  def realTimeSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      SummarizeActor.props(out)
    }

  }

  def generateRandomTwoDimArr(x: List[String], y: List[String], max: Int) = {
    val listX = x.zipWithIndex.map { case (s, index) => Tag(index, s, RandomUtils.randomInt(max)) }
    val listY = y.zipWithIndex.map { case (s, index) => Tag(index, s, RandomUtils.randomInt(max)) }

    (listX, listY, listX.map { xd =>
      listY.map { xy =>
        if (xd.count > xy.count)
          RandomUtils.randomInt(xd.count)
        else
          RandomUtils.randomInt(xy.count)
      }
    })
  }

}
