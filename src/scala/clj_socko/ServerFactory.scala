package clj_socko
import scala.collection.JavaConverters._
import org.mashupbots.socko.webserver.{WebServer,WebServerConfig}
import org.mashupbots.socko.routes.{Routes,HttpRequest}
import akka.actor.{Props,ActorSystem}

object ServerFactory {
  def headersAsJava(a: Map[String, String]) = a.asJava
  def headersAsScala(a: java.util.Map[String, String]) = a.asScala.toMap

  def makeServer(handler: Props, cnf: java.util.Map[String, Object], system: ActorSystem) = {
    val config = cnf.asScala
    val conf = new WebServerConfig(
      serverName = config.get("server-name").getOrElse("ClojureServer").asInstanceOf[String],
      hostname = config.get("hostname").getOrElse("0.0.0.0").asInstanceOf[String],
      port = config.get("port").getOrElse(8080.toLong).asInstanceOf[Long].toInt
    )
    val routes = Routes({
      case HttpRequest(req) => system.actorOf(handler) ! req
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
