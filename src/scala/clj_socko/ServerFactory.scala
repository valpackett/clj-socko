package clj_socko
import java.util.{Map=>JMap}
import scala.collection.JavaConverters._
import org.mashupbots.socko.webserver.{WebServer,WebServerConfig}
import org.mashupbots.socko.routes.{Routes,HttpRequest,WebSocketHandshake,WebSocketFrame}
import org.mashupbots.socko.events.{SockoEvent,HttpResponseStatus,HttpRequestEvent}
import akka.actor.{Props,Actor,ActorRef,ActorSystem}
import akka.routing.FromConfig

class NotFoundActor extends Actor {
  def receive = {
    case req: HttpRequestEvent =>
      req.response.write(HttpResponseStatus.NOT_FOUND)
      context.stop(self)
  }
}

trait Handler {
  def apply(system: ActorSystem): ActorRef
}

object NotFoundHandler extends Handler {
  def apply(system: ActorSystem) =
    system.actorOf(Props[NotFoundActor])
}

object ServerFactory {
  def headersAsJava(a: Map[String, String]) = a.asJava
  def headersAsScala(a: JMap[String, String]) = a.asScala.toMap

  def makeServer(hnd: JMap[String, Handler], cnf: JMap[String, Object], system: ActorSystem) = {
    val config = cnf.asScala
    val conf = new WebServerConfig(
      serverName = config.get("server-name").getOrElse("ClojureServer").asInstanceOf[String],
      hostname = config.get("host").getOrElse("0.0.0.0").asInstanceOf[String],
      port = config.get("port").getOrElse(8080.toLong).asInstanceOf[Long].toInt
    )
    val handlers = hnd.asScala
    val http_handler = handlers.get("http").getOrElse(NotFoundHandler)
    val ws_handler = handlers.get("ws").getOrElse(NotFoundHandler)
    val routes = Routes({
      case HttpRequest(req) => http_handler(system) ! req
      case WebSocketHandshake(hs) => hs.authorize()
      case WebSocketFrame(f) => ws_handler(system) ! f
    })
    new WebServer(conf, routes, system)
  }

  def runServer(server: WebServer) = {
    server.start()
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { server.stop() }
    })
  }
}
