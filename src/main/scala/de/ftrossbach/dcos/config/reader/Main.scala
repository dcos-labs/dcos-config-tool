package de.ftrossbach.dcos.config.reader

import java.net.URL

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import de.ftrossbach.dcos.config.reader.ConfigReader.Update
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import de.ftrossbach.dcos.config.reader.RepositoryFacade.AddRepository

import scala.concurrent.Future

/**
  * Created by ftr on 13/05/16.
  */
object Main {
  implicit val system = ActorSystem("dcos-config")
  def main(args: Array[String]) {



    val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val facade = system.actorOf(Props[RepositoryFacade])




    val bindingFuture = Http().bindAndHandle(new RestRouter().route(facade), "localhost", 8080)

    bindingFuture.onSuccess({
      case _ => facade ! AddRepository("universe", "https://universe.mesosphere.com/repo")
    })
  }

}
